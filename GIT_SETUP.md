# Git y GitHub: pasos

## Pablo (una vez)
1. Crear en GitHub un repositorio **privado** (por ejemplo `hce-saas`). No marcar "Add README" si vas a subir el kit.
2. En la carpeta del proyecto: `git init`, `git add .`, `git commit -m "Base del proyecto"`, `git branch -M main`, `git remote add origin <URL>`, `git push -u origin main`.
3. Verificar que `.gitignore` incluye `target/`, `data/`, `.idea/`, `*.log`, `*.mv.db` y `*.trace.db`.
4. Repositorio → Settings → Collaborators → invitar a Alexander y Mattias por su usuario de GitHub.
5. Si el plan lo permite: Settings → Branches → proteger `main` (exigir pull request). Si no, regla del grupo: nadie hace push directo a `main`.

## Alexander y Mattias (una vez)
1. Instalar **Git for Windows** (opciones por defecto). Mattias, que no lo tiene: es la primera tarea.
2. Aceptar la invitación al repositorio (llega por correo o en GitHub).
3. `git config --global user.name "Tu Nombre"` y `git config --global user.email "tu-correo"`.
4. `git clone <URL>` y abrir la carpeta en IntelliJ (el panel Git de IntelliJ sirve si no quieren usar comandos).

## Cada tarea
1. `git checkout main` y `git pull`.
2. `git checkout -b feat/nombre-de-la-tarea`.
3. Trabajar, probar con `.\mvnw.cmd test`.
4. `git add .`, `git commit -m "CU-xx: lo que hiciste"`, `git push -u origin feat/nombre-de-la-tarea`.
5. En GitHub: abrir Pull Request hacia `main`, pegar la ficha y la captura de los tests. Pablo revisa y hace el merge.

## Si algo sale mal
- Conflicto al hacer pull: no borres nada; avisa a Pablo con la captura.
- Subiste algo por error (clave, datos): avisa de inmediato, no lo "arregles" solo.
