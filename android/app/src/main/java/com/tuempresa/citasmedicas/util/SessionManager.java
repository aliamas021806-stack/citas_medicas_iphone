package com.tuempresa.citasmedicas.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.tuempresa.citasmedicas.model.User;

/**
 * Gestiona la sesión del usuario (login simulado) persistida con
 * SharedPreferences. Almacena también la EDAD del paciente.
 */
public class SessionManager {

    private static final String PREF_NAME = "citas_medicas_session";
    private static final String KEY_LOGGED = "logged_in";
    private static final String KEY_ID = "user_id";
    private static final String KEY_NAME = "user_name";
    private static final String KEY_EMAIL = "user_email";
    private static final String KEY_PHONE = "user_phone";
    private static final String KEY_AGE = "user_age";

    private final SharedPreferences prefs;
    private static SessionManager instance;

    private SessionManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }

    public void saveSession(User user) {
        prefs.edit()
                .putBoolean(KEY_LOGGED, true)
                .putString(KEY_ID, user.getId())
                .putString(KEY_NAME, user.getFullName())
                .putString(KEY_EMAIL, user.getEmail())
                .putString(KEY_PHONE, user.getPhone())
                .putInt(KEY_AGE, user.getAge())
                .apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_LOGGED, false);
    }

    public User getUser() {
        if (!isLoggedIn()) {
            return null;
        }
        return new User(
                prefs.getString(KEY_ID, "user_01"),
                prefs.getString(KEY_NAME, "Paciente"),
                prefs.getString(KEY_EMAIL, ""),
                prefs.getString(KEY_PHONE, ""),
                prefs.getInt(KEY_AGE, 0));
    }

    public void logout() {
        // Cierra también la sesión de Firebase Auth si estaba activa.
        try {
            com.tuempresa.citasmedicas.data.firebase.FirebaseAuthDataSource.signOut();
        } catch (Throwable ignored) {
        }
        prefs.edit().clear().apply();
    }
}
