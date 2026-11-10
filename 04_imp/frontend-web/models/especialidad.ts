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

export const NOMBRE_ESPECIALIDAD: Record<Especialidad, string> = {
  CONSULTORIO_FAMILIAR: 'Médico General',
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

export const BASICAS: readonly Especialidad[] = ['CONSULTORIO_FAMILIAR', 'ODONTOLOGIA', 'PEDIATRIA']

export function nombreEspecialidad(codigo: string | null | undefined): string {
  if (!codigo) return ''
  return NOMBRE_ESPECIALIDAD[codigo as Especialidad] ?? codigo
}

export function esBasica(e: Especialidad): boolean {
  return BASICAS.includes(e)
}
