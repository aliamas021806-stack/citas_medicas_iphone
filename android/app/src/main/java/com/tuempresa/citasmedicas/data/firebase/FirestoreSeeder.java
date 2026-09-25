package com.tuempresa.citasmedicas.data.firebase;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.tuempresa.citasmedicas.data.mock.MockData;
import com.tuempresa.citasmedicas.data.repository.RepositoryCallback;
import com.tuempresa.citasmedicas.model.Appointment;
import com.tuempresa.citasmedicas.model.AppointmentStatus;
import com.tuempresa.citasmedicas.model.Doctor;
import com.tuempresa.citasmedicas.model.Specialty;
import com.tuempresa.citasmedicas.model.TimeSlot;

import java.util.List;

/**
 * Siembra idempotente de datos sintéticos en Firestore.
 *
 * <p><b>Estrategia anti-coste (cuenta demo/gratuita)</b>:
 * <ol>
 *   <li>Lee UNA sola vez el documento centinela {@code metadata/seed}. Si ya
 *       tiene la versión actual, NO escribe nada (0 escrituras).</li>
 *   <li>Si hay que sembrar, agrupa TODAS las escrituras en un único
 *       {@link WriteBatch} (máx. ~500 ops). Así la siembra completa consume
 *       ~1 lectura + 1 operación de escritura por documento en un solo commit.</li>
 *   <li>Nunca se ejecuta dentro de bucles anidados ni en cada arranque.</li>
 * </ol>
 * Llama a {@link #seedIfNeeded(RepositoryCallback)} UNA vez tras el login.
 */
public final class FirestoreSeeder {

    private static final String TAG = "FirestoreSeeder";
    /** Sube esta versión cuando cambies los datos semilla para forzar re-siembra. */
    public static final int SEED_VERSION = 1;

    private FirestoreSeeder() {
    }

    public static void seedIfNeeded(final RepositoryCallback<Boolean> callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference seedRef =
                db.collection(FirestoreContract.COL_METADATA).document(FirestoreContract.DOC_SEED);

        // ---- 1 lectura ----
        seedRef.get()
                .addOnSuccessListener(doc -> {
                    Long version = doc.exists()
                            ? doc.getLong("version") : null;
                    if (version != null && version >= SEED_VERSION) {
                        Log.i(TAG, "Datos ya sembrados (v" + version + "). No se escribe nada.");
                        if (callback != null) {
                            callback.onSuccess(false);
                        }
                        return;
                    }
                    writeSeed(db, seedRef, callback);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "No se pudo verificar la bandera de siembra", e);
                    if (callback != null) {
                        callback.onError("No se pudo verificar la siembra: " + e.getMessage());
                    }
                });
    }

    private static void writeSeed(FirebaseFirestore db, DocumentReference seedRef,
                                  RepositoryCallback<Boolean> callback) {
        WriteBatch batch = db.batch();
        int ops = 0;

        // Especialidades
        List<Specialty> specialties = MockData.getSpecialties();
        for (Specialty s : specialties) {
            batch.set(db.collection(FirestoreContract.COL_SPECIALTIES).document(s.getId()),
                    FirestoreMapper.fromSpecialty(s));
            ops++;
        }

        // Doctores
        List<Doctor> doctors = MockData.getDoctors();
        for (Doctor d : doctors) {
            batch.set(db.collection(FirestoreContract.COL_DOCTORS).document(d.getId()),
                    FirestoreMapper.fromDoctor(d));
            ops++;
        }

        // Horarios (sólo los próximos 7 días de cada doctor; suficiente para demo)
        int slotCount = 0;
        for (Doctor d : doctors) {
            for (TimeSlot t : MockData.getAvailableSlots(d.getId())) {
                batch.set(db.collection(FirestoreContract.COL_SLOTS).document(t.getId()),
                        FirestoreMapper.fromSlot(t));
                ops++;
                slotCount++;
            }
        }

        // Citas precargadas (mismo ids que usa el login simulado de demo)
        String userId = "user_01";
        List<Appointment> seeds = MockData.getSeedAppointments();
        for (Appointment a : seeds) {
            DocumentReference ref = db.collection(FirestoreContract.COL_APPOINTMENTS).document();
            batch.set(ref, FirestoreMapper.fromAppointment(a, userId));
            ops++;
        }

        // Bandera de siembra (misma transacción = 0 lecturas extra)
        java.util.Map<String, Object> meta = new java.util.HashMap<>();
        meta.put("version", SEED_VERSION);
        meta.put("specialtiesSeeded", specialties.size());
        meta.put("doctorsSeeded", doctors.size());
        meta.put("slotsSeeded", slotCount);
        meta.put("appointmentsSeeded", seeds.size());
        meta.put("seededAt", System.currentTimeMillis());
        batch.set(seedRef, meta);
        ops++;

        final int totalOps = ops;
        batch.commit()
                .addOnSuccessListener(unused -> {
                    Log.i(TAG, "Siembra completada. Operaciones agrupadas: " + totalOps);
                    if (callback != null) {
                        callback.onSuccess(true);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Fallo al sembrar datos", e);
                    if (callback != null) {
                        callback.onError("No se pudieron sembrar los datos: " + e.getMessage());
                    }
                });
    }
}
