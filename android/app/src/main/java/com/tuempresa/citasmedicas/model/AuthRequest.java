package com.tuempresa.citasmedicas.model;

/**
 * Credenciales para login / registro simulado (incluye edad del paciente).
 */
public class AuthRequest {

    private String fullName;
    private String email;
    private String password;
    private int age;

    public AuthRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public AuthRequest(String fullName, String email, String password) {
        this(fullName, email, password, 0);
    }

    public AuthRequest(String fullName, String email, String password, int age) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.age = age;
    }

    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }

    /** Edad indicada en el registro (0 si no se especifica). */
    public int getAge() { return age; }
}
