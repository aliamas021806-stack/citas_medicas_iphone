package com.tuempresa.citasmedicas.model;

import java.io.Serializable;

/**
 * Doctor / profesional de la salud.
 * Incluye la EDAD del doctor (campo expuesto por la API mock).
 */
public class Doctor implements Serializable {

    private String id;
    private String fullName;
    private String specialtyId;
    private String specialtyName;
    private String bio;
    private double rating;
    private int yearsExperience;
    private int age;
    private double consultationFee;
    private String hospital;
    private String photoUrl;

    public Doctor() {
    }

    public Doctor(String id, String fullName, String specialtyId, String specialtyName,
                  String bio, double rating, int yearsExperience, int age,
                  double consultationFee, String hospital, String photoUrl) {
        this.id = id;
        this.fullName = fullName;
        this.specialtyId = specialtyId;
        this.specialtyName = specialtyName;
        this.bio = bio;
        this.rating = rating;
        this.yearsExperience = yearsExperience;
        this.age = age;
        this.consultationFee = consultationFee;
        this.hospital = hospital;
        this.photoUrl = photoUrl;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getSpecialtyId() { return specialtyId; }
    public void setSpecialtyId(String specialtyId) { this.specialtyId = specialtyId; }
    public String getSpecialtyName() { return specialtyName; }
    public void setSpecialtyName(String specialtyName) { this.specialtyName = specialtyName; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public int getYearsExperience() { return yearsExperience; }
    public void setYearsExperience(int yearsExperience) { this.yearsExperience = yearsExperience; }

    /** Edad del doctor en años. */
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public double getConsultationFee() { return consultationFee; }
    public void setConsultationFee(double consultationFee) { this.consultationFee = consultationFee; }
    public String getHospital() { return hospital; }
    public void setHospital(String hospital) { this.hospital = hospital; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    /** Prefijo del título profesional (Dr./Dra.) según el nombre. */
    public String getDisplayName() {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "Doctor";
        }
        String firstName = fullName.trim().split("\\s+")[0];
        boolean female = firstName.toLowerCase().endsWith("a");
        return (female ? "Dra. " : "Dr. ") + fullName;
    }

    /** Iniciales para avatar. */
    public String getInitials() {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "?";
        }
        String[] parts = fullName.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        sb.append(Character.toUpperCase(parts[0].charAt(0)));
        if (parts.length > 1) {
            sb.append(Character.toUpperCase(parts[parts.length - 1].charAt(0)));
        }
        return sb.toString();
    }
}
