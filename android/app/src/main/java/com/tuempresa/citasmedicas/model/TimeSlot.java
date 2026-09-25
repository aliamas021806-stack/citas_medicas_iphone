package com.tuempresa.citasmedicas.model;

import java.io.Serializable;

/**
 * Horario (slot) disponible para un doctor. Formato fecha: yyyy-MM-dd ; hora: HH:mm
 */
public class TimeSlot implements Serializable {

    private String id;
    private String doctorId;
    private String date;
    private String time;
    private boolean available;

    public TimeSlot() {
    }

    public TimeSlot(String id, String doctorId, String date, String time, boolean available) {
        this.id = id;
        this.doctorId = doctorId;
        this.date = date;
        this.time = time;
        this.available = available;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}
