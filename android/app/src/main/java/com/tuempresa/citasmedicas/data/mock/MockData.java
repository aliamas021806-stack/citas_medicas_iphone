package com.tuempresa.citasmedicas.data.mock;

import com.tuempresa.citasmedicas.model.Appointment;
import com.tuempresa.citasmedicas.model.AppointmentStatus;
import com.tuempresa.citasmedicas.model.Doctor;
import com.tuempresa.citasmedicas.model.Patient;
import com.tuempresa.citasmedicas.model.Specialty;
import com.tuempresa.citasmedicas.model.TimeSlot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Fuente de datos sintéticos (Mock Data).
 * <p>
 * Simula la respuesta de un backend real. Los DOCTORES y, sobre todo, los
 * PACIENTES son "datos generados por IA al azar" (valores deterministas
 * generados programáticamente) que se exponen a través de la API mock
 * ({@code MockInterceptor}). Cada paciente incluye su EDAD como parte de su
 * información clínica.
 */
public final class MockData {

    private MockData() {
        // Clase utilitaria
    }

    public static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    // =====================================================================
    // ESPECIALIDADES
    // =====================================================================
    public static List<Specialty> getSpecialties() {
        List<Specialty> list = new ArrayList<>();
        list.add(new Specialty("esp_01", "Cardiología", "\uD83E\uDEC0", "Diagnóstico y tratamiento de enfermedades del corazón", 3));
        list.add(new Specialty("esp_02", "Pediatría", "\uD83E\uDDD2", "Atención médica integral para niños y adolescentes", 2));
        list.add(new Specialty("esp_03", "Dermatología", "\uD83E\uDDF4", "Cuidado de la piel, cabello y uñas", 2));
        list.add(new Specialty("esp_04", "Traumatología", "\uD83E\uDDB4", "Lesiones del sistema músculo-esquelético", 2));
        list.add(new Specialty("esp_05", "Neurología", "\uD83E\uDDE0", "Trastornos del sistema nervioso central y periférico", 2));
        list.add(new Specialty("esp_06", "Medicina General", "\uD83E\uDE7A", "Atención primaria y consulta general", 3));
        list.add(new Specialty("esp_07", "Ginecología", "\uD83C\uDF38", "Salud reproductiva y femenina", 2));
        list.add(new Specialty("esp_08", "Oftalmología", "\uD83D\uDC41", "Cuidado de la visión y enfermedades oculares", 2));
        return list;
    }

    public static Specialty getSpecialtyById(String id) {
        for (Specialty s : getSpecialties()) {
            if (s.getId().equals(id)) {
                return s;
            }
        }
        return null;
    }

    // =====================================================================
    // DOCTORES (con EDAD)
    // =====================================================================
    public static List<Doctor> getDoctors() {
        List<Doctor> list = new ArrayList<>();
        list.add(new Doctor("doc_01", "Carlos Ramírez", "esp_01", "Cardiología",
                "Cardiólogo intervencionista con amplia experiencia en arritmias y prevención cardiovascular.",
                4.8, 15, 52, 45.0, "Hospital Central Vitalis", null));
        list.add(new Doctor("doc_02", "María González", "esp_02", "Pediatría",
                "Pediatra dedicada al desarrollo infantil y vacunación. Paciente y cercana con los más pequeños.",
                4.9, 12, 44, 35.0, "Clínica Infantil Aurora", null));
        list.add(new Doctor("doc_03", "Andrea López", "esp_03", "Dermatología",
                "Especialista en dermatología clínica y estética, tratamiento del acné y cuidado del melanoma.",
                4.7, 10, 39, 40.0, "Centro DermaSalud", null));
        list.add(new Doctor("doc_04", "Javier Moreno", "esp_04", "Traumatología",
                "Traumatólogo especializado en lesiones deportivas y cirugía de rodilla.",
                4.6, 14, 47, 50.0, "Hospital Central Vitalis", null));
        list.add(new Doctor("doc_05", "Lucía Fernández", "esp_05", "Neurología",
                "Neuróloga con enfoque en epilepsia, cefaleas y trastornos del movimiento.",
                4.9, 18, 55, 55.0, "Instituto NeuroVida", null));
        list.add(new Doctor("doc_06", "Diego Torres", "esp_06", "Medicina General",
                "Médico general orientado a la prevención y seguimiento de enfermedades crónicas.",
                4.5, 8, 36, 25.0, "Centro Médico Familiar", null));
        list.add(new Doctor("doc_07", "Sofía Herrera", "esp_07", "Ginecología",
                "Ginecóloga y obstetra, control prenatal y salud reproductiva de la mujer.",
                4.8, 13, 45, 42.0, "Clínica Mujer & Vida", null));
        list.add(new Doctor("doc_08", "Pablo Castro", "esp_08", "Oftalmología",
                "Oftalmólogo especializado en cataratas, glaucoma y cirugía refractiva láser.",
                4.7, 16, 49, 48.0, "Instituto Ocular Visio", null));
        list.add(new Doctor("doc_09", "Elena Ruiz", "esp_01", "Cardiología",
                "Cardióloga clínica enfocada en hipertensión y rehabilitación cardíaca.",
                4.6, 9, 38, 43.0, "Hospital del Corazón", null));
        list.add(new Doctor("doc_10", "Miguel Ángel Díaz", "esp_06", "Medicina General",
                "Medicina general y urgencias, atención rápida y diagnóstico oportuno.",
                4.4, 6, 34, 22.0, "Centro Médico Familiar", null));
        return list;
    }

