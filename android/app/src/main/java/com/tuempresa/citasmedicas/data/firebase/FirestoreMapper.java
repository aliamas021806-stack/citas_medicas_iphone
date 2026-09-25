package com.tuempresa.citasmedicas.data.firebase;

import com.google.firebase.firestore.DocumentSnapshot;
import com.tuempresa.citasmedicas.model.Appointment;
import com.tuempresa.citasmedicas.model.AppointmentStatus;
import com.tuempresa.citasmedicas.model.Doctor;
import com.tuempresa.citasmedicas.model.Patient;
import com.tuempresa.citasmedicas.model.Specialty;
import com.tuempresa.citasmedicas.model.TimeSlot;

import java.util.HashMap;
import java.util.Map;

/**
 * Conversión entre modelos de dominio y documentos de Firestore.
 *
 * <p>Importante para ROOM/Firestore en Flutter se usan claves explícitas; en
 * Java usamos Map para no depender de la reflexión/ProGuard y controlar
 * exactamente qué campos se escriben (menos bytes = menos coste de escritura).
 */
public final class FirestoreMapper {

    private FirestoreMapper() {
    }

    // ------------------------- SPECIALTY -------------------------
    public static Specialty toSpecialty(DocumentSnapshot doc) {
        Specialty s = new Specialty();
        s.setId(value(doc, FirestoreContract.F_ID, doc.getId()));
        s.setName(value(doc, FirestoreContract.F_NAME, ""));
        s.setIconEmoji(value(doc, FirestoreContract.F_ICON, "\ud83e\ude7a"));
        s.setDescription(value(doc, FirestoreContract.F_DESCRIPTION, ""));
        s.setDoctorCount(number(doc, FirestoreContract.F_DOCTOR_COUNT));
        return s;
    }

    public static Map<String, Object> fromSpecialty(Specialty s) {
        Map<String, Object> m = new HashMap<>();
        m.put(FirestoreContract.F_ID, s.getId());
        m.put(FirestoreContract.F_NAME, s.getName());
        m.put(FirestoreContract.F_ICON, s.getIconEmoji());
        m.put(FirestoreContract.F_DESCRIPTION, s.getDescription());
        m.put(FirestoreContract.F_DOCTOR_COUNT, s.getDoctorCount());
        return m;
    }

    // --------------------------- DOCTOR --------------------------
    public static Doctor toDoctor(DocumentSnapshot doc) {
        Doctor d = new Doctor();
        d.setId(value(doc, FirestoreContract.F_ID, doc.getId()));
        d.setFullName(value(doc, FirestoreContract.F_FULL_NAME, ""));
        d.setSpecialtyId(value(doc, FirestoreContract.F_SPECIALTY_ID, ""));
        d.setSpecialtyName(value(doc, FirestoreContract.F_SPECIALTY_NAME, ""));
        d.setBio(value(doc, FirestoreContract.F_BIO, ""));
        d.setRating(decimal(doc, FirestoreContract.F_RATING));
        d.setYearsExperience(number(doc, FirestoreContract.F_YEARS));
        d.setAge(number(doc, FirestoreContract.F_AGE));
        d.setConsultationFee(decimal(doc, FirestoreContract.F_FEE));
        d.setHospital(value(doc, FirestoreContract.F_HOSPITAL, ""));
        d.setPhotoUrl(doc.getString(FirestoreContract.F_PHOTO));
        return d;
    }

    public static Map<String, Object> fromDoctor(Doctor d) {
        Map<String, Object> m = new HashMap<>();
        m.put(FirestoreContract.F_ID, d.getId());
        m.put(FirestoreContract.F_FULL_NAME, d.getFullName());
        m.put(FirestoreContract.F_SPECIALTY_ID, d.getSpecialtyId());
        m.put(FirestoreContract.F_SPECIALTY_NAME, d.getSpecialtyName());
        m.put(FirestoreContract.F_BIO, d.getBio());
        m.put(FirestoreContract.F_RATING, d.getRating());
        m.put(FirestoreContract.F_YEARS, d.getYearsExperience());
        m.put(FirestoreContract.F_AGE, d.getAge());
        m.put(FirestoreContract.F_FEE, d.getConsultationFee());
        m.put(FirestoreContract.F_HOSPITAL, d.getHospital());
        m.put(FirestoreContract.F_PHOTO, d.getPhotoUrl());
        return m;
    }

