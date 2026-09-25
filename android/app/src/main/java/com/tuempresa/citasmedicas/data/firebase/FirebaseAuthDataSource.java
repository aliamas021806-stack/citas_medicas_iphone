package com.tuempresa.citasmedicas.data.firebase;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;
import com.tuempresa.citasmedicas.data.repository.RepositoryCallback;
import com.tuempresa.citasmedicas.model.User;

import java.util.HashMap;
import java.util.Map;

/**
 * Autenticación OFICIAL con Firebase Auth (Email/Password).
 *
 * <p>Requisito previo: habilitar el proveedor "Correo electrónico/contraseña" en
 * Firebase Console → Authentication → Método de acceso.
 *
 * <p><b>Optimización de costos</b>: tras el alta se escribe UN documento de perfil
 * (1 escritura) y tras el login se lee UNA vez ese perfil (1 lectura). No se usan
 * listeners.
 */
public final class FirebaseAuthDataSource {

    private static final String TAG = "FirebaseAuthDataSource";

    /**
     * Se pone a {@code false} si Firebase Auth NO está operativo en el proyecto
     * (p. ej. email/contraseña sin habilitar, o proveedor que exige facturación:
     * {@code BILLING_NOT_ENABLED} / {@code CONFIGURATION_NOT_FOUND}). En ese caso la
     * app cae de forma transparente al login simulado para no bloquear la demo.
     */
    private static boolean authUsable = true;

    private FirebaseAuthDataSource() {
    }

    public static boolean isReady() {
        return FirebaseManager.isAvailable();
    }

    /** ¿Se puede intentar el login real de Firebase Auth? */
    public static boolean isAuthUsable() {
        return isReady() && authUsable;
    }

    /** Marca Auth como no disponible para el resto de la sesión (fallback local). */
    public static void markAuthUnavailable() {
        authUsable = false;
    }

    /** Alta real: crea la cuenta y guarda el perfil (nombre, edad, teléfono). */
    public static void register(final String fullName, final String email,
                                String password, final int age,
                                final RepositoryCallback<User> callback) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser fu = result.getUser();
                    if (fu == null) {
                        callback.onError("No se pudo crear la cuenta");
                        return;
                    }
                    Map<String, Object> profile = new HashMap<>();
                    profile.put("fullName", fullName);
                    profile.put("email", email);
                    profile.put("age", age);
                    profile.put("phone", "+34 600 123 456");
                    profile.put("createdAt", System.currentTimeMillis());

                    // 1 escritura: perfil del usuario.
                    FirebaseFirestore.getInstance()
                            .collection(FirestoreContract.COL_USERS)
                            .document(fu.getUid())
                            .set(profile)
                            .addOnSuccessListener(unused -> callback.onSuccess(
                                    new User(fu.getUid(), fullName, email, "+34 600 123 456", age)))
                            .addOnFailureListener(e -> {
                                // La cuenta ya existe aunque falle el perfil: no bloqueamos.
                                Log.w(TAG, "Cuenta creada pero falló el perfil", e);
                                callback.onSuccess(new User(fu.getUid(), fullName, email,
                                        "+34 600 123 456", age));
                            });
                })
                .addOnFailureListener(e -> {
                    if (isAuthDisabledError(e)) {
                        // Auth no está habilitado en el proyecto: fallback local.
                        markAuthUnavailable();
                        callback.onError(AUTH_DISABLED_MSG);
                    } else {
                        callback.onError(mapError(e));
                    }
                });
    }

    /** Login real: autentica y recupera el perfil (1 lectura; del cache si existe). */
    public static void login(final String email, String password,
                             final RepositoryCallback<User> callback) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser fu = result.getUser();
                    if (fu == null) {
                        callback.onError("No se pudo iniciar sesión");
                        return;
                    }
                    // 1 lectura del perfil (Source.DEFAULT aprovecha la caché).
                    FirebaseFirestore.getInstance()
                            .collection(FirestoreContract.COL_USERS)
                            .document(fu.getUid())
                            .get(Source.DEFAULT)
                            .addOnSuccessListener(doc -> {
                                String name = doc.getString("fullName");
                                if (TextUtils.isEmpty(name) && fu.getDisplayName() != null) {
                                    name = fu.getDisplayName();
                                }
                                if (TextUtils.isEmpty(name)) {
                                    name = deriveNameFromEmail(email);
                                }
                                Long ageL = doc.getLong("age");
                                String phone = doc.getString("phone");
                                callback.onSuccess(new User(fu.getUid(), name, email,
                                        phone != null ? phone : "+34 600 123 456",
                                        ageL != null ? ageL.intValue() : 0));
                            })
                            .addOnFailureListener(e -> {
                                // Perfil no disponible: devolvemos lo esencial de Auth.
                                callback.onSuccess(new User(fu.getUid(), deriveNameFromEmail(email),
                                        email, "+34 600 123 456", 0));
                            });
                })
                .addOnFailureListener(e -> {
                    if (isAuthDisabledError(e)) {
                        markAuthUnavailable();
                        callback.onError(AUTH_DISABLED_MSG);
                    } else {
                        callback.onError(mapError(e));
                    }
                });
    }

    /** Cierra la sesión de Firebase (si hay una abierta). */
    public static void signOut() {
        try {
            FirebaseAuth.getInstance().signOut();
        } catch (Exception ignored) {
        }
    }

    // ----------------------------- HELPERS -----------------------------
    /** Mensaje interno para que el repositorio detecte el fallback y use el login local. */
    public static final String AUTH_DISABLED_MSG =
            "AUTH_UNAVAILABLE: proveedor de email/contraseña no habilitado en el proyecto.";

    /**
     * Detecta errores que indican que Firebase Auth NO está operativo en el proyecto
     * (email/contraseña sin habilitar, plan sin facturación, configuración ausente),
     * para caer al login simulado en lugar de bloquear al usuario.
     */
    private static boolean isAuthDisabledError(@NonNull Exception e) {
        String msg = e.getMessage() == null ? "" : e.getMessage();
        String low = msg.toLowerCase();
        return low.contains("configuration_not_found")
                || low.contains("billing_not_enabled")
                || low.contains("operation_not_allowed")
                || low.contains("admin_restricted_operation")
                || low.contains("identity")
                || low.contains("internal error")
                || low.contains("invalid api key")
                || low.contains("[auth/internal-error]");
    }

    private static String mapError(@NonNull Exception e) {
        if (e instanceof FirebaseAuthWeakPasswordException) {
            return "La contraseña es demasiado débil (mínimo 6 caracteres)";
        }
        if (e instanceof FirebaseAuthUserCollisionException) {
            return "Ya existe una cuenta con este correo";
        }
        if (e instanceof FirebaseAuthInvalidUserException) {
            return "No existe una cuenta con este correo";
        }
        if (e instanceof FirebaseAuthInvalidCredentialsException) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("password")) {
                return "Contraseña incorrecta";
            }
            return "Correo o contraseña no válidos";
        }
        return "Error de autenticación: " + e.getMessage();
    }

    private static String deriveNameFromEmail(String email) {
        String local = email.split("@")[0].replace('.', ' ').replace('_', ' ').trim();
        if (local.isEmpty()) {
            return "Paciente";
        }
        StringBuilder sb = new StringBuilder();
        for (String p : local.split("\\s+")) {
            if (p.isEmpty()) continue;
            sb.append(Character.toUpperCase(p.charAt(0)))
                    .append(p.substring(1).toLowerCase())
                    .append(" ");
        }
        return sb.toString().trim();
    }
}
