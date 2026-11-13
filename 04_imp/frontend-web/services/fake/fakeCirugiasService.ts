import { ApiError } from '../apiClient'
import type { CirugiasService } from '../cirugiasService'
import type { FichasInternasService } from '../fichasInternasService'
import { guardarDatos, leerGuardado } from './fakeFichasInternasService'
import {
  QUIROFANO_ABRE, QUIROFANO_CIERRA,
  type Cirugia, type DatosCirugia, type PropuestaCirugia, type Quirofano,
} from '~/models/cirugia'
import { DURACION_CIRUGIA, nombreEspecialidad, type Especialidad } from '~/models/especialidad'
import type { MedicoActual } from '~/models/medico'
import { horaActual, hoyISO, minutosDe, sumarDias, sumarMinutos } from '~/models/fechas'
import { mismoNombre } from '~/models/texto'

/** Quirofanos de prueba: no se comparten entre especialidades. */
const QUIROFANOS: Quirofano[] = [
  { codigo: 'G-1', especialidad: 'GASTROENTEROLOGIA', limpiezaMin: 40 },
  { codigo: 'G-2', especialidad: 'GASTROENTEROLOGIA', limpiezaMin: 30 },
  { codigo: 'H-1', especialidad: 'HEMATOLOGIA', limpiezaMin: 40 },
  { codigo: 'D-1', especialidad: 'DERMATOLOGIA', limpiezaMin: 30 },
]

const DIAS_HABILES_MAXIMOS = 15

/**
 * Cirugias en memoria con los datos del Figma. Cero choques: quirofano (incluida la limpieza), cirujano y
 * paciente. Una cirugia urgente se busca desde ahora (tambien fuera de horario) y, si no hay hueco hoy, desplaza a
 * la primera cirugia no urgente de hoy que todavia no empezo.
 */
export class FakeCirugiasService implements CirugiasService {
  cirugias: Cirugia[]
  private siguiente = 58

  constructor(private fichas: FichasInternasService, private ahora: () => Date = () => new Date(),
              private clave?: string) {
    const hoy = hoyISO(this.ahora())
    this.cirugias = leerGuardado<Cirugia[]>(clave, hoy) ?? semilla(hoy)
    this.siguiente = Math.max(58, ...this.cirugias.map(c => c.numero + 1))
  }

  private guardar() {
    guardarDatos(this.clave, hoyISO(this.ahora()), this.cirugias)
  }

  async proponer(d: DatosCirugia, medico: MedicoActual): Promise<PropuestaCirugia> {
    const rango = DURACION_CIRUGIA[medico.especialidad]
    if (!rango) throw new ApiError('ESPECIALIDAD_NO_OPERA', `${nombreEspecialidad(medico.especialidad)} no opera en quirófano.`, 422)
    const ficha = await this.fichas.porTicket(d.fichaTicket)
    if (!ficha || ficha.estado !== 'ATENDIDA' || !mismoNombre(ficha.medico, medico.nombre)) {
      throw new ApiError('FICHA_NO_ATENDIDA', 'Solo el especialista que atendió la ficha puede programar la cirugía.', 422)
    }
    if (d.duracionMin < rango[0] || d.duracionMin > rango[1]) {
      throw new ApiError('DURACION_FUERA_DE_RANGO',
        `${nombreEspecialidad(medico.especialidad)}: de ${rango[0]} a ${rango[1]} min.`, 422)
    }
    const quirofanos = QUIROFANOS.filter(q => q.especialidad === medico.especialidad)
    const ahora = this.ahora()
    if (d.urgente) {
      const desde = sumarMinutos(horaActual(ahora), 10 - (ahora.getMinutes() % 10))
      const hoy = hoyISO(ahora)
      const hueco = this.hueco(quirofanos, hoy, desde, '23:59', d.duracionMin, medico.nombre, ficha.pacienteId)
      if (hueco) return { ...hueco, especialidad: medico.especialidad, desplaza: null }
      return this.desplazando(quirofanos, hoy, desde, d.duracionMin, medico)
    }
    let fecha = hoyISO(ahora)
    for (let i = 0; i < DIAS_HABILES_MAXIMOS; i++) {
      fecha = sumarDias(fecha, 1, true)
      const hueco = this.hueco(quirofanos, fecha, QUIROFANO_ABRE, QUIROFANO_CIERRA, d.duracionMin, medico.nombre,
        ficha.pacienteId)
      if (hueco) return { ...hueco, especialidad: medico.especialidad, desplaza: null }
    }
    throw new ApiError('SIN_HUECO', 'No hay quirófano libre en los próximos 15 días hábiles.', 409)
  }

