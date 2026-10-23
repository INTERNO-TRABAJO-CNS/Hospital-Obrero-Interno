# Comandos Git — CNS

```bash
git status                        # Qué cambió
git branch -a                     # Ver ramas locales y remotas
git fetch origin                  # Traer referencias remotas
git switch develop                # Cambiar a develop
git pull --ff-only origin develop # Actualizar develop sin merges accidentales
git switch -c feature/TXX-cambio  # Nueva rama desde rama actual
git add ruta/del/archivo.md       # Preparar solo cambios deseados
git commit -m "docs(arq): documentar dominio" -m "Refs #N"
git push -u origin feature/TXX-cambio
```

**`Refs #N`** enlaza informalmente el avance. Usa número real de Issue. Un PR a `develop` no garantiza el cierre automático con `Closes #N` si la rama por defecto es `main`: cierra el issue manualmente tras verificar el merge.

La automatización `gh` y los scripts de sincronización quedan **pospuestos**; la gestión de Milestones e Issues de este paquete es manual en la interfaz de GitHub.
