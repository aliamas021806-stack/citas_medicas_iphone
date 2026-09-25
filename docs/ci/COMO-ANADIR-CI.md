# Workflows de CI (GitHub Actions)

Estos archivos NO pudieron subirse automáticamente porque una GitHub App no
tiene permiso para crear/actualizar `workflows` (requiere el scope `workflows`).

Para añadirlos tú (30 segundos, por la web):
1. En GitHub, crea la carpeta `.github/workflows/` en la rama `main`.
2. Crea el archivo `android-ci.yml` y pega el contenido de `android-ci.yml`.
   (Igual para `ios-ci.yml` en su repositorio iOS.)
3. Commit. GitHub Actions se activará en el siguiente push.

Archivos incluidos:
- `android-ci.yml` -> compila APK + AAB de release.
- `ios-ci.yml`     -> compila el proyecto iOS en un runner macOS.
