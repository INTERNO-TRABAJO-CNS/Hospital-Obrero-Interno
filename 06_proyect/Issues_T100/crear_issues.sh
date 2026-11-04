#!/usr/bin/env bash
# crear_issues.sh — crea en GitHub los issues T100-00 … T100-15 (Hospital Obrero CNS)
# Uso (desde la raíz del repositorio, con `gh auth login` hecho por QUIEN LO EJECUTA):
#   bash 06_proyect/Issues_T100/crear_issues.sh                 # crea los 16 (con confirmación)
#   bash 06_proyect/Issues_T100/crear_issues.sh Matías          # solo los de una persona
#   DRY=1 bash 06_proyect/Issues_T100/crear_issues.sh           # solo muestra qué haría
# Requiere: gh (GitHub CLI) con permiso de escritura en el repositorio.
set -u
REPO="INTERNO-TRABAJO-CNS/Hospital-Obrero-Interno"
DIR="$(cd "$(dirname "$0")" && pwd)"
FILTRO="${1:-}"
DRY="${DRY:-0}"

# Usuarios de GitHub (completar los que faltan; si queda vacío, el issue se crea sin asignar)
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

echo "== 1) Etiquetas =="
for l in "fase:construccion|1D76DB" "tipo:tarea|0E8A16" "rama:T100|5319E7" "tec:java|B60205" "tec:flutter|02569B"; do
  run gh label create "${l%%|*}" --color "${l##*|}" --repo "$REPO" --force
done

echo "== 2) Milestones (se crean o se corrigen las fechas) =="
asegurar_milestone() { # titulo  vence(YYYY-MM-DD)  descripcion
  local num
  num=$(gh api "repos/$REPO/milestones?state=all&per_page=100" --jq ".[] | select(.title==\"$1\") | .number" 2>/dev/null | head -1)
  if [ -n "$num" ]; then
    run gh api -X PATCH "repos/$REPO/milestones/$num" -f due_on="$2T23:59:59Z" -f description="$3" >/dev/null
    echo "  actualizado: $1 ($2)"
  else
    run gh api -X POST "repos/$REPO/milestones" -f title="$1" -f due_on="$2T23:59:59Z" -f description="$3" >/dev/null
    echo "  creado: $1 ($2)"
  fi
}
asegurar_milestone "Sprint 4 - Gestion de citas" 2026-11-12 "Inicio: 2026-11-06. Rama T100, catálogos, login y pacientes, agenda médica, historial del paciente."
asegurar_milestone "Sprint 5 - Integracion y seguimiento" 2026-11-20 "Inicio: 2026-11-13. Validación y asignación, solicitud, listado y TV, confirmación, cancelación, receta, notificaciones y aviso T-45."
asegurar_milestone "Sprint 6 - Validacion y entrega" 2026-11-28 "Inicio: 2026-11-21. Reportes y supervisión, reprogramar, historial de cambios, integración, pruebas finales y entrega."

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
00|T100-00_base-contratos-flutter.md|Benjamín|Sprint 4 - Gestion de citas|fase:construccion,tipo:tarea,rama:T100,tec:java,tec:flutter,caso-uso:transversal
01|T100-01_cu02-catalogos-crud.md|Benjamín|Sprint 4 - Gestion de citas|fase:construccion,tipo:tarea,rama:T100,tec:java,tec:flutter,caso-uso:CU-02
02|T100-02_cu06-validar-disponibilidad.md|Benjamín|Sprint 5 - Integracion y seguimiento|fase:construccion,tipo:tarea,rama:T100,tec:java,caso-uso:CU-06
03|T100-03_cu05-cu07-asignacion-registro.md|Benjamín|Sprint 5 - Integracion y seguimiento|fase:construccion,tipo:tarea,rama:T100,tec:java,caso-uso:CU-05+CU-07
04|T100-04_cu19-cu20-reportes-supervision.md|Benjamín|Sprint 6 - Validacion y entrega|fase:construccion,tipo:tarea,rama:T100,tec:java,tec:flutter,caso-uso:CU-19+CU-20
05|T100-05_cu01-cu03-login-pacientes.md|Matías|Sprint 4 - Gestion de citas|fase:construccion,tipo:tarea,rama:T100,tec:java,tec:flutter,caso-uso:CU-01+CU-03
06|T100-06_cu04-solicitar-cita.md|Matías|Sprint 5 - Integracion y seguimiento|fase:construccion,tipo:tarea,rama:T100,tec:java,tec:flutter,caso-uso:CU-04
07|T100-07_cu08-cu10-mis-citas-reprogramar.md|Matías|Sprint 6 - Validacion y entrega|fase:construccion,tipo:tarea,rama:T100,tec:java,tec:flutter,caso-uso:CU-08+CU-10
08|T100-08_cu13-agenda-medico.md|Alejandro|Sprint 4 - Gestion de citas|fase:construccion,tipo:tarea,rama:T100,tec:java,tec:flutter,caso-uso:CU-13
09|T100-09_cu09-fichas-del-dia-tv.md|Alejandro|Sprint 5 - Integracion y seguimiento|fase:construccion,tipo:tarea,rama:T100,tec:java,tec:flutter,caso-uso:CU-09
10|T100-10_cu12-confirmar-asistencia-fin.md|Alejandro|Sprint 5 - Integracion y seguimiento|fase:construccion,tipo:tarea,rama:T100,tec:java,tec:flutter,caso-uso:CU-12
11|T100-11_cu11-cancelar-cita-auto.md|Alex|Sprint 5 - Integracion y seguimiento|fase:construccion,tipo:tarea,rama:T100,tec:java,tec:flutter,caso-uso:CU-11
12|T100-12_cu14-historial-paciente.md|Alex|Sprint 4 - Gestion de citas|fase:construccion,tipo:tarea,rama:T100,tec:java,tec:flutter,caso-uso:CU-14
13|T100-13_cu18-historial-cambios.md|Alex|Sprint 6 - Validacion y entrega|fase:construccion,tipo:tarea,rama:T100,tec:java,tec:flutter,caso-uso:CU-18
14|T100-14_cu15-registrar-atencion.md|Abigail|Sprint 4 - Gestion de citas|fase:construccion,tipo:tarea,rama:T100,tec:java,tec:flutter,caso-uso:CU-15
15|T100-15_cu16-cu17-notificaciones-aviso.md|Abigail|Sprint 5 - Integracion y seguimiento|fase:construccion,tipo:tarea,rama:T100,tec:java,tec:flutter,caso-uso:CU-16+CU-17
LISTA
echo "Listo. Revisa https://github.com/$REPO/issues"
