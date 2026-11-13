import { ApiError } from '../apiClient'
import type { FichasInternasService } from '../fichasInternasService'
import { DURACION_INTERNA, type Especialidad } from '~/models/especialidad'
import {
  compararPrioridad, EDAD_PEDIATRIA, fichaActiva, MAX_MOTIVO,
  type CitaOrigen, type FichaInterna, type PacienteBuscado, type ResultadoFicha, type SolicitudFicha, type TablaFicha,
} from '~/models/fichaInterna'
import type { MedicoActual } from '~/models/medico'
import { horaActual, hoyISO, minutosDe, sumarDias, sumarMinutos } from '~/models/fechas'
import { mismoNombre } from '~/models/texto'

interface Especialista { nombre: string; especialidad: Especialidad; consultorio: string; duracionMin: number }

/** Especialistas de prueba (nombres del Figma). Endocrinologia no tiene: sus fichas quedan EN_ESPERA. */
const ESPECIALISTAS: Especialista[] = [
  { nombre: 'Raúl Vargas', especialidad: 'GASTROENTEROLOGIA', consultorio: '12', duracionMin: 25 },
  { nombre: 'Patricia Arce', especialidad: 'GASTROENTEROLOGIA', consultorio: '14', duracionMin: 25 },
  { nombre: 'Miguel Paredes', especialidad: 'CARDIOLOGIA', consultorio: '15', duracionMin: 30 },
  { nombre: 'Daniela Salinas', especialidad: 'DERMATOLOGIA', consultorio: '16', duracionMin: 20 },
  { nombre: 'Sonia Pinto', especialidad: 'HEMATOLOGIA', consultorio: '17', duracionMin: 30 },
  { nombre: 'Óscar Rocha', especialidad: 'NEUMOLOGIA', consultorio: '18', duracionMin: 25 },
  { nombre: 'Gloria Mendoza', especialidad: 'NEFROLOGIA', consultorio: '19', duracionMin: 30 },
  { nombre: 'Iván Cuéllar', especialidad: 'INFECTOLOGIA', consultorio: '20', duracionMin: 40 },
  { nombre: 'Rocío Aliaga', especialidad: 'MEDICINA_TRABAJO', consultorio: '21', duracionMin: 30 },
  { nombre: 'Hugo Lima', especialidad: 'TERAPIA_INTENSIVA', consultorio: 'UTI', duracionMin: 45 },
]

/** Pacientes que se pueden buscar por N° de afiliado en una urgencia. */
const DIRECTORIO: PacienteBuscado[] = [
  { pacienteId: 104, nombre: 'Rosa Choque', numeroAfiliado: '7781-03', edad: 68 },
  { pacienteId: 103, nombre: 'Luis Quispe', numeroAfiliado: '5012-08', edad: 39 },
  { pacienteId: 120, nombre: 'Mario Cruz', numeroAfiliado: '3310-07', edad: 40 },
]

const INICIO_JORNADA = '08:00'
const FIN_JORNADA = '16:00'
const DIAS_HABILES_MAXIMOS = 15

/**
 * Fichas internas en memoria con los datos del Figma (agenda del Dr. Ramos, agenda de fichas del Dr. Vargas y
 * cola de Gastroenterologia). Aplica las reglas del 10/11/2026: una derivacion por cita, Terapia Intensiva solo
 * con urgencia, Pediatria < 10 anios, una ficha activa por paciente y busqueda de hasta 15 dias habiles.
 * El desplazamiento por prioridad lo decide el backend (T100-20); aqui una urgencia toma el siguiente hueco de hoy.
 */
export class FakeFichasInternasService implements FichasInternasService {
  fichas: FichaInterna[]
  private siguienteTicket = 1033

  /** @param clave si se indica, los datos de prueba se guardan en el navegador y sobreviven a una recarga (ese dia). */
  constructor(private ahora: () => Date = () => new Date(), private clave?: string) {
    const hoy = hoyISO(this.ahora())
    this.fichas = leerGuardado<FichaInterna[]>(clave, hoy) ?? semilla(hoy)
    this.siguienteTicket = Math.max(1033, ...this.fichas.map(f => f.ticket + 1))
  }

  private guardar() {
    guardarDatos(this.clave, hoyISO(this.ahora()), this.fichas)
  }

