/** Compara nombres sin importar tildes ni mayusculas («Raul Vargas» = «Raúl Vargas»). */
export function mismoNombre(a: string | null | undefined, b: string | null | undefined): boolean {
  const normal = (s: string) => s.normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase().trim()
  return !!a && !!b && normal(a) === normal(b)
}
