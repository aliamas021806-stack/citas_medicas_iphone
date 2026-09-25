package com.tuempresa.citasmedicas.model;

import java.util.Objects;

/**
 * Usuario (paciente) para el flujo simulado de autenticación.
 * Incluye la EDAD como parte de su información.
 */
public class User {

    private String id;
    private String fullName;
    private String email;
    private String phone;
    private int age;

    public User() {
    }

    public User(String id, String fullName, String email, String phone) {
        this(id, fullName, email, phone, 0);
    }

    public User(String id, String fullName, String email, String phone, int age) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.age = age;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }
}