    // ---------------------------- SLOT ---------------------------
    public static TimeSlot toSlot(DocumentSnapshot doc) {
        TimeSlot t = new TimeSlot();
        t.setId(doc.getId());
        t.setDoctorId(value(doc, FirestoreContract.F_DOCTOR_ID, ""));
        t.setDate(value(doc, FirestoreContract.F_DATE, ""));
        t.setTime(value(doc, FirestoreContract.F_TIME, ""));
        Boolean av = doc.getBoolean(FirestoreContract.F_AVAILABLE);
        t.setAvailable(av == null || av);
        return t;
    }

    public static Map<String, Object> fromSlot(TimeSlot t) {
        Map<String, Object> m = new HashMap<>();
        m.put(FirestoreContract.F_DOCTOR_ID, t.getDoctorId());
        m.put(FirestoreContract.F_DATE, t.getDate());
        m.put(FirestoreContract.F_TIME, t.getTime());
        m.put(FirestoreContract.F_AVAILABLE, t.isAvailable());
        return m;
    }

    // ------------------------ APPOINTMENT ------------------------
    public static Appointment toAppointment(DocumentSnapshot doc) {
        Appointment a = new Appointment();
        a.setId(doc.getId());
        a.setDoctorId(value(doc, FirestoreContract.F_DOCTOR_ID, ""));
        a.setDoctorName(value(doc, FirestoreContract.F_DOCTOR_NAME, ""));
        a.setSpecialtyName(value(doc, FirestoreContract.F_SPECIALTY_NAME, ""));
        a.setPatientName(value(doc, FirestoreContract.F_PATIENT_NAME, ""));
        a.setPatientAge(number(doc, FirestoreContract.F_PATIENT_AGE));
        a.setDate(value(doc, FirestoreContract.F_DATE, ""));
        a.setTime(value(doc, FirestoreContract.F_TIME, ""));
        a.setStatus(AppointmentStatus.fromValue(value(doc, FirestoreContract.F_STATUS, "Confirmed")));
        a.setReason(value(doc, FirestoreContract.F_REASON, ""));
        Long created = doc.getLong(FirestoreContract.F_CREATED_AT);
        a.setCreatedAt(created != null ? created : System.currentTimeMillis());
        return a;
    }

    public static Map<String, Object> fromAppointment(Appointment a, String userId) {
        Map<String, Object> m = new HashMap<>();
        m.put(FirestoreContract.F_USER_ID, userId);
        m.put(FirestoreContract.F_DOCTOR_ID, a.getDoctorId());
        m.put(FirestoreContract.F_DOCTOR_NAME, a.getDoctorName());
        m.put(FirestoreContract.F_SPECIALTY_NAME, a.getSpecialtyName());
        m.put(FirestoreContract.F_PATIENT_NAME, a.getPatientName());
        m.put(FirestoreContract.F_PATIENT_AGE, a.getPatientAge());
        m.put(FirestoreContract.F_DATE, a.getDate());
        m.put(FirestoreContract.F_TIME, a.getTime());
        m.put(FirestoreContract.F_STATUS, a.getStatus() != null
                ? a.getStatus().getValue() : AppointmentStatus.CONFIRMED.getValue());
        m.put(FirestoreContract.F_REASON, a.getReason());
        m.put(FirestoreContract.F_CREATED_AT, System.currentTimeMillis());
        return m;
    }

    // ---------------------------- USER ---------------------------
    public static Patient toPatient(DocumentSnapshot doc) {
        Patient p = new Patient();
        p.setId(doc.getId());
        p.setFullName(value(doc, FirestoreContract.F_FULL_NAME, ""));
        p.setEmail(value(doc, "email", ""));
        p.setPhone(value(doc, "phone", ""));
        p.setAge(number(doc, FirestoreContract.F_AGE));
        return p;
    }

    // --------------------------- HELPERS -------------------------
    private static String value(DocumentSnapshot doc, String key, String def) {
        String v = doc.getString(key);
        return v != null ? v : def;
    }

    private static int number(DocumentSnapshot doc, String key) {
        Long l = doc.getLong(key);
        return l != null ? l.intValue() : 0;
    }

    private static double decimal(DocumentSnapshot doc, String key) {
        Double d = doc.getDouble(key);
        return d != null ? d : 0d;
    }
}
