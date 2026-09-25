# CitasMedicas — App iOS nativa (Swift + SwiftUI)

Migración nativa a **iOS** del proyecto Android *CitasMedicas*, fusionando la
**lógica de negocio / MVVM / Mock Data** (primer ZIP) con la **línea visual
"Clinical Soft Light"** (segundo ZIP). 100% **Swift + SwiftUI**, sin backend.

> ## 🧪 ¿No tienes Mac ni iPhone?
> Esta carpeta `web_preview/` contiene una **demo web interactiva** que replica
> exactamente esta app (mismo diseño, datos y flujos) y se abre en cualquier
> navegador. Sirve para *ver cómo se ve y se comporta* la app antes de abrirla
> en Xcode. No es el código nativo, es una maqueta funcional para clientes/QA.
> Ver la sección **"Probar sin Mac"** al final de este README.

---

## 1. Arquitectura

Patrón **MVVM** limpio con inyección de dependencias y `async/await`:

```
CitasMedicas/
├── App/
│   ├── CitasMedicasApp.swift      # @main + RootView (Login/Main según sesión)
│   └── AppEnvironment.swift       # Composition Root (repos + VMs compartidos)
├── DesignSystem/
│   ├── Theme.swift                # Colores, espaciado, radios, sombras, cardStyle
│   └── Typography.swift           # Escala tipográfica (Plus Jakarta Sans / Inter)
├── Models/                        # Dominio (Codable)
│   ├── User.swift  Patient.swift  Doctor.swift  Specialty.swift
│   └── TimeSlot.swift  Appointment.swift  AuthRequest.swift
├── Services/                      # Capa de datos y persistencia
│   ├── MockData.swift             # Datos sintéticos (especialidades, doctores, pacientes, slots)
│   ├── Resource.swift             # Estado loading/success/error
│   ├── SessionManager.swift       # Sesión persistida (UserDefaults)
│   ├── Persistence.swift          # AppointmentStore (JSON en Documents) — sustituible por CoreData
│   ├── AuthRepository.swift       # Login/Registro simulado (async, validaciones)
│   ├── DoctorRepository.swift     # Especialidades, doctores, slots
│   ├── PatientRepository.swift    # Pacientes (con EDAD)
│   └── AppointmentRepository.swift# CRUD local de citas + sembrado inicial
├── ViewModels/                    # MVVM (@MainActor, ObservableObject)
│   ├── AuthViewModel.swift  DoctorViewModel.swift
│   ├── AppointmentViewModel.swift  PatientViewModel.swift
└── Views/
    ├── Auth/LoginView.swift
    ├── Main/MainTabView.swift  HomeView.swift
    │         AppointmentListView.swift  ProfileView.swift
    ├── Doctors/DoctorDetailView.swift
    ├── Appointment/NewAppointmentView.swift  AppointmentDetailView.swift
    └── Components/ AvatarView, StatusBadge, PrimaryButton, SearchBar,
                    StateViews, FormField, AppointmentCard
```

**Flujo de datos:** `View → ViewModel → Repository → (MockData | Persistencia)`.
Las vistas nunca tocan repositorios: sólo observan los `@Published` del ViewModel.

---

## 2. Funcionalidades implementadas

| Función | Descripción |
|---|---|
| **Login / Registro** | Simulado con validación (email regex, contraseña ≥ 4, edad 1–120) y delay de 0.9 s. Persistido en `UserDefaults`. |
| **Home** | Saludo contextual, métricas de citas, búsqueda por médico/especialidad, filtros por especialidad y listado de doctores. |
| **Detalle del doctor** | Bio, rating, hospital, tarifa y **selector de fecha (7 días) + hora** con franjas disponibles/ocupadas. |
| **Nueva Cita** | Asistente de 3 pasos (Médico → Horario → Detalles) con barra de progreso, modalidad (Presencial/Telemedicina) y recordatorio. |
| **Mis Citas** | Ver, filtrar y **cancelar** citas; detalle con estado, doctor y datos del paciente. |
| **Perfil** | Datos del paciente con **EDAD** destacada, estadísticas y cierre de sesión. |

### Persistencia (sin backend)
- **Sesión:** `UserDefaults` (JSON del `User`).
- **Citas:** archivo `appointments.json` en el directorio de *Documents*.
  El protocolo `AppointmentStore` permite **cambiar a CoreData** sin tocar los
  ViewModels (basta crear `CoreDataAppointmentStore: AppointmentStore`).

---

## 3. Requisitos

