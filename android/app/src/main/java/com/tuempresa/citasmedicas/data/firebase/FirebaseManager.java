package com.tuempresa.citasmedicas.data.firebase;

import android.content.Context;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.PersistentCacheSettings;

/**
 * Punto único de inicialización de Firebase.
 *
 * <p>La app funciona en DOS modos:
 * <ul>
 *   <li><b>Firebase disponible</b>: si se ha añadido {@code app/google-services.json}
 *       (o se ha inicializado manualmente con {@link FirebaseApp#initializeApp}), se
 *       usa Firestore/Auth como fuente de datos real.</li>
 *   <li><b>Modo local</b>: si no hay credenciales, la app cae automáticamente en la
 *       capa mock/Room existente para poder compilar y probar en la PC sin Firebase.</li>
 * </ul>
 *
 * <p><b>Optimización de costos</b>: se habilita la caché OFFLINE de Firestore
 * (memoria + disco). Esto reduce muchísimas lecturas de red porque los datos ya
 * vistos se sirven desde el dispositivo en lugar de volver a facturarse.
 */
public final class FirebaseManager {

    private static final String TAG = "FirebaseManager";
    private static boolean initialised = false;
    private static boolean available = false;

    private FirebaseManager() {
    }

    /** Inicializa Firebase una sola vez. Devuelve {@code true} si quedó operativo. */
    public static synchronized boolean init(Context context) {
        if (initialised) {
            return available;
        }
        initialised = true;
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                // Si existe google-services.json, esto lo inicializa automáticamente.
                FirebaseApp.initializeApp(context);
            }
            if (FirebaseApp.getApps(context).isEmpty()) {
                Log.w(TAG, "Firebase no configurado: se usará el modo local (mock/Room).");
                available = false;
                return false;
            }

            // Caché offline: memoria (rapidísima) + disco (persistente).
            // CLAVE para reducir lecturas facturables.
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setLocalCacheSettings(
                            PersistentCacheSettings.newBuilder().build())
                    .build();
            FirebaseFirestore.getInstance().setFirestoreSettings(settings);
            available = true;
            Log.i(TAG, "Firebase inicializado correctamente (Firestore con caché offline).");
        } catch (Exception e) {
            Log.e(TAG, "Error inicializando Firebase, se usará el modo local", e);
            available = false;
        }
        return available;
    }

    public static boolean isAvailable() {
        return available;
    }
}
