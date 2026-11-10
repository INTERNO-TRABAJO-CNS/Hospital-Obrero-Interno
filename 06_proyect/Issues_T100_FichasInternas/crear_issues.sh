#!/usr/bin/env bash
# crear_issues.sh — crea en GitHub los issues T100-16 … T100-32 (fichas internas, quirófanos y ajustes)
# Uso (desde la raíz del repositorio, con `gh auth login` hecho por QUIEN LO EJECUTA):
#   bash 06_proyect/Issues_T100_FichasInternas/crear_issues.sh            # crea los 17 (omite los que ya existen)
#   bash 06_proyect/Issues_T100_FichasInternas/crear_issues.sh Matías     # solo los de una persona
#   DRY=1 bash 06_proyect/Issues_T100_FichasInternas/crear_issues.sh      # solo muestra qué haría
# Requiere: gh (GitHub CLI) con permiso de escritura en el repositorio.
set -u
REPO="INTERNO-TRABAJO-CNS/Hospital-Obrero-Interno"
DIR="$(cd "$(dirname "$0")" && pwd)"
FILTRO="${1:-}"
DRY="${DRY:-0}"

# Usuarios de GitHub (los mismos de Issues_T100/crear_issues.sh; si queda vacío, el issue se crea sin asignar)
USR_BENJAMIN=""            # <- tu usuario de GitHub
USR_MATIAS="mtiasgarcia1712"
USR_ALEJANDRO="Alejandro5989"
USR_ALEX=""                # <- usuario de Alex
USR_ABIGAIL="AbigailFarfan"

usuario_de() {
  case "$1" in
    "Benjamín") echo "$USR_BENJAMIN";; "Matías") echo "$USR_MATIAS";; "Alejandro") echo "$USR_ALEJANDRO";;
    "Alex") echo "$USR_ALEX";; "Abigail") echo "$USR_ABIGAIL";; *) echo "";;
  esac
}
run() { if [ "$DRY" = "1" ]; then echo "[DRY] $*"; else "$@"; fi; }

command -v gh >/dev/null || { echo "Falta gh (https://cli.github.com)"; exit 1; }
[ "$DRY" = "1" ] || gh auth status >/dev/null 2>&1 || { echo "Ejecuta primero: gh auth login"; exit 1; }

echo "== 1) Etiquetas nuevas =="
for l in "tec:nuxt|41B883" "tec:postgresql|336791" "tec:websocket|FBCA04" "tipo:ajuste|D93F0B" "tipo:prueba|0052CC"          "tipo:documentacion|C2E0C6" "modulo:fichas-internas|E99695" "modulo:quirofano|B60205"          "modelo-v:requisitos|5319E7" "modelo-v:integracion|5319E7" "modelo-v:aceptacion|5319E7"; do
  run gh label create "${l%%|*}" --color "${l##*|}" --repo "$REPO" --force
done

echo "== 2) Milestones (solo se verifica que existan; NO se cambian sus fechas) =="
for m in "Sprint 4 - Gestion de citas" "Sprint 5 - Integracion y seguimiento" "Sprint 6 - Validacion y entrega"; do
  if [ "$DRY" != "1" ] && ! gh api "repos/$REPO/milestones?state=all&per_page=100" --jq ".[].title" | grep -qx "$m"; then
    echo "  FALTA el milestone «$m»: créalo primero con 06_proyect/Issues_T100/crear_issues.sh"; exit 1
  fi
  echo "  ok: $m"
done

echo "== 3) Issues =="
while IFS='|' read -r ID ARCHIVO DUENO MILESTONE ETIQUETAS; do
  [ -z "$ID" ] && continue
  [ -n "$FILTRO" ] && [ "$FILTRO" != "$DUENO" ] && continue
  TITULO="$(head -n 1 "$DIR/$ARCHIVO" | sed 's/^# //')"
  # evitar duplicados
  if [ "$DRY" != "1" ] && gh issue list --repo "$REPO" --state all --search "\"[T100-$ID]\" in:title" --json number --jq '.[0].number' 2>/dev/null | grep -q .; then
    echo "  ya existe: T100-$ID (se omite)"; continue
  fi
  CUERPO="$(mktemp)"; tail -n +2 "$DIR/$ARCHIVO" > "$CUERPO"
  ARGS=(--repo "$REPO" --title "$TITULO" --body-file "$CUERPO" --milestone "$MILESTONE")
  IFS=',' read -ra ETQ <<< "$ETIQUETAS"
  for e in "${ETQ[@]}"; do run gh label create "$e" --color C5DEF5 --repo "$REPO" 2>/dev/null; ARGS+=(--label "$e"); done
  U="$(usuario_de "$DUENO")"; [ -n "$U" ] && ARGS+=(--assignee "$U")
  echo "  T100-$ID  ($DUENO)  $MILESTONE"
  run gh issue create "${ARGS[@]}"
  rm -f "$CUERPO"