- **Xcode 15+**, **iOS 16.0+**, **Swift 5.9+**
- (Opcional) [XcodeGen](https://github.com/yonaskolb/XcodeGen) para regenerar el `.xcodeproj`

---

## 4. Cómo abrir el proyecto

### Opción A — Con XcodeGen (recomendado)
```bash
brew install xcodegen
cd CitasMedicas
xcodegen generate          # genera CitasMedicas.xcodeproj
open CitasMedicas.xcodeproj
```

### Opción B — Proyecto Xcode manual
1. Xcode → *File → New → Project → iOS App* (nombre: `CitasMedicas`, interfaz **SwiftUI**).
2. Copia las carpetas `App/`, `DesignSystem/`, `Models/`, `Services/`,
   `ViewModels/`, `Views/` y `Assets.xcassets/` dentro del grupo del proyecto.
3. Añade los `Info.plist` y las fuentes (ver §5).
4. Build & Run (⌘R).

---

## 5. Fuentes (opcional pero recomendado)

El sistema respeta *Dynamic Type* y hace **fallback al font del sistema** si las
fuentes no están presentes. Para la fidelidad total del diseño, añade a
`Resources/Fonts/` y al target (y ya están declaradas en `Info.plist → UIAppFonts`):

- `PlusJakartaSans-Bold.ttf`
- `PlusJakartaSans-SemiBold.ttf`
- `Inter-Regular.ttf`
- `Inter-Medium.ttf`
- `Inter-SemiBold.ttf`

Descárgalas gratis en Google Fonts. **Nota:** las cifras de horas/fechas usan
`.monospacedDigit()` para evitar *jitter*, tal como pide el DESIGN.md.

---

## 6. Sistema de diseño "Clinical Soft Light"

| Token | Valor |
|---|---|
| Fondo | `#F8FAFC` |
| Superficie | `#FFFFFF` |
| Primario | `#2563EB` |
| Primario suave | `#E0F2FE` |
| Éxito / Confirmada | `#10B981` / fondo `#D1FAE5` |
| Pendiente | `#F59E0B` / fondo `#FEF3C7` |
| Cancelada | `#F43F5E` / fondo `#FFE4E6` |
| Texto titular / cuerpo / muted | `#0F172A` / `#475569` / `#94A3B8` |
| Radios | cards 16 · inputs 12 · pills full |
| Sombras | ambiente tintada azul `rgba(37,99,235,0.04)` |

---

## 7. Listo para producción

- ✅ Arquitectura escalable (repositorios intercambiables, VMs aislados).
- ✅ Manejo de estados de carga/éxito/error y **estados vacíos** en todas las listas.
- ✅ Validaciones de formulario y errores amigables.
- ✅ Diseño responsivo (SafeArea, ScrollView, `adaptive` grids, Dynamic Type).
- ✅ Sin dependencias externas (cero SPM/CocoaPods) → fácil de auditar y firmar.
- ✅ Localización en español (`es`) y orientación vertical para iPhone.
- ✅ Accesibilidad: colores por rol semántico, textos legibles, tap targets ≥ 44 pt.

### Checklist antes de subir a App Store
- [ ] Configurar `DEVELOPMENT_TEAM` y la firma automática en Xcode.
- [ ] Sustituir textos "demo" por el branding final si aplica.
- [ ] Añadir fuentes (Plus Jakarta Sans / Inter) o dejar el fallback.
- [ ] Ejecutar *Product → Archive* y subir con Xcode Cloud / Transporter.

---

## 🖥️ Probar el código SIN Mac ni iPhone

El proyecto nativo Swift/SwiftUI **necesita un Mac con Xcode** para compilarse
(no existe forma de ejecutar SwiftUI en Windows/Linux/Android). Por eso tienes
**3 caminos** según lo que quieras hacer:

### Opción 1 — Ver y usar la app ahora mismo (recomendado) 👈
Abre esta URL en el navegador de tu móvil o PC:

```
https://5060-ikcbx3nlcanc63znv0nwg-0e616f0a.sandbox.novita.ai/
```

Es una **demo interactiva 1:1** de la app: login, home, doctores, selección de
fecha/hora, alta y cancelación de citas, perfil… con los mismos datos y diseño.
Ideal para validar la UX. (Datos guardados en el navegador, se reinician al limpiar.)

### Opción 2 — Verificar que el código iOS COMPILA (sin Mac)
Usa **GitHub Actions**: un servidor macOS gratuito de GitHub compila el proyecto.
1. Crea un repositorio en GitHub y sube esta carpeta:
   ```bash
   cd CitasMedicas
   git init && git add . && git commit -m "iOS app"
   git branch -M main
   git remote add origin https://github.com/TU_USUARIO/citasmedicas-ios.git
   git push -u origin main
   ```
2. Ve a la pestaña **Actions** → verás el workflow **"iOS Build (verificación)"**
   compilando en `macos-14`. Un ✅ verde = el código Swift compila correctamente.

### Opción 3 — Ejecutarla de verdad en iPhone
Necesitas **un Mac** (o un Mac en la nube) con Xcode:
- **Mac compartido / alquilado**: [MacinCloud](https://www.macincloud.com),
  [MacStadium](https://www.macstadium.com).
- **Xcode Cloud**: subes el repo a GitHub y compilas en la nube de Apple
  (requiere cuenta de Apple Developer, 99 USD/año).
- Una vez en un Mac: `xcodegen generate && open CitasMedicas.xcodeproj` → ⌘R.

### Opción 4 — Probar la demo localmente (sin internet)
```bash
cd web_preview
python3 -m http.server 5060
# Abre http://localhost:5060
```

### Estructura de la demo web
```
web_preview/
├── index.html    # estructura (marco de teléfono)
├── styles.css    # Design System "Clinical Soft Light" portado a CSS
├── app.js        # lógica (espejo de MockData.swift + repositorios) y vistas
└── app_icon_192.png
```

---

## 📦 Entregables

| Recurso | Ruta |
|---|---|
| Proyecto iOS (Swift/SwiftUI) | `CitasMedicas/` (38 archivos Swift) |
| Demo web interactiva | `web_preview/` |
| Icono 1024×1024 | `CitasMedicas/CitasMedicas/Assets.xcassets/AppIcon.appiconset/` |
| Localización es/en | `CitasMedicas/CitasMedicas/Resources/*.lproj/` |
| CI (compila en macOS) | `.github/workflows/ios-build.yml` |
| Config Xcode | `project.yml` (XcodeGen) |