  async generar(s: SolicitudFicha, medico: MedicoActual, cita: CitaOrigen | null): Promise<ResultadoFicha> {
    const hoy = hoyISO(this.ahora())
    const rechazo = (codigo: string, mensaje: string): ResultadoFicha => ({ resultado: 'RECHAZADA', codigo, mensaje })
    if (!medico.puedeDerivar) return rechazo('MEDICO_NO_DERIVA', 'Solo Médico General y Pediatría pueden derivar.')
    const motivo = s.motivo.trim()
    if (motivo.length < 1 || motivo.length > MAX_MOTIVO) {
      return rechazo('MOTIVO_INVALIDO', `El motivo debe tener de 1 a ${MAX_MOTIVO} caracteres.`)
    }
    if (s.especialidad === 'TERAPIA_INTENSIVA' && !s.urgencia) {
      return rechazo('TERAPIA_SOLO_URGENCIA', 'Terapia Intensiva solo se asigna con Urgencia.')
    }
    if (!s.urgencia) {
      const valida = cita && cita.fecha === hoy && (cita.estado === 'ASISTENCIA_CONFIRMADA' || cita.estado === 'ATENDIDA')
      if (!valida) return rechazo('CITA_NO_VALIDA', 'La cita debe ser tuya, de hoy y estar confirmada o atendida.')
    }
    if (cita) {
      const previa = this.fichas.find(f => f.citaOrigenId === cita.citaId)
      if (previa) {
        return rechazo('CITA_YA_DERIVADA',
          `Esta cita ya tiene una ficha interna (Ticket N° ${previa.ticket}). Solo se permite una derivación por cita.`)
      }
    }
    if (medico.especialidad === 'PEDIATRIA' && s.edad >= EDAD_PEDIATRIA) {
      return rechazo('EDAD_NO_PEDIATRICA', 'Pediatría solo atiende a menores de 10 años.')
    }
    if (this.fichas.some(f => f.pacienteId === s.pacienteId && fichaActiva(f))) {
      return rechazo('PACIENTE_CON_FICHA_ACTIVA', 'El paciente ya tiene una ficha interna activa.')
    }

    const ficha: FichaInterna = {
      ticket: this.siguienteTicket++,
      citaOrigenId: cita?.citaId ?? null,
      pacienteId: s.pacienteId,
      nombrePaciente: s.nombrePaciente,
      numeroAfiliado: s.numeroAfiliado,
      edad: s.edad,
      especialidad: s.especialidad,
      tabla: medico.especialidad === 'PEDIATRIA' ? 'PEDIATRIA' : 'GENERAL',
      urgencia: s.urgencia,
      gravedad: s.gravedad,
      motivo,
      estado: 'EN_ESPERA',
      fecha: null,
      hora: null,
      duracionMin: DURACION_INTERNA[s.especialidad]?.[0] ?? 20,
      medico: null,
      consultorio: null,
      derivadoPor: medico.nombre,
      especialidadOrigen: medico.especialidad,
      llegada: this.ahora().toISOString(),
    }
    const hueco = s.urgencia ? this.huecoHoy(s.especialidad) : this.huecoDesdeManana(s.especialidad)
    if (hueco) {
      Object.assign(ficha, { estado: 'ASIGNADA', fecha: hueco.fecha, hora: hueco.hora, medico: hueco.medico.nombre,
        consultorio: hueco.medico.consultorio, duracionMin: hueco.medico.duracionMin })
    }
    this.fichas.push(ficha)
    this.guardar()
    return { resultado: ficha.estado === 'ASIGNADA' ? 'ASIGNADA' : 'EN_ESPERA', ficha: { ...ficha } }
  }

  async deCita(citaId: number) {
    const f = this.fichas.find(x => x.citaOrigenId === citaId)
    return f ? { ...f } : null
  }

  async porTicket(ticket: number) {
    const f = this.fichas.find(x => x.ticket === ticket)
    return f ? { ...f } : null
  }

  async delPaciente(pacienteId: number) {
    return this.fichas.filter(f => f.pacienteId === pacienteId).map(f => ({ ...f }))
  }

  async cola(especialidad: Especialidad, tabla: TablaFicha) {
    return this.fichas
      .filter(f => f.especialidad === especialidad && f.tabla === tabla && fichaActiva(f))
      .sort(compararPrioridad)
      .map(f => ({ ...f }))
  }

  async buscarPaciente(numeroAfiliado: string) {
    const p = DIRECTORIO.find(x => x.numeroAfiliado === numeroAfiliado.trim())
    return p ? { ...p } : null
  }

  async agendaEspecialista(medico: MedicoActual, fecha: string) {
    return this.fichas
      .filter(f => mismoNombre(f.medico, medico.nombre) && f.fecha === fecha)
      .sort((a, b) => (a.hora ?? '').localeCompare(b.hora ?? ''))
      .map(f => ({ ...f }))
  }

  async marcarAtendida(ticket: number, observaciones: string) {
    const f = this.exigir(ticket)
    if (f.estado !== 'ASIGNADA') throw new ApiError('ESTADO_NO_PERMITE', 'Solo se atiende una ficha ASIGNADA', 409)
    f.estado = 'ATENDIDA'
    f.observaciones = observaciones.trim() || undefined
    this.guardar()
    return { ...f }
  }

