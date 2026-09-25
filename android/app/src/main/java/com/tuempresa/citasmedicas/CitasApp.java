package com.tuempresa.citasmedicas;

import android.app.Application;

import com.tuempresa.citasmedicas.data.firebase.FirebaseManager;

/**
 * Aplicación. Inicializa Firebase UNA vez al arrancar.
 *
 * <p>Si existe {@code google-services.json}, Firestore queda disponible y el
 * catálogo (especialidades/doctores/horarios) se sirve desde Firestore. Si no,
 * la app funciona igual usando los datos mock locales (modo PC/emulador sin
 * credenciales).
 */
public class CitasApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseManager.init(this);
    }
}