  async confirmar(d: DatosCirugia, p: PropuestaCirugia, medico: MedicoActual): Promise<Cirugia> {
    const ficha = await this.fichas.porTicket(d.fichaTicket)
    if (!ficha) throw new ApiError('NO_ENCONTRADA', 'La ficha no existe', 404)
    const quirofano = QUIROFANOS.find(q => q.codigo === p.quirofano)!
    const desplazada = p.desplaza ? this.cirugias.find(c => c.numero === p.desplaza!.numero) : undefined
    const choca = this.ocupado(p.quirofano, p.fecha, p.inicio, p.limpiezaHasta, medico.nombre, ficha.pacienteId,
      desplazada?.numero)
    if (choca) {
      throw new ApiError('CHOQUE_DE_HORARIO', `Otro especialista confirmó ese horario del Quirófano ${p.quirofano} `
        + 'mientras revisabas la propuesta. No se guardó nada: pide una nueva propuesta.', 409)
    }
    if (desplazada && p.desplaza) {
      const duracion = desplazada.duracionMin
      Object.assign(desplazada, { fecha: p.desplaza.nuevaFecha, inicio: p.desplaza.nuevoInicio,
        fin: sumarMinutos(p.desplaza.nuevoInicio, duracion),
        limpiezaHasta: sumarMinutos(p.desplaza.nuevoInicio, duracion + quirofano.limpiezaMin) })
    }
    const nueva: Cirugia = {
      numero: this.siguiente++, fichaTicket: ficha.ticket, pacienteId: ficha.pacienteId,
      nombrePaciente: ficha.nombrePaciente, numeroAfiliado: ficha.numeroAfiliado, edad: ficha.edad,
      especialidad: medico.especialidad, quirofano: p.quirofano, cirujano: medico.nombre, fecha: p.fecha,
      inicio: p.inicio, fin: p.fin, limpiezaHasta: p.limpiezaHasta, duracionMin: d.duracionMin, urgente: d.urgente,
      gravedad: d.gravedad, estado: 'PROGRAMADA',
    }
    this.cirugias.push(nueva)
    this.guardar()
    return { ...nueva }
  }

  async delDia(medico: MedicoActual, fecha: string) {
    const codigos = QUIROFANOS.filter(q => q.especialidad === medico.especialidad).map(q => q.codigo)
    return this.cirugias.filter(c => codigos.includes(c.quirofano) && c.fecha === fecha)
      .sort((a, b) => a.inicio.localeCompare(b.inicio)).map(c => ({ ...c }))
  }

  async delPaciente(pacienteId: number) {
    return this.cirugias.filter(c => c.pacienteId === pacienteId).map(c => ({ ...c }))
  }

  async porNumero(numero: number) {
    const c = this.cirugias.find(x => x.numero === numero)
    return c ? { ...c } : null
  }

  async marcar(numero: number, estado: 'REALIZADA' | 'CANCELADA') {
    const c = this.cirugias.find(x => x.numero === numero)
    if (!c) throw new ApiError('NO_ENCONTRADA', 'La cirugía no existe', 404)
    if (c.estado !== 'PROGRAMADA') throw new ApiError('ESTADO_NO_PERMITE', 'La cirugía ya no está programada', 409)
    c.estado = estado
    this.guardar()
    return { ...c }
  }

  /** Quirofanos de la especialidad que existen en el Figma (para la linea de tiempo). */
  quirofanosDe(especialidad: Especialidad): Quirofano[] {
    return QUIROFANOS.filter(q => q.especialidad === especialidad)
  }

  private hueco(quirofanos: Quirofano[], fecha: string, desde: string, hasta: string, duracion: number,
                cirujano: string, pacienteId: number) {
    let mejor: Omit<PropuestaCirugia, 'especialidad' | 'desplaza'> | null = null
    for (const q of quirofanos) {
      for (let inicio = desde; minutosDe(inicio) + duracion <= minutosDe(hasta); inicio = sumarMinutos(inicio, 10)) {
        const limpiezaHasta = sumarMinutos(inicio, duracion + q.limpiezaMin)
        if (!this.ocupado(q.codigo, fecha, inicio, limpiezaHasta, cirujano, pacienteId)) {
          if (!mejor || inicio < mejor.inicio) {
            mejor = { quirofano: q.codigo, fecha, inicio, fin: sumarMinutos(inicio, duracion), limpiezaHasta,
              limpiezaMin: q.limpiezaMin }
          }
          break
        }
      }
    }
    return mejor
  }

