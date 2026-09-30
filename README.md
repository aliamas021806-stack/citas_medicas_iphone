# Sistema de Citas Médicas — Código completo

Aplicación de gestión de citas médicas en **tres plataformas**, fusionando la lógica
de negocio con un sistema de diseño cuidado. **Backend en Firebase Firestore** (ya
sembrado con datos reales).

| Plataforma | Carpeta | Tecnología |
|---|---|---|
| 📱 **Android** | [`android/`](android/) | Java · MVVM · RecyclerView · Room · Firestore |
| 🍎 **iOS** | [`ios/`](ios/) | Swift · SwiftUI · MVVM |
| 🌐 **Demo web** | [`web-demo/`](web-demo/) | HTML · CSS · JavaScript (prototipo interactivo) |

---

## ✅ Estado funcional

- **Flujo de citas corregido**: `Doctor → Fecha → Horario → Confirmar`.
- **Firestore con datos reales** (proyecto `citas-medicas-d6b59`):
  - `specialties` 8 · `doctors` 10 · `slots` 540 · `appointments` 3 · `users` 1 → **562 documentos**.
- **`android/app/google-services.json`** incluido (paquete `com.tuempresa.citasmedicas`).
- **Login**: Firebase Auth si está habilitado; si no, **fallback automático** a login
  simulado (la demo nunca se bloquea; Firestore sí lee/escribe).

### Datos y esquema (NoSQL desnormalizado)
```
specialties/{id}    name, iconEmoji, description, doctorCount
doctors/{id}        fullName, specialtyId, specialtyName*, bio, rating,
                    yearsExperience, age, consultationFee, hospital
slots/{id}          doctorId, date, time, available
appointments/{id}   userId, doctorId, doctorName, specialtyName, patientName,
                    patientAge, date, time, status, reason, createdAt
users/{id}          fullName, email, phone, age
metadata/seed       version, ... (bandera de siembra idempotente)
```
`* specialtyName` se duplica a propósito en `doctors` para pintar la lista sin leer
`specialties` (menos lecturas → menos coste).

---

## 💸 Optimización de costos (cuenta demo/gratuita)

| Técnica | Dónde |
|---|---|
| `limit(PAGE_SIZE)` en todas las consultas | Repositorios Firestore |
| `get()` en lugar de `addSnapshotListener` | Toda la capa Firestore |
| Filtro por 1 igualdad + **orden en memoria** (sin índices compuestos) | Repositorios Firestore |
| Caché offline de Firestore (memoria + disco) | `FirebaseManager` |
| Arquitectura **local-first** (Room) | `AppointmentRepository` |
| Cancelar = **1 `update()`** de un campo | `FirestoreAppointmentRepository` |
| Siembra **idempotente** (1 lectura + 1 batch) | `FirestoreSeeder`, `docs/firebase/scripts` |

---

## 🚀 Cómo ejecutar

### Android (Java)
1. Abre `android/` con **Android Studio**.
2. `File → Open` la carpeta `android` y pulsa **Run ▶** (emulador API 24+).
3. Login: cualquier email válido + contraseña de 4+ caracteres.
4. Flujo: **Doctor → Fecha → Horario → Confirmar cita**.

Compilar por línea de comandos:
```bash
cd android
export ANDROID_HOME=/ruta/a/tu/Android/Sdk
./gradlew :app:assembleRelease
# APK: app/build/outputs/apk/release/app-release.apk
```

### iOS (Swift/SwiftUI) — requiere Mac + Xcode
1. Abre `ios/` en **Xcode** (o genera el proyecto con XcodeGen si usas `project.yml`).
2. Selecciona un simulador de iPhone y pulsa **Run ▶**.

### Demo web
Abre `web-demo/index.html` en el navegador (o sírvelo con
`python3 -m http.server 5060 --directory web-demo`).

---

## 🔥 Firebase

- **Configuración de app**: `android/app/google-services.json` (ya incluido).
- **Guiar el aprovisionamiento**: ver [`docs/FIREBASE_SETUP.md`](docs/FIREBASE_SETUP.md).
- **Scripts de siembra** (Admin SDK, idempotentes):
  ```bash


