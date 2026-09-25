package com.tuempresa.citasmedicas.data.firebase;

/**
 * Contrato del esquema NoSQL en Firestore.
 *
 * <p><b>Diseño desnormalizado y anti-coste</b>: se modela la información para
 * que la pantalla principal se resuelva con un número CONSTANTE y pequeño de
 * lecturas (ideal 2-3), evitando bucles de consultas (N+1).
 *
 * <pre>
 * specialties/{specialtyId}
 *   name, iconEmoji, description, doctorCount
 *
 * doctors/{doctorId}
 *   fullName, specialtyId, specialtyName, bio, rating, yearsExperience,
 *   age, consultationFee, hospital, photoUrl
 *   -> specialtyName se duplica de forma deliberada: la lista de doctores se
 *      pinta sin necesidad de leer specialties (evita lecturas extra).
 *
 * slots/{slotId}                     (slotId = doctorId_yyyy-MM-dd_HHmm)
 *   doctorId, date, time, available
 *
 * appointments/{autoId}
 *   userId, doctorId, doctorName, specialtyName, patientName, patientAge,
 *   date, time, status, reason, createdAt
 *
 * metadata/seed
 *   version, doctorsSeeded, specialtiesSeeded, slotsSeeded, appointmentsSeeded
 *   -> bandera de siembra IDEMPOTENTE: se consulta 1 vez para no re-sembrar.
 * </pre>
 */
public final class FirestoreContract {

    private FirestoreContract() {
    }

    public static final String COL_SPECIALTIES = "specialties";
    public static final String COL_DOCTORS = "doctors";
    public static final String COL_SLOTS = "slots";
    public static final String COL_APPOINTMENTS = "appointments";
    public static final String COL_USERS = "users";

    public static final String COL_METADATA = "metadata";
    public static final String DOC_SEED = "seed";

    // ---- Campos ----
    public static final String F_ID = "id";
    public static final String F_NAME = "name";
    public static final String F_FULL_NAME = "fullName";
    public static final String F_SPECIALTY_ID = "specialtyId";
    public static final String F_SPECIALTY_NAME = "specialtyName";
    public static final String F_ICON = "iconEmoji";
    public static final String F_DESCRIPTION = "description";
    public static final String F_DOCTOR_COUNT = "doctorCount";
    public static final String F_BIO = "bio";
    public static final String F_RATING = "rating";
    public static final String F_YEARS = "yearsExperience";
    public static final String F_AGE = "age";
    public static final String F_FEE = "consultationFee";
    public static final String F_HOSPITAL = "hospital";
    public static final String F_PHOTO = "photoUrl";

    public static final String F_DOCTOR_ID = "doctorId";
    public static final String F_DATE = "date";
    public static final String F_TIME = "time";
    public static final String F_AVAILABLE = "available";

    public static final String F_USER_ID = "userId";
    public static final String F_DOCTOR_NAME = "doctorName";
    public static final String F_PATIENT_NAME = "patientName";
    public static final String F_PATIENT_AGE = "patientAge";
    public static final String F_STATUS = "status";
    public static final String F_REASON = "reason";
    public static final String F_CREATED_AT = "createdAt";

    /** Tamaño de página por defecto. Mantener BAJO para minimizar lecturas. */
    public static final int PAGE_SIZE = 20;
}
