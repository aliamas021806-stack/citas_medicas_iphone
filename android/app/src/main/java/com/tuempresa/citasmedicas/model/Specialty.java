package com.tuempresa.citasmedicas.model;

/**
 * Especialidad médica (Cardiología, Pediatría, etc.).
 */
public class Specialty {

    private String id;
    private String name;
    private String iconEmoji;
    private String description;
    private int doctorCount;

    public Specialty() {
    }

    public Specialty(String id, String name, String iconEmoji, String description, int doctorCount) {
        this.id = id;
        this.name = name;
        this.iconEmoji = iconEmoji;
        this.description = description;
        this.doctorCount = doctorCount;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIconEmoji() { return iconEmoji; }
    public void setIconEmoji(String iconEmoji) { this.iconEmoji = iconEmoji; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getDoctorCount() { return doctorCount; }
    public void setDoctorCount(int doctorCount) { this.doctorCount = doctorCount; }
}