  async noSePresento(ticket: number) {
    const f = this.exigir(ticket)
    if (f.estado !== 'ASIGNADA') throw new ApiError('ESTADO_NO_PERMITE', 'Solo se cancela una ficha ASIGNADA', 409)
    const ahora = this.ahora()
    if (f.fecha && (f.fecha > hoyISO(ahora) || (f.fecha === hoyISO(ahora) && (f.hora ?? '') > horaActual(ahora)))) {
      throw new ApiError('TODAVIA_NO_ES_LA_HORA', `«No se presentó» se habilita después de las ${f.hora}.`, 409)
    }
    f.estado = 'CANCELADA'
    this.guardar()
    return { ...f }
  }

  private exigir(ticket: number): FichaInterna {
    const f = this.fichas.find(x => x.ticket === ticket)
    if (!f) throw new ApiError('NO_ENCONTRADA', 'La ficha no existe', 404)
    return f
  }

  /** Urgencia: hoy, desde ahora, con el especialista que se libere antes. */
  private huecoHoy(especialidad: Especialidad) {
    const ahora = this.ahora()
    const desde = sumarMinutos(horaActual(ahora), 10 - (ahora.getMinutes() % 10))
    return this.primerHueco(especialidad, hoyISO(ahora), desde, '23:30')
  }

  /** Sin urgencia: desde manana, hasta 15 dias habiles, con el especialista que tenga menos fichas ese dia. */
  private huecoDesdeManana(especialidad: Especialidad) {
    let fecha = hoyISO(this.ahora())
    for (let i = 0; i < DIAS_HABILES_MAXIMOS; i++) {
      fecha = sumarDias(fecha, 1, true)
      const hueco = this.primerHueco(especialidad, fecha, INICIO_JORNADA, FIN_JORNADA)
      if (hueco) return hueco
    }
    return null
  }

  private primerHueco(especialidad: Especialidad, fecha: string, desde: string, hasta: string) {
    const medicos = ESPECIALISTAS.filter(m => m.especialidad === especialidad)
      .sort((a, b) => this.fichasDe(a, fecha).length - this.fichasDe(b, fecha).length)
    let mejor: { medico: Especialista; fecha: string; hora: string } | null = null
    for (const medico of medicos) {
      const ocupadas = this.fichasDe(medico, fecha)
      for (let hora = desde; minutosDe(hora) + medico.duracionMin <= minutosDe(hasta);
        hora = sumarMinutos(hora, 5)) {
        const fin = minutosDe(hora) + medico.duracionMin
        const choca = ocupadas.some(f => minutosDe(f.hora!) < fin && minutosDe(hora) < minutosDe(f.hora!) + f.duracionMin)
        if (!choca) {
          if (!mejor || hora < mejor.hora) mejor = { medico, fecha, hora }
          break
        }
      }
    }
    return mejor
  }

  private fichasDe(medico: Especialista, fecha: string) {
    return this.fichas.filter(f => f.medico === medico.nombre && f.fecha === fecha && f.estado !== 'CANCELADA')
  }
}

/** Lee los datos de prueba guardados hoy (si hay). */
export function leerGuardado<T>(clave: string | undefined, hoy: string): T | null {
  if (!clave) return null
  try {
    const texto = localStorage.getItem(clave)
    const guardado = texto ? JSON.parse(texto) as { dia: string; datos: T } : null
    return guardado?.dia === hoy ? guardado.datos : null
  } catch {
    return null
  }
}

export function guardarDatos(clave: string | undefined, hoy: string, datos: unknown) {
  if (!clave) return
  try {
    localStorage.setItem(clave, JSON.stringify({ dia: hoy, datos }))
  } catch {
    // sin almacenamiento: los datos de prueba duran hasta recargar
  }
}

