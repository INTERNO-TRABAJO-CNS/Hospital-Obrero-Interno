# Actualizar y revisar ramas desde VS Code / terminal

El repositorio remoto generalmente se llama `origin`. Verifica con `git remote -v` antes de ejecutar los ejemplos. **No uses `git pull origin develop` desde una rama feature sin comprender si quieres fusionar o rebasar**; sincroniza explícitamente.

```bash
git status
git fetch origin
git switch develop
git pull --ff-only origin develop
```

Para crear una rama nueva (solo si no existe):

```bash
git switch develop
git switch -c feature/TXX-arquitectura-cns
git push -u origin feature/TXX-arquitectura-cns
```

Para actualizar tu rama existente respecto a `develop` mediante merge (con cambios limpios, o guardados mediante commit):

```bash
git status
git fetch origin
git switch feature/NOMBRE-EXISTENTE
git merge origin/develop
git push origin feature/NOMBRE-EXISTENTE
```

Si aparece conflicto, resolver archivos, `git add` y `git commit` para terminar merge. No usar `git push --force` por defecto. No actualizar 20 ramas indiscriminadamente: hacerlo en las activas y para los compañeros que lo necesiten. Las reglas de `main` / `develop` se mantienen.
