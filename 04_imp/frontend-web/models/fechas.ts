const DIAS = ['domingo', 'lunes', 'martes', 'miércoles', 'jueves', 'viernes', 'sábado']
const DIAS_CORTOS = ['Dom', 'Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb']
const MESES = ['enero', 'febrero', 'marzo', 'abril', 'mayo', 'junio', 'julio', 'agosto', 'septiembre', 'octubre',
  'noviembre', 'diciembre']
const MESES_CORTOS = ['ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'sep', 'oct', 'nov', 'dic']

/** Fecha local de hoy en formato YYYY-MM-DD. */
export function hoyISO(ahora: Date = new Date()): string {
  const m = String(ahora.getMonth() + 1).padStart(2, '0')
  const d = String(ahora.getDate()).padStart(2, '0')
  return `${ahora.getFullYear()}-${m}-${d}`
}

/** Hora local HH:mm. */
export function horaActual(ahora: Date = new Date()): string {
  return `${String(ahora.getHours()).padStart(2, '0')}:${String(ahora.getMinutes()).padStart(2, '0')}`
}

function partes(iso: string): [number, number, number] {
  const [a, m, d] = iso.slice(0, 10).split('-').map(Number)
  return [a, m, d]
}

/** "2026-11-12" -> "jueves 12 de noviembre de 2026" (sin depender de la zona horaria). */
export function fechaLarga(iso: string): string {
  const [a, m, d] = partes(iso)
  return `${DIAS[new Date(a, m - 1, d).getDay()]} ${d} de ${MESES[m - 1]} de ${a}`
}

/** "2026-11-12" -> "Jueves 12 de noviembre" (titulos de la agenda). */
export function fechaTitulo(iso: string): string {
  const [a, m, d] = partes(iso)
  const dia = DIAS[new Date(a, m - 1, d).getDay()]
  return `${dia.charAt(0).toUpperCase()}${dia.slice(1)} ${d} de ${MESES[m - 1]}`
}

/** "2026-11-12" -> "Jueves 12 de noviembre de 2026". */
export function fechaTituloAnio(iso: string): string {
  return `${fechaTitulo(iso)} de ${partes(iso)[0]}`
}

/** "2026-11-12" -> "Jueves 12 nov 2026" (encabezado de la atencion). */
export function fechaDiaMes(iso: string): string {
  const [a, m, d] = partes(iso)
  const dia = DIAS[new Date(a, m - 1, d).getDay()]
  return `${dia.charAt(0).toUpperCase()}${dia.slice(1)} ${d} ${MESES_CORTOS[m - 1]} ${a}`
}

/** "2026-11-12" -> "12/11/2026" */
export function fechaCorta(iso: string): string {
  const [a, m, d] = iso.slice(0, 10).split('-')
  return `${d}/${m}/${a}`
}

/** "2026-11-12" -> "12 nov 2026" (linea de tiempo del historial). */
export function fechaMes(iso: string, conAnio = true): string {
  const [a, m, d] = partes(iso)
  return conAnio ? `${d} ${MESES_CORTOS[m - 1]} ${a}` : `${d} ${MESES_CORTOS[m - 1]}`
}

/** "2026-11-12" -> "Jue 12/11/2026" (ticket impreso). */
export function fechaTicket(iso: string): string {
  const [a, m, d] = partes(iso)
  return `${DIAS_CORTOS[new Date(a, m - 1, d).getDay()]} ${fechaCorta(iso)}`
}

/** "08:40:00" -> "08:40" */
export function horaCorta(hora: string): string {
  return hora.slice(0, 5)
}

/** Suma minutos a una hora HH:mm (puede pasar de las 24:00 solo en urgencias; se corta en 23:59). */
export function sumarMinutos(hora: string, minutos: number): string {
  const [h, m] = hora.split(':').map(Number)
  const total = Math.min(h * 60 + m + minutos, 23 * 60 + 59)
  return `${String(Math.floor(total / 60)).padStart(2, '0')}:${String(total % 60).padStart(2, '0')}`
}

export function minutosDe(hora: string): number {
  const [h, m] = hora.split(':').map(Number)
  return h * 60 + m
}

/** Suma dias a una fecha YYYY-MM-DD; con {@code habiles} salta sabados y domingos. */
export function sumarDias(iso: string, dias: number, habiles = false): string {
  const [a, m, d] = partes(iso)
  const fecha = new Date(a, m - 1, d)
  let restantes = dias
  while (restantes > 0) {
    fecha.setDate(fecha.getDate() + 1)
    if (!habiles || (fecha.getDay() !== 0 && fecha.getDay() !== 6)) restantes--
  }
  return hoyISO(fecha)
}