    public static List<Doctor> getDoctorsBySpecialty(String specialtyId) {
        List<Doctor> result = new ArrayList<>();
        for (Doctor d : getDoctors()) {
            if (d.getSpecialtyId().equals(specialtyId)) {
                result.add(d);
            }
        }
        return result;
    }

    public static Doctor getDoctorById(String doctorId) {
        for (Doctor d : getDoctors()) {
            if (d.getId().equals(doctorId)) {
                return d;
            }
        }
        return null;
    }

    // =====================================================================
    // PACIENTES  (DATOS GENERADOS POR IA AL AZAR)
    // =====================================================================
    /**
     * Devuelve el listado de pacientes generados por IA. Cada registro incluye
     * la EDAD del paciente junto con el resto de su información clínica.
     * Los valores son deterministas (seed fijo) para que la app muestre siempre
     * los mismos datos de demostración.
     */
    public static List<Patient> getPatients() {
        List<Patient> list = new ArrayList<>();

        // Datos base "generados por IA": nombre, edad, género, grupo sanguíneo...
        String[][] seed = {
                // id,        nombre,                email,                        teléfono,         edad, sangre, género,    dirección,                       aseguradora,        alergias,             crónicas,                    estatura, peso, contacto emergencia
                {"pat_01", "Laura Jiménez", "laura.jimenez@example.com", "+34 611 220 331", "34", "O+", "Femenino", "Calle Mayor 12, Madrid", "SaludPlus", "Penicilina", "Ninguna", "165", "62", "Jorge Jiménez"},
                {"pat_02", "Andrés Molina", "andres.molina@example.com", "+34 622 331 442", "47", "A+", "Masculino", "Av. Libertad 88, Sevilla", "VidaTotal", "Ninguna", "Hipertensión", "178", "85", "María Molina"},
                {"pat_03", "Carmen Delgado", "carmen.delgado@example.com", "+34 633 442 553", "29", "B-", "Femenino", "Calle Sol 3, Valencia", "SaludPlus", "Polen", "Asma leve", "160", "55", "Luis Delgado"},
                {"pat_04", "Roberto Sánchez", "roberto.sanchez@example.com", "+34 644 553 664", "61", "AB+", "Masculino", "Paseo Mar 45, Málaga", "MediSeguro", "Ibuprofeno", "Diabetes tipo 2", "172", "80", "Ana Sánchez"},
                {"pat_05", "Elena Navarro", "elena.navarro@example.com", "+34 655 664 775", "38", "O-", "Femenino", "Calle Luna 7, Bilbao", "VidaTotal", "Ninguna", "Ninguna", "168", "60", "Pedro Navarro"},
                {"pat_06", "Javier Ortega", "javier.ortega@example.com", "+34 666 775 886", "52", "A-", "Masculino", "Ronda Norte 21, Zaragoza", "SaludPlus", "Mariscos", "Colesterol alto", "180", "90", "Rosa Ortega"},
                {"pat_07", "Patricia Vega", "patricia.vega@example.com", "+34 677 886 997", "42", "B+", "Femenino", "Av. Europa 5, Alicante", "MediSeguro", "Ninguna", "Migraña crónica", "163", "58", "Diego Vega"},
                {"pat_08", "Fernando Lozano", "fernando.lozano@example.com", "+34 688 997 108", "25", "O+", "Masculino", "Calle Río 9, Granada", "VidaTotal", "Látex", "Ninguna", "175", "72", "Sara Lozano"},
                {"pat_09", "Isabel Ramírez", "isabel.ramirez@example.com", "+34 699 108 219", "70", "A+", "Femenino", "Plaza Centro 2, Murcia", "SaludPlus", "Aspirina", "Artrosis", "158", "65", "Mario Ramírez"},
                {"pat_10", "Tomás Iglesias", "tomas.iglesias@example.com", "+34 610 219 320", "33", "AB-", "Masculino", "Camino Viejo 14, Valladolid", "MediSeguro", "Ninguna", "Ninguna", "183", "78", "Lucía Iglesias"}
        };

        for (String[] r : seed) {
            list.add(new Patient(
                    r[0], r[1], r[2], r[3],
                    Integer.parseInt(r[4]),
                    r[5], r[6], r[7], r[8], r[9], r[10],
                    Double.parseDouble(r[11]), Double.parseDouble(r[12]), r[13],
                    System.currentTimeMillis()));
        }
        return list;
    }

