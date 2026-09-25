package com.tuempresa.citasmedicas.data.firebase;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.tuempresa.citasmedicas.data.repository.RepositoryCallback;
import com.tuempresa.citasmedicas.model.Doctor;
import com.tuempresa.citasmedicas.model.Specialty;
import com.tuempresa.citasmedicas.model.TimeSlot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Repositorio de doctores/especialidades/horarios respaldado por Firestore.
 *
 * <p><b>Optimización de costos aplicada</b>:
 * <ul>
 *   <li>Sólo consultas de UNA sola lectura ({@code get()}), nunca listeners en
 *       tiempo real (no hay {@code addSnapshotListener} que deje sockets abiertos
 *       ni lecturas recurrentes).</li>
 *   <li>Todas las consultas llevan {@code limit(PAGE_SIZE)} para acotar el
 *       número máximo de documentos facturables.</li>
 *   <li>No se usan {@code orderBy()} combinados con {@code whereEqualTo()} para
 *       NO depender de índices compuestos: el orden se aplica en memoria.</li>
 *   <li>{@link Source#DEFAULT} aprovecha la caché offline (menos lecturas de red).</li>
 * </ul>
 */
public class FirestoreDoctorRepository {

    private static final String TAG = "FirestoreDoctorRepo";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void getSpecialties(final RepositoryCallback<List<Specialty>> callback) {
        db.collection(FirestoreContract.COL_SPECIALTIES)
                .limit(FirestoreContract.PAGE_SIZE)
                .get(Source.DEFAULT)
                .addOnSuccessListener(snap -> callback.onSuccess(mapSpecialties(snap)))
                .addOnFailureListener(e -> fail("especialidades", e, callback));
    }

    public void getDoctors(final RepositoryCallback<List<Doctor>> callback) {
        db.collection(FirestoreContract.COL_DOCTORS)
                .limit(FirestoreContract.PAGE_SIZE)
                .get(Source.DEFAULT)
                .addOnSuccessListener(snap -> callback.onSuccess(mapDoctors(snap)))
                .addOnFailureListener(e -> fail("doctores", e, callback));
    }

    public void getDoctorsBySpecialty(String specialtyId,
                                      final RepositoryCallback<List<Doctor>> callback) {
        db.collection(FirestoreContract.COL_DOCTORS)
                .whereEqualTo(FirestoreContract.F_SPECIALTY_ID, specialtyId)
                .limit(FirestoreContract.PAGE_SIZE)
                .get(Source.DEFAULT)
                .addOnSuccessListener(snap -> callback.onSuccess(mapDoctors(snap)))
                .addOnFailureListener(e -> fail("doctores por especialidad", e, callback));
    }

    public void getAvailableSlots(String doctorId,
                                  final RepositoryCallback<List<TimeSlot>> callback) {
        // Un doctor tiene ~9 horarios x hasta 7 días (~63). Acotamos con un límite
        // holgado pero finito para no leer más de la cuenta.
        db.collection(FirestoreContract.COL_SLOTS)
                .whereEqualTo(FirestoreContract.F_DOCTOR_ID, doctorId)
                .limit(120)
                .get(Source.DEFAULT)
                .addOnSuccessListener(snap -> {
                    List<TimeSlot> slots = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snap) {
                        slots.add(FirestoreMapper.toSlot(doc));
                    }
                    // Orden en memoria (fecha y hora) para evitar índices compuestos.
                    Collections.sort(slots, (a, b) -> {
                        int c = safe(a.getDate()).compareTo(safe(b.getDate()));
                        return c != 0 ? c : safe(a.getTime()).compareTo(safe(b.getTime()));
                    });
                    callback.onSuccess(slots);
                })
                .addOnFailureListener(e -> fail("horarios", e, callback));
    }

    // ----------------------------- HELPERS -----------------------------
    private List<Specialty> mapSpecialties(QuerySnapshot snap) {
        List<Specialty> list = new ArrayList<>();
        for (QueryDocumentSnapshot doc : snap) {
            list.add(FirestoreMapper.toSpecialty(doc));
        }
        return list;
    }

    private List<Doctor> mapDoctors(QuerySnapshot snap) {
        List<Doctor> list = new ArrayList<>();
        for (QueryDocumentSnapshot doc : snap) {
            list.add(FirestoreMapper.toDoctor(doc));
        }
        return list;
    }

    private String safe(@Nullable String s) {
        return s != null ? s : "";
    }

    private <T> void fail(String what, Exception e,
                          RepositoryCallback<T> callback) {
        Log.e(TAG, "Error cargando " + what, e);
        callback.onError("No se pudieron cargar " + what + ": " + e.getMessage());
    }
}
