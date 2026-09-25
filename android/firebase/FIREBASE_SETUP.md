# Guía de Firebase y datos sintéticos — Sistema de Citas Médicas

> ## ✅ ESTADO ACTUAL (ya configurado)
> - **Proyecto Firebase:** `citas-medicas-d6b59` (restaurado, estado ACTIVE).
> - **Base de datos Firestore:** creada (edición Standard, `europe-west1`).
> - **Datos sembrados (562 documentos):** `specialties` 8 · `doctors` 10 ·
>   `slots` 540 · `appointments` 3 · `users` 1.
> - **`app/google-services.json`:** generado y colocado (paquete
>   `com.tuempresa.citasmedicas`).
> - **APK release:** compilado y firmado, con estos datos incluidos.
> - **Firebase Auth (email/contraseña):** pendiente de habilitar en la consola.
>   Mientras tanto, la app **cae automáticamente al login simulado** (no se bloquea).
>   Firestore **sí** funciona: el login simulado usa `user_01`, que ya existe.

Esta guía explica, paso a paso, cómo **conectar tu propia cuenta de Firebase**,
**poblar la base de datos** con datos de prueba y **probar la app en la PC**
(emulador de Android Studio) sin necesidad de Mac ni iPhone.

---

## 1. ¿Qué archivos debo rellenar con MIS credenciales?

Solo **dos** cosas dependen de tu cuenta. El resto del proyecto funciona igual:

| Archivo | Para qué sirve | Dónde obtenerlo |
|---|---|---|
| `app/google-services.json` | Credenciales del **cliente Android** (la app). | Firebase Console → ⚙️ Configuración del proyecto → *Tus apps* → App Android. |
| Clave **Admin SDK** (`firebase-admin-sdk.json`) | **Solo** para subir los datos de prueba desde el PC (script Python). **No** va dentro de la app. | Firebase Console → ⚙️ Configuración del proyecto → *Cuentas de servicio* → **Python** → *Generar nueva clave privada*. |

> ⚠️ **Importante**: el plugin `google-services` solo se **activa** si existe
> `app/google-services.json`. Si NO lo pones, la app **igual compila** y funciona
> con los datos locales (modo demo sin nube). Así puedes probar el flujo de citas
> en la PC aunque todavía no tengas Firebase.

### 1.1. Pasos para `app/google-services.json`
1. Ve a <https://console.firebase.google.com/> y abre (o crea) tu proyecto.
2. Crea la app Android con el **nombre de paquete exacto**:
   ```
   com.tuempresa.citasmedicas
   ```
   > El paquete debe coincidir con `applicationId` de `app/build.gradle` y con el
   > `package` del `AndroidManifest.xml`. Si cambias el paquete, cámbialo en los
   > tres sitios (o edítalo también aquí).
3. Descarga `google-services.json` y colócalo en:
   ```
   app/google-services.json
   ```
4. (Opcional) Si quieres **login real con Firebase Auth**, habilítalo en
   Firebase Console → Build → Authentication → *Correo electrónico/contraseña*.

### 1.2. Pasos para la clave Admin SDK (subir datos de prueba)
1. Firebase Console → ⚙️ Configuración del proyecto → **Cuentas de servicio**.
2. Idioma **Python** → **Generar nueva clave privada** → se descarga un `.json`.
3. Guárdala, por ejemplo, en `/opt/flutter/firebase-admin-sdk.json` o donde
   prefieras y pásala al script como argumento.

---

## 2. Crear la base de datos y poblar los datos

### 2.1. Todo en UN comando (recomendado) — `setup_firebase.py`
Este único script lo hace **todo** con solo darle la clave Admin SDK:

1. Conecta con tu proyecto.
2. **Si la base Firestore NO existe, la CREA por ti** (vía API REST).
3. Espera a que esté lista.
4. Sube los datos de prueba (8 especialidades, 10 doctores, 540 horarios, 3 citas, 1 usuario).
5. Escribe la bandera `metadata/seed` y **verifica** leyendo de vuelta.

```bash
pip install firebase-admin==7.1.0        # ya viene instalado en el entorno
cd firebase/scripts
python3 setup_firebase.py /ruta/a/firebase-admin-sdk.json
```

Variantes útiles:
```bash
python3 setup_firebase.py /ruta/... --check       # solo verifica, no escribe
python3 setup_firebase.py /ruta/... --force       # re-siembra aunque ya exista
python3 setup_firebase.py /ruta/... --location europe-west1   # otra ubicación
python3 setup_firebase.py                          # auto-busca la clave en tu equipo
```

