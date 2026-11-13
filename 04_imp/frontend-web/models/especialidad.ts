// Debe coincidir con TipoEspecialidad del backend (3 basicas + 10 internas, decision del 10/11/2026).
export type Especialidad =
  | 'CONSULTORIO_FAMILIAR'
  | 'ODONTOLOGIA'
  | 'PEDIATRIA'
  | 'CARDIOLOGIA'
  | 'NEFROLOGIA'
  | 'NEUMOLOGIA'
  | 'ENDOCRINOLOGIA'
  | 'GASTROENTEROLOGIA'
  | 'HEMATOLOGIA'
  | 'INFECTOLOGIA'
  | 'DERMATOLOGIA'
  | 'MEDICINA_TRABAJO'
  | 'TERAPIA_INTENSIVA'

export type CategoriaEspecialidad = 'BASICA' | 'INTERNA'

/** Nombre de la especialidad tal como aparece en el Figma (catalogo, consultas, historial). */
export const NOMBRE_ESPECIALIDAD: Record<Especialidad, string> = {
  CONSULTORIO_FAMILIAR: 'Consultorio Familiar',
  ODONTOLOGIA: 'Odontología',
  PEDIATRIA: 'Pediatría',
  CARDIOLOGIA: 'Cardiología',
  NEFROLOGIA: 'Nefrología',
  NEUMOLOGIA: 'Neumología',
  ENDOCRINOLOGIA: 'Endocrinología',
  GASTROENTEROLOGIA: 'Gastroenterología',
  HEMATOLOGIA: 'Hematología',
  INFECTOLOGIA: 'Infectología',
  DERMATOLOGIA: 'Dermatología',
  MEDICINA_TRABAJO: 'Medicina del Trabajo',
  TERAPIA_INTENSIVA: 'Terapia Intensiva',
}

/** Duracion de ficha (minutos) de cada especialidad interna; igual que TipoEspecialidad. */
export const DURACION_INTERNA: Partial<Record<Especialidad, [number, number]>> = {
  CARDIOLOGIA: [20, 40],
  NEFROLOGIA: [20, 40],
  NEUMOLOGIA: [20, 30],
  ENDOCRINOLOGIA: [20, 40],
  GASTROENTEROLOGIA: [20, 30],
  HEMATOLOGIA: [20, 40],
  INFECTOLOGIA: [30, 45],
  DERMATOLOGIA: [15, 30],
  MEDICINA_TRABAJO: [20, 40],
  TERAPIA_INTENSIVA: [30, 60],
}

/** Rango de duracion de cirugia (minutos) de las especialidades que operan. */
export const DURACION_CIRUGIA: Partial<Record<Especialidad, [number, number]>> = {
  GASTROENTEROLOGIA: [30, 120],
  HEMATOLOGIA: [30, 120],
  DERMATOLOGIA: [15, 120],
}

export const BASICAS: readonly Especialidad[] = ['CONSULTORIO_FAMILIAR', 'ODONTOLOGIA', 'PEDIATRIA']
export const INTERNAS = Object.keys(DURACION_INTERNA) as Especialidad[]

export function nombreEspecialidad(codigo: string | null | undefined): string {
  if (!codigo) return ''
  return NOMBRE_ESPECIALIDAD[codigo as Especialidad] ?? codigo
}

export function esBasica(e: Especialidad): boolean {
  return BASICAS.includes(e)
}

/** Terapia Intensiva: solo urgencias (reanimacion), sin agenda propia. */
export function soloUrgencia(e: Especialidad): boolean {
  return e === 'TERAPIA_INTENSIVA'
}

/** Como se presenta el medico en la barra superior: «Médico General», «Pediatría» u «Odontología». */
export function tipoDeMedico(e: Especialidad): string {
  if (e === 'CONSULTORIO_FAMILIAR') return 'Médico General'
  return esBasica(e) ? nombreEspecialidad(e) : 'Especialista'
}