done <<'LISTA'
16|T100-16_fichas-internas-modelo-v.md|Benjamín|Sprint 4 - Gestion de citas|fase:construccion,tipo:documentacion,modelo-v:requisitos,modulo:fichas-internas,modulo:quirofano
17|T100-17_contratos-fichas-quirofano.md|Benjamín|Sprint 4 - Gestion de citas|fase:construccion,tipo:tarea,rama:T100,tec:java,tec:postgresql,modulo:fichas-internas,modulo:quirofano
18|T100-18_base-nuxt-web-medico.md|Benjamín|Sprint 4 - Gestion de citas|fase:construccion,tipo:tarea,rama:T100,tec:nuxt,tec:java
19|T100-19_cu02-especialistas-quirofanos.md|Benjamín|Sprint 5 - Integracion y seguimiento|fase:construccion,tipo:tarea,rama:T100,tec:java,tec:flutter,caso-uso:CU-02,modulo:quirofano
20|T100-20_cu21-cu22-ficha-interna-prioridad.md|Benjamín|Sprint 5 - Integracion y seguimiento|fase:construccion,tipo:tarea,rama:T100,tec:java,caso-uso:CU-21+CU-22,modulo:fichas-internas
21|T100-21_cu21-cu25-web-ticket-interno-cola.md|Benjamín|Sprint 5 - Integracion y seguimiento|fase:construccion,tipo:tarea,rama:T100,tec:nuxt,tec:java,caso-uso:CU-21+CU-25,modulo:fichas-internas
22|T100-22_cu23-agenda-especialista.md|Benjamín|Sprint 5 - Integracion y seguimiento|fase:construccion,tipo:tarea,rama:T100,tec:java,tec:nuxt,caso-uso:CU-23,modulo:fichas-internas
23|T100-23_cu24-programar-cirugia-quirofano.md|Benjamín|Sprint 5 - Integracion y seguimiento|fase:construccion,tipo:tarea,rama:T100,tec:java,tec:nuxt,caso-uso:CU-24,modulo:quirofano
24|T100-24_ajuste-cu05-cu06-edad-cruces.md|Benjamín|Sprint 5 - Integracion y seguimiento|fase:construccion,tipo:ajuste,rama:T100,tec:java,tec:postgresql,caso-uso:CU-05+CU-06
25|T100-25_ajuste-cu19-cu20-supervision-fichas-quirofanos.md|Benjamín|Sprint 5 - Integracion y seguimiento|fase:construccion,tipo:ajuste,rama:T100,tec:java,tec:flutter,tec:websocket,caso-uso:CU-19+CU-20,modulo:fichas-internas,modulo:quirofano
26|T100-26_ajuste-cu03-cu04-edad-pediatria.md|Matías|Sprint 5 - Integracion y seguimiento|fase:construccion,tipo:ajuste,rama:T100,tec:java,tec:flutter,caso-uso:CU-03+CU-04
27|T100-27_ajuste-cu13-agenda-medico-nuxt.md|Alejandro|Sprint 5 - Integracion y seguimiento|fase:construccion,tipo:ajuste,rama:T100,tec:nuxt,caso-uso:CU-13
28|T100-28_ajuste-cu14-historial-nuxt.md|Alex|Sprint 5 - Integracion y seguimiento|fase:construccion,tipo:ajuste,rama:T100,tec:java,tec:nuxt,caso-uso:CU-14
29|T100-29_ajuste-cu15-atencion-nuxt.md|Abigail|Sprint 5 - Integracion y seguimiento|fase:construccion,tipo:ajuste,rama:T100,tec:nuxt,caso-uso:CU-15
30|T100-30_ajuste-cu16-notificacion-ticket-impreso.md|Abigail|Sprint 5 - Integracion y seguimiento|fase:construccion,tipo:ajuste,rama:T100,tec:java,tec:flutter,tec:nuxt,caso-uso:CU-16
31|T100-31_pruebas-integracion-concurrencia.md|Benjamín|Sprint 6 - Validacion y entrega|fase:construccion,tipo:prueba,rama:T100,tec:java,tec:postgresql,modelo-v:integracion
32|T100-32_pruebas-aceptacion-sistema.md|Benjamín|Sprint 6 - Validacion y entrega|fase:construccion,tipo:prueba,modelo-v:aceptacion
LISTA
echo "Listo. Revisa https://github.com/$REPO/issues y agrega los issues al tablero (Project → Board)."
