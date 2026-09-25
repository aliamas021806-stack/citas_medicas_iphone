package com.tuempresa.citasmedicas.data.repository;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.tuempresa.citasmedicas.data.firebase.FirebaseAuthDataSource;
import com.tuempresa.citasmedicas.model.AuthRequest;
import com.tuempresa.citasmedicas.model.User;

import java.util.regex.Pattern;

/**
 * Repositorio de autenticación.
 *
 * <p><b>Doble modo</b>:
 * <ul>
 *   <li>Si Firebase está configurado ({@code google-services.json} presente), usa
 *       <b>Firebase Auth real</b> (email/password) — login/registro oficiales.</li>
 *   <li>Si no, cae al modo simulado local para poder compilar y probar en la PC
 *       sin credenciales (con un pequeño delay que imita la latencia de red).</li>
 * </ul>
 */
public class AuthRepository {

    private static final long FAKE_DELAY_MS = 900L;
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /** Login: Firebase Auth real si está disponible; si no, simulado. */
    public void login(AuthRequest request, RepositoryCallback<User> callback) {
        if (FirebaseAuthDataSource.isAuthUsable()) {
            if (!validateEmail(request.getEmail(), callback)) {
                return;
            }
            // Firebase Auth exige >= 6 caracteres; damos un mensaje claro.
            if (TextUtils.isEmpty(request.getPassword()) || request.getPassword().length() < 6) {
                callback.onError("La contraseña debe tener al menos 6 caracteres");
                return;
            }
            FirebaseAuthDataSource.login(request.getEmail(), request.getPassword(),
                    new RepositoryCallback<User>() {
                        @Override
                        public void onSuccess(User data) {
                            callback.onSuccess(data);
                        }

                        @Override
                        public void onError(String message) {
                            if (FirebaseAuthDataSource.AUTH_DISABLED_MSG.equals(message)) {
                                // Auth no habilitado en el proyecto: fallback local transparente.
                                loginSimulated(request, callback);
                            } else {
                                callback.onError(message);
                            }
                        }
                    });
            return;
        }
        loginSimulated(request, callback);
    }

    /** Registro: Firebase Auth real si está disponible; si no, simulado. */
    public void register(AuthRequest request, RepositoryCallback<User> callback) {
        if (FirebaseAuthDataSource.isAuthUsable()) {
            if (TextUtils.isEmpty(request.getFullName())
                    || request.getFullName().trim().length() < 3) {
                callback.onError("Ingresa tu nombre completo");
                return;
            }
            if (!validateEmail(request.getEmail(), callback)) {
                return;
            }
            if (TextUtils.isEmpty(request.getPassword()) || request.getPassword().length() < 6) {
                callback.onError("La contraseña debe tener al menos 6 caracteres");
                return;
            }
            if (request.getAge() < 1 || request.getAge() > 120) {
                callback.onError("Ingresa una edad válida (entre 1 y 120 años)");
                return;
            }
            FirebaseAuthDataSource.register(request.getFullName().trim(), request.getEmail(),
                    request.getPassword(), request.getAge(),
                    new RepositoryCallback<User>() {
                        @Override
                        public void onSuccess(User data) {
                            callback.onSuccess(data);
                        }

                        @Override
                        public void onError(String message) {
                            if (FirebaseAuthDataSource.AUTH_DISABLED_MSG.equals(message)) {
                                // Auth no habilitado: creamos la cuenta en modo local.
                                registerSimulated(request, callback);
                            } else {
                                callback.onError(message);
                            }
                        }
                    });
            return;
        }
        registerSimulated(request, callback);
    }

    // ----------------------------- LOCAL (sin Firebase) -----------------------------
    private void loginSimulated(AuthRequest request, final RepositoryCallback<User> callback) {
        mainHandler.postDelayed(() -> {
            if (!isEmailValid(request.getEmail())) {
                callback.onError("Ingresa un correo electrónico válido");
                return;
            }
            if (TextUtils.isEmpty(request.getPassword()) || request.getPassword().length() < 4) {
                callback.onError("La contraseña debe tener al menos 4 caracteres");
                return;
            }
            String name = deriveNameFromEmail(request.getEmail());
            int age = request.getAge() > 0 ? request.getAge() : 30;
            callback.onSuccess(new User("user_01", name, request.getEmail(), "+34 600 123 456", age));
        }, FAKE_DELAY_MS);
    }

    private void registerSimulated(AuthRequest request, final RepositoryCallback<User> callback) {
        mainHandler.postDelayed(() -> {
            if (TextUtils.isEmpty(request.getFullName())
                    || request.getFullName().trim().length() < 3) {
                callback.onError("Ingresa tu nombre completo");
                return;
            }
            if (!isEmailValid(request.getEmail())) {
                callback.onError("Ingresa un correo electrónico válido");
                return;
            }
            if (TextUtils.isEmpty(request.getPassword()) || request.getPassword().length() < 4) {
                callback.onError("La contraseña debe tener al menos 4 caracteres");
                return;
            }
            if (request.getAge() < 1 || request.getAge() > 120) {
                callback.onError("Ingresa una edad válida (entre 1 y 120 años)");
                return;
            }
            callback.onSuccess(new User("user_01", request.getFullName().trim(),
                    request.getEmail(), "+34 600 123 456", request.getAge()));
        }, FAKE_DELAY_MS);
    }

    private boolean validateEmail(String email, RepositoryCallback<User> callback) {
        if (TextUtils.isEmpty(email) || !EMAIL_PATTERN.matcher(email).matches()) {
            callback.onError("Ingresa un correo electrónico válido");
            return false;
        }
        return true;
    }

    private boolean isEmailValid(String email) {
        return !TextUtils.isEmpty(email) && EMAIL_PATTERN.matcher(email).matches();
    }

    private String deriveNameFromEmail(String email) {
        String local = email.split("@")[0];
        local = local.replace('.', ' ').replace('_', ' ').trim();
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
