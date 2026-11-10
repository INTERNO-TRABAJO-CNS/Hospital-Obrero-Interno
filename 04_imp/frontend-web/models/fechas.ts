const DIAS = ['domingo', 'lunes', 'martes', 'miércoles', 'jueves', 'viernes', 'sábado']
const MESES = ['enero', 'febrero', 'marzo', 'abril', 'mayo', 'junio', 'julio', 'agosto', 'septiembre', 'octubre',
  'noviembre', 'diciembre']

/** Fecha local de hoy en formato YYYY-MM-DD. */
export function hoyISO(ahora: Date = new Date()): string {
  const m = String(ahora.getMonth() + 1).padStart(2, '0')
  const d = String(ahora.getDate()).padStart(2, '0')
  return `${ahora.getFullYear()}-${m}-${d}`
}

/** "2026-11-10" -> "martes 10 de noviembre de 2026" (sin depender de la zona horaria). */
export function fechaLarga(iso: string): string {
  const [a, m, d] = iso.split('-').map(Number)
  const fecha = new Date(a, m - 1, d)
  return `${DIAS[fecha.getDay()]} ${d} de ${MESES[m - 1]} de ${a}`
}

/** "2026-11-10" -> "10/11/2026" */
export function fechaCorta(iso: string): string {
  const [a, m, d] = iso.slice(0, 10).split('-')
  return `${d}/${m}/${a}`
}

/** "08:40:00" -> "08:40" */
export function horaCorta(hora: string): string {
  return hora.slice(0, 5)
}
