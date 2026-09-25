package com.tuempresa.citasmedicas.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Modelo de dominio de una cita médica (independiente de la entidad de Room).
 */
public class Appointment implements Serializable {

    private String id;
    private String doctorId;
    private String doctorName;
    private String specialtyName;
    private String patientName;
    private int patientAge;
    private String date;
    private String time;
    private AppointmentStatus status;
    private String reason;
    private long createdAt;

    public Appointment() {
    }

    public Appointment(String id, String doctorId, String doctorName, String specialtyName,
                       String patientName, int patientAge, String date, String time,
                       AppointmentStatus status, String reason, long createdAt) {
        this.id = id;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.specialtyName = specialtyName;
        this.patientName = patientName;
        this.patientAge = patientAge;
        this.date = date;
        this.time = time;
        this.status = status;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public String getSpecialtyName() { return specialtyName; }
    public void setSpecialtyName(String specialtyName) { this.specialtyName = specialtyName; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    /** Edad del paciente al agendar la cita. */
    public int getPatientAge() { return patientAge; }
    public void setPatientAge(int patientAge) { this.patientAge = patientAge; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public boolean isCancellable() {
        return status == AppointmentStatus.CONFIRMED || status == AppointmentStatus.PENDING;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Appointment that = (Appointment) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
