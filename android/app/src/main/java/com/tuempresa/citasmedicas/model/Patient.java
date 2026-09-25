package com.tuempresa.citasmedicas.model;

import java.io.Serializable;

/**
 * Información del paciente expuesta por la API mock.
 * <p>
 * Los datos son generados por IA (valores sintéticos aleatorios controlados)
 * para simular un backend de expediente clínico. Incluye la EDAD del paciente
 * como campo de primera clase relacionado con su información.
 */
public class Patient implements Serializable {

    private String id;
    private String fullName;
    private String email;
    private String phone;
    private int age;                 // EDAD del paciente (años)
    private String bloodType;        // Grupo sanguíneo
    private String gender;           // Género
    private String address;          // Dirección
    private String insurance;        // Aseguradora
    private String allergies;        // Alergias conocidas
    private String chronicConditions;// Condiciones crónicas
    private double heightCm;         // Estatura (cm)
    private double weightKg;         // Peso (kg)
    private String emergencyContact; // Contacto de emergencia
    private long createdAt;          // Alta en el sistema

    public Patient() {
    }

    public Patient(String id, String fullName, String email, String phone, int age,
                   String bloodType, String gender, String address, String insurance,
                   String allergies, String chronicConditions, double heightCm,
                   double weightKg, String emergencyContact, long createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.age = age;
        this.bloodType = bloodType;
        this.gender = gender;
        this.address = address;
        this.insurance = insurance;
        this.allergies = allergies;
        this.chronicConditions = chronicConditions;
        this.heightCm = heightCm;
        this.weightKg = weightKg;
        this.emergencyContact = emergencyContact;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    /** Edad del paciente en años. */
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getBloodType() { return bloodType; }
    public void setBloodType(String bloodType) { this.bloodType = bloodType; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getInsurance() { return insurance; }
    public void setInsurance(String insurance) { this.insurance = insurance; }
    public String getAllergies() { return allergies; }
    public void setAllergies(String allergies) { this.allergies = allergies; }
    public String getChronicConditions() { return chronicConditions; }
    public void setChronicConditions(String chronicConditions) { this.chronicConditions = chronicConditions; }
    public double getHeightCm() { return heightCm; }
    public void setHeightCm(double heightCm) { this.heightCm = heightCm; }
    public double getWeightKg() { return weightKg; }
    public void setWeightKg(double weightKg) { this.weightKg = weightKg; }
    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

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

    /** Índice de masa corporal (IMC) calculado. */
    public double getBmi() {
        if (heightCm <= 0) {
            return 0;
        }
        double m = heightCm / 100.0;
        return Math.round((weightKg / (m * m)) * 10.0) / 10.0;
    }
}