    public static Patient getPatientById(String patientId) {
        for (Patient p : getPatients()) {
            if (p.getId().equals(patientId)) {
                return p;
            }
        }
        return null;
    }

    /** Edad media de los pacientes (estadística generada). */
    public static double getAveragePatientAge() {
        List<Patient> patients = getPatients();
        if (patients.isEmpty()) {
            return 0;
        }
        double sum = 0;
        for (Patient p : patients) {
            sum += p.getAge();
        }
        return Math.round((sum / patients.size()) * 10.0) / 10.0;
    }

    // =====================================================================
    // HORARIOS (SLOTS)
    // =====================================================================
    public static List<TimeSlot> getAvailableSlots(String doctorId) {
        List<TimeSlot> slots = new ArrayList<>();
        String[] hours = {"08:00", "09:00", "10:00", "11:00", "12:00",
                "15:00", "16:00", "17:00", "18:00"};

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        for (int day = 0; day < 7; day++) {
            if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                cal.add(Calendar.DAY_OF_MONTH, 1);
                continue;
            }
            String date = DATE_FORMAT.format(cal.getTime());
            int i = 0;
            for (String hour : hours) {
                boolean available = ((day * 7 + i) % 4) != 0;
                slots.add(new TimeSlot(
                        "slot_" + doctorId + "_" + date + "_" + hour.replace(":", ""),
                        doctorId, date, hour, available));
                i++;
            }
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        return slots;
    }

    public static List<TimeSlot> getSlotsByDate(String doctorId, String date) {
        List<TimeSlot> result = new ArrayList<>();
        for (TimeSlot s : getAvailableSlots(doctorId)) {
            if (s.getDate().equals(date)) {
                result.add(s);
            }
        }
        return result;
    }

    // =====================================================================
    // CITAS SEMILLA (para poblar Firebase y la base local)
    // =====================================================================
    /**
     * Citas de ejemplo usadas tanto por la siembra de Firestore como por la
     * base local Room. Compartir esta fuente garantiza que la app muestre los
     * mismos datos en cualquier modo (Firebase o local).
     */
    public static List<Appointment> getSeedAppointments() {
        List<String> dates = getSelectableDates();
        String d0 = dates.size() > 0 ? dates.get(0) : "2025-01-01";
        String d1 = dates.size() > 1 ? dates.get(1) : d0;
        String d3 = dates.size() > 3 ? dates.get(3) : d1;

        List<Appointment> list = new ArrayList<>();

        Appointment a1 = new Appointment();
        a1.setDoctorId("doc_01");
        a1.setDoctorName("Carlos Ramírez");
        a1.setSpecialtyName("Cardiología");
        a1.setPatientName("Laura Jiménez");
        a1.setPatientAge(34);
        a1.setDate(d1);
        a1.setTime("10:00");
        a1.setStatus(AppointmentStatus.CONFIRMED);
        a1.setReason("Chequeo cardiológico anual");
        a1.setCreatedAt(System.currentTimeMillis());
        list.add(a1);

        Appointment a2 = new Appointment();
        a2.setDoctorId("doc_02");
        a2.setDoctorName("María González");
        a2.setSpecialtyName("Pediatría");
        a2.setPatientName("Laura Jiménez");
        a2.setPatientAge(34);
        a2.setDate(d3);
        a2.setTime("16:00");
        a2.setStatus(AppointmentStatus.CONFIRMED);
        a2.setReason("Control de crecimiento");
        a2.setCreatedAt(System.currentTimeMillis());
        list.add(a2);

        Appointment a3 = new Appointment();
        a3.setDoctorId("doc_03");
        a3.setDoctorName("Andrea López");
        a3.setSpecialtyName("Dermatología");
        a3.setPatientName("Laura Jiménez");
        a3.setPatientAge(34);
        a3.setDate(d0);
        a3.setTime("09:00");
        a3.setStatus(AppointmentStatus.CANCELLED);
        a3.setReason("Revisión de lunar");
        a3.setCreatedAt(System.currentTimeMillis());
        list.add(a3);

        return list;
    }

    public static List<String> getSelectableDates() {        List<String> dates = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        for (int day = 0; day < 10 && dates.size() < 7; day++) {
            if (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                dates.add(DATE_FORMAT.format(cal.getTime()));
            }
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        return dates;
    }
}