> 💡 Si no indicas la ruta, el script **busca automáticamente** la clave en
> ubicaciones comunes (`/opt/flutter`, `~/uploaded_files`, `~/Downloads`, `~`, etc.).

### 2.2. Alternativa: solo subir datos (si ya creaste la base a mano)
Firebase Console → **Build → Firestore Database → Crear base de datos**.
Luego:
```bash
cd firebase/scripts
python3 upload_seed.py /ruta/a/firebase-admin-sdk.json
```

> 🔁 Ambos scripts son **idempotentes**: usan `metadata/seed` con una `version`.
> Si los datos ya están subidos, no vuelven a escribir (1 sola lectura).
> Si cambias los datos, sube `SEED_VERSION` para forzar el refresco.

### 2.3. Alternativa: siembra desde la propia app (sin PC)
La app puede sembrar los datos sola la **primera vez** que un usuario inicia
sesión (clase `FirestoreSeeder.java`). También es idempotente: **1 lectura** para
comprobar la bandera y **1 batch** de escritura si hace falta.

### 2.4. Regenerar los JSON (opcional)
```bash
cd firebase/scripts
python3 generate_seed.py   # regenera firebase/seed/*.json
```

### 2.5. Reglas de seguridad
`firebase/firestore.rules` incluye dos bloques:
- **DEMO** (activo): `allow read, write: if true;` → ideal para probar en PC.
- **PRODUCCIÓN** (comentado): descoméntalo cuando uses Firebase Auth y quieras
  proteger los datos por usuario.

Pégalas en Firebase Console → Firestore Database → **Reglas** → *Publicar*.
O con la CLI: `firebase deploy --only firestore:rules`.

---

## 3. Optimización de costos aplicada (cuenta demo/gratuita)

| Técnica | Dónde está | Beneficio |
|---|---|---|
| **1 lectura + 1 batch** en la siembra | `FirestoreSeeder.java`, `upload_seed.py` | No re-escribe datos ya existentes. |
| **`limit(PAGE_SIZE)`** en toda consulta | `FirestoreDoctorRepository`, `FirestoreAppointmentRepository` | Acota el número máximo de documentos leídos. |
| **`get()` en lugar de `addSnapshotListener`** | Toda la capa Firestore | Evita sockets abiertos y lecturas en tiempo real recurrentes. |
| **Sin `orderBy()` + `whereEqualTo()`** | Filtro por 1 igualdad + orden **en memoria** | Evita índices compuestos y consultas más caras. |
| **Caché offline de Firestore** | `FirebaseManager.java` (memoria + disco) | Muchas lecturas se sirven localmente, sin facturar red. |
| **Arquitectura local-first (Room)** | `AppointmentRepository` | La UI lee de Room; Firestore solo se sincroniza a demanda. |
| **Cancelar = 1 `update()` de un campo** | `FirestoreAppointmentRepository.cancel` | Escritura mínima (no reescribe el documento completo). |

**Esquema NoSQL desnormalizado** (documentado en `FirestoreContract.java`):
```
specialties/{id}    name, iconEmoji, description, doctorCount
doctors/{id}        fullName, specialtyId, specialtyName*, bio, rating,
                    yearsExperience, age, consultationFee, hospital
slots/{id}          doctorId, date, time, available
appointments/{id}   userId, doctorId, doctorName, specialtyName, patientName,
                    patientAge, date, time, status, reason, createdAt
users/{id}          fullName, email, phone, age
metadata/seed       version, ... (bandera de siembra)
```
`* specialtyName` se **duplica** a propósito en `doctors` para pintar la lista sin
leer `specialties` (menos lecturas).

---

## 4. Probar en la PC (Android Studio / emulador)

### 4.1. Requisitos
- Android Studio + un **emulador (AVD)** con API 24+.
- Java 17/21.

### 4.2. Abrir y ejecutar
1. `Android Studio → Open` y selecciona la carpeta del proyecto.
2. Si usas Firebase, copia `app/google-services.json` **antes** de abrir.
3. Crea un AVD y pulsa **Run ▶**. La app arranca en el emulador.

### 4.3. Flujo a probar (doctor → fecha → horario → confirmar)
1. Inicia sesión (cualquier email válido + contraseña de 4+ caracteres).
2. En **Doctores**, toca una tarjeta → abre el detalle. *(Fix Error 1)*
3. Elige una **fecha** y toca un **horario disponible** → se resalta. *(Fix Error 2)*
4. El botón **Confirmar cita** se habilita y guarda la cita. *(Fix Error 3)*
5. En **Mis Citas** verás la cita; puedes cancelarla.

