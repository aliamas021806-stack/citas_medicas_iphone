package com.tuempresa.citasmedicas.model;

/**
 * Estado de una cita médica.
 */
public enum AppointmentStatus {
    CONFIRMED("Confirmed"),
    PENDING("Pending"),
    CANCELLED("Cancelled");

    private final String value;

    AppointmentStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static AppointmentStatus fromValue(String value) {
        if (value == null) {
            return CONFIRMED;
        }
        for (AppointmentStatus s : values()) {
            if (s.value.equalsIgnoreCase(value) || s.name().equalsIgnoreCase(value)) {
                return s;
            }
        }
        return CONFIRMED;
    }
}