/** Datos del Figma, con fechas relativas a hoy. */
function semilla(hoy: string): FichaInterna[] {
  const manana = sumarDias(hoy, 1, true)
  const base = (f: Partial<FichaInterna> & Pick<FichaInterna, 'ticket' | 'pacienteId' | 'nombrePaciente'>): FichaInterna => ({
    citaOrigenId: null, numeroAfiliado: '', edad: 40, especialidad: 'GASTROENTEROLOGIA', tabla: 'GENERAL',
    urgencia: false, gravedad: 'MEDIA', motivo: '', estado: 'ASIGNADA', fecha: hoy, hora: '08:00', duracionMin: 25,
    medico: 'Raúl Vargas', consultorio: '12', derivadoPor: 'Carlos Ramos', especialidadOrigen: 'CONSULTORIO_FAMILIAR',
    llegada: `${hoy}T07:00:00.000Z`, ...f,
  })
  return [
    // Derivacion de la agenda del Dr. Ramos: Ana Mamani (ficha 2, 08:45) -> Cardiologia, Ticket N° 1021.
    base({ ticket: 1021, citaOrigenId: 502, pacienteId: 101, nombrePaciente: 'Ana Mamani', numeroAfiliado: '2290-15',
      edad: 41, especialidad: 'CARDIOLOGIA', gravedad: 'BAJA', motivo: 'Palpitaciones al esfuerzo, control de ritmo.',
      fecha: sumarDias(hoy, 2, true), hora: '09:00', duracionMin: 30, medico: 'Miguel Paredes', consultorio: '15',
      llegada: `${hoy}T12:55:00.000Z` }),
    // Agenda de fichas internas del Dr. Vargas (hoy).
    base({ ticket: 1019, pacienteId: 110, nombrePaciente: 'Pedro Huanca', numeroAfiliado: '6120-02', edad: 63,
      gravedad: 'ALTA', motivo: 'Hemorragia digestiva alta', estado: 'ATENDIDA', hora: '08:00' }),
    base({ ticket: 1020, pacienteId: 111, nombrePaciente: 'Elena Rojas', numeroAfiliado: '4410-05', edad: 52,
      gravedad: 'ALTA', motivo: 'Dolor epigástrico recurrente', estado: 'ATENDIDA', hora: '08:30' }),
    base({ ticket: 1022, pacienteId: 112, nombrePaciente: 'Carmen Ticona', numeroAfiliado: '3871-11', edad: 66,
      motivo: 'Control de gastritis crónica', estado: 'CANCELADA', hora: '09:00' }),
    base({ ticket: 1024, pacienteId: 113, nombrePaciente: 'Marco Ríos', numeroAfiliado: '4587-12', edad: 24,
      motivo: 'Dolor abdominal persistente hace 3 semanas, sospecha de úlcera.', hora: '10:20',
      llegada: `${hoy}T13:52:00.000Z` }),
    base({ ticket: 1026, pacienteId: 114, nombrePaciente: 'Sofía Vargas', numeroAfiliado: '5530-04', edad: 6,
      tabla: 'PEDIATRIA', motivo: 'Reflujo en niña de 6 años', hora: '11:00', derivadoPor: 'Lucía Quispe',
      especialidadOrigen: 'PEDIATRIA' }),
    base({ ticket: 1031, pacienteId: 115, nombrePaciente: 'Juan Mamani', numeroAfiliado: '6702-09', edad: 52,
      urgencia: true, gravedad: 'ALTA', motivo: 'Sangrado digestivo (urgencia de hoy)', hora: '12:30' }),
    // Cola de Gastroenterologia (tabla general).
    base({ ticket: 1032, pacienteId: 104, nombrePaciente: 'Rosa Choque', numeroAfiliado: '7781-03', edad: 68,
      urgencia: true, gravedad: 'ALTA', motivo: 'Vómitos con sangre', hora: '14:20', llegada: `${hoy}T17:05:00.000Z` }),
    base({ ticket: 1027, pacienteId: 116, nombrePaciente: 'Luis Paz', numeroAfiliado: '2981-06', edad: 66,
      gravedad: 'ALTA', motivo: 'Disfagia progresiva', fecha: manana, hora: '08:00', llegada: '2026-11-11T13:10:00.000Z' }),
    base({ ticket: 1028, pacienteId: 117, nombrePaciente: 'Ana Rojas', numeroAfiliado: '6011-02', edad: 45,
      gravedad: 'ALTA', motivo: 'Anemia por pérdida digestiva', fecha: manana, hora: '08:30', medico: 'Patricia Arce',
      consultorio: '14', llegada: '2026-11-10T19:40:00.000Z' }),
    base({ ticket: 1029, pacienteId: 118, nombrePaciente: 'Teresa Ticona', numeroAfiliado: '1873-10', edad: 71,
      motivo: 'Dolor abdominal crónico', fecha: manana, hora: '09:00', medico: 'Patricia Arce', consultorio: '14',
      llegada: '2026-11-12T12:20:00.000Z' }),
    base({ ticket: 1030, pacienteId: 119, nombrePaciente: 'Jorge Limachi', numeroAfiliado: '4402-01', edad: 62,
      gravedad: 'BAJA', motivo: 'Control de colon irritable', fecha: sumarDias(hoy, 5, true), hora: '11:00',
      medico: 'Patricia Arce', consultorio: '14', llegada: '2026-11-09T14:30:00.000Z' }),
    base({ ticket: 1025, pacienteId: 120, nombrePaciente: 'Mario Cruz', numeroAfiliado: '3310-07', edad: 40,
      gravedad: 'BAJA', motivo: 'Control de hígado graso', estado: 'EN_ESPERA', fecha: null, hora: null, medico: null,
      consultorio: null, llegada: '2026-11-09T15:00:00.000Z' }),
  ]
}