  private desplazando(quirofanos: Quirofano[], hoy: string, desde: string, duracion: number,
                      medico: MedicoActual): PropuestaCirugia {
    const codigos = quirofanos.map(q => q.codigo)
    const victima = this.cirugias
      .filter(c => codigos.includes(c.quirofano) && c.fecha === hoy && c.estado === 'PROGRAMADA' && !c.urgente
        && c.inicio >= desde)
      .sort((a, b) => a.inicio.localeCompare(b.inicio))[0]
    if (!victima) throw new ApiError('SIN_HUECO', 'No hay quirófano libre ni cirugía que se pueda desplazar hoy.', 409)
    const q = quirofanos.find(x => x.codigo === victima.quirofano)!
    let nuevaFecha = hoy
    let nuevo: ReturnType<FakeCirugiasService['hueco']> = null
    for (let i = 0; i < DIAS_HABILES_MAXIMOS && !nuevo; i++) {
      nuevaFecha = sumarDias(nuevaFecha, 1, true)
      nuevo = this.hueco([q], nuevaFecha, QUIROFANO_ABRE, QUIROFANO_CIERRA, victima.duracionMin, victima.cirujano,
        victima.pacienteId)
    }
    return {
      quirofano: victima.quirofano, especialidad: medico.especialidad, fecha: hoy, inicio: victima.inicio,
      fin: sumarMinutos(victima.inicio, duracion), limpiezaHasta: sumarMinutos(victima.inicio, duracion + q.limpiezaMin),
      limpiezaMin: q.limpiezaMin,
      desplaza: { ...victima, nuevaFecha, nuevoInicio: nuevo?.inicio ?? QUIROFANO_ABRE },
    }
  }

  /** ¿Choca con alguna cirugia viva del quirofano (con limpieza), del cirujano o del paciente? */
  private ocupado(quirofano: string, fecha: string, inicio: string, limpiezaHasta: string, cirujano: string,
                  pacienteId: number, ignorar?: number): boolean {
    const a = minutosDe(inicio)
    const b = minutosDe(limpiezaHasta)
    return this.cirugias.some((c) => {
      if (c.fecha !== fecha || c.estado === 'CANCELADA' || c.numero === ignorar) return false
      const mismoQuirofano = c.quirofano === quirofano && a < minutosDe(c.limpiezaHasta) && minutosDe(c.inicio) < b
      const mismaPersona = (mismoNombre(c.cirujano, cirujano) || c.pacienteId === pacienteId)
        && a < minutosDe(c.fin) && minutosDe(c.inicio) < b
      return mismoQuirofano || mismaPersona
    })
  }
}

function semilla(hoy: string): Cirugia[] {
  const base = (c: Partial<Cirugia> & Pick<Cirugia, 'numero' | 'nombrePaciente' | 'quirofano' | 'inicio' | 'fin'
    | 'limpiezaHasta'>): Cirugia => ({
    fichaTicket: 0, pacienteId: 900 + c.numero, numeroAfiliado: '', edad: 50, especialidad: 'GASTROENTEROLOGIA',
    cirujano: 'Raúl Vargas', fecha: hoy, duracionMin: minutosDe(c.fin) - minutosDe(c.inicio), urgente: false,
    gravedad: 'MEDIA', estado: 'PROGRAMADA', ...c,
  })
  return [
    base({ numero: 50, nombrePaciente: 'Ana Quispe', quirofano: 'G-1', inicio: '07:30', fin: '08:30',
      limpiezaHasta: '09:10', estado: 'REALIZADA' }),
    base({ numero: 51, nombrePaciente: 'Marco Ríos', quirofano: 'G-1', inicio: '10:00', fin: '11:30',
      limpiezaHasta: '12:10', cirujano: 'Patricia Arce' }),
    base({ numero: 52, nombrePaciente: 'Luis Gómez', quirofano: 'G-1', inicio: '15:00', fin: '16:00',
      limpiezaHasta: '16:40' }),
    base({ numero: 53, nombrePaciente: 'Lidia Paz', quirofano: 'G-2', inicio: '09:00', fin: '10:00',
      limpiezaHasta: '10:30', cirujano: 'Patricia Arce' }),
    base({ numero: 54, nombrePaciente: 'Juan Mamani', quirofano: 'G-2', inicio: '13:30', fin: '14:30',
      limpiezaHasta: '15:00', urgente: true, gravedad: 'ALTA', pacienteId: 115, fichaTicket: 1031 }),
    base({ numero: 55, nombrePaciente: 'Raúl Choque', quirofano: 'G-2', inicio: '16:00', fin: '17:30',
      limpiezaHasta: '18:00', cirujano: 'Patricia Arce' }),
    base({ numero: 40, nombrePaciente: 'Marco Ríos', pacienteId: 113, numeroAfiliado: '4587-12',
      especialidad: 'DERMATOLOGIA', quirofano: 'D-1', fecha: '2026-09-05', inicio: '09:00', fin: '09:45',
      limpiezaHasta: '10:15', cirujano: 'Daniela Salinas', estado: 'REALIZADA' }),
  ]
}