### 4.4. Compilar por línea de comandos (sin Android Studio)
```bash
export ANDROID_HOME=/ruta/a/tu/Android/Sdk
./gradlew :app:assembleDebug
# APK en: app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 5. Notas de compatibilidad

- Versiones de Firebase elegidas **compatibles con AGP 8.5.2 y JDK 17/21**:
  `firebase-bom:32.7.4` (arrastra `firebase-firestore` y `firebase-auth`).
- Si tu entorno exige otras versiones, ajusta **solo** el BOM en
  `app/build.gradle` (una línea) y deja que el BOM gestione el resto.
- El login sigue siendo **simulado** por defecto. La dependencia `firebase-auth`
  ya está incluida para cuando quieras migrar a autenticación real.

---

## 6. Login OFICIAL con Firebase Auth (email/contraseña)

La app usa **Firebase Auth real** cuando está habilitado; si no, **cae sola al modo
simulado** (la demo nunca se bloquea). Para activar el login oficial:

1. Firebase Console → **Compilación → Authentication → Comenzar**.
2. Pestaña **Método de acceso** → habilita **Correo electrónico/contraseña** → Guardar.
   > ⚠️ En proyectos nuevos, Google puede pedir **vincular una cuenta de
   > facturación** (plan Blaze) para inicializar Authentication. Si no quieres
   > activar facturación, **deja el login simulado**: la app funciona igual y
   > Firestore sigue leyendo/escribiendo datos reales.
3. Compila (ya incluye `app/google-services.json`).
   - Con Auth activo: login/registro REALES (Firebase exige **contraseña ≥ 6**).
   - Sin Auth: login simulado (contraseña ≥ 4), sin bloquear la demo.

Tras el alta se guarda un perfil en `users/{uid}` (1 escritura) y tras el login se
lee **una sola vez** (1 lectura) — optimización de costos.

> 💡 **Fallback automático**: si el proveedor no está habilitado o el proyecto pide
> facturación, la app **detecta el error y usa el login local** en lugar de mostrar
> un fallo. Lo implementa `FirebaseAuthDataSource.isAuthDisabledError()` +
> `AuthRepository`.

### ¿Cómo me das acceso a MI Firebase para que yo genere la base de datos?
Tienes 3 opciones, de la menos a la más sensible:

| Opción | Qué me envías | Puedo hacer | Riesgo |
|---|---|---|---|
| **A. Mejor / recomendada** | Nada | Yo dejo TODO el código, los scripts y los JSON listos. **Tú** ejecutas `setup_firebase.py` en tu PC (2 comandos). Crea la base y siembra sola. | Ninguno. Las llaves nunca salen de tu equipo. |
| **B. Práctico** | La **clave Admin SDK (Python)** [+ el **`google-services.json`** de tu app] | Yo **creo tu base de datos Firestore automáticamente** desde aquí, la siembro y dejo todo listo. Solo tú subes el/los archivo(s). | Medio: son claves reales de tu proyecto. |
| **C. Avanzado** | Nada | Configuras **Firebase Emulator Suite** en tu PC y pruebas todo local, sin tocar la nube ni gastar cuota. | Ninguno; requiere instalar la CLI. |

> ⚠️ **Nunca compartas**: contraseñas de tu cuenta Google, tokens de la consola ni el
> `serviceAccountKey.json` de un proyecto de producción. La clave Admin SDK otorga
> acceso total a Firestore; úsala idealmente en un proyecto **de prueba** y rótala
> (elimínala/regenérala) al terminar.

> 🟢 **Camino sin riesgos**: usa la **Opción A**. Ya está todo preparado; solo tienes
> que poner TU `google-services.json` y ejecutar el script. No necesito acceso.

#### Flujo Opción B (tú me das la clave)
1. Firebase Console → ⚙️ Configuración del proyecto → **Cuentas de servicio** → **Python** → *Generar nueva clave privada*.
2. Súbela con la **pestaña de archivos** (o suéltala en el chat).
3. Yo ejecuto:
   ```bash
   cd firebase/scripts
   python3 setup_firebase.py            # auto-detecta la clave y crea+siembra la base
   ```
4. Te devuelvo el resumen con los conteos verificados de cada colección.

#### Flujo Opción A (tú lo haces, sin darme claves) — 3 pasos
1. Descarga el kit: `firebase/CitasMedicas-Firebase-Kit.zip` (incluye `scripts/` + `seed/` + esta guía).
2. Descomprime y en una terminal:
   ```bash
   pip install firebase-admin==7.1.0
   cd scripts && python3 setup_firebase.py /ruta/a/tu-firebase-admin-sdk.json
   ```
3. Copia `google-services.json` a `app/google-services.json` y compila. ¡Listo!
