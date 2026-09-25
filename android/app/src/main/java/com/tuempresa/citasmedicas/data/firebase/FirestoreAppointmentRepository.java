package com.tuempresa.citasmedicas.data.firebase;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.tuempresa.citasmedicas.data.repository.RepositoryCallback;
import com.tuempresa.citasmedicas.model.Appointment;
import com.tuempresa.citasmedicas.model.AppointmentStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Repositorio de citas respaldado por Firestore.
 *
 * <p><b>Optimización de costos</b>:
 * <ul>
 *   <li>Lecturas puntuales con {@code get()} + {@code limit()} (sin listeners).</li>
 *   <li>Filtro por {@code userId} (una sola igualdad) y orden en MEMORIA, para
 *       no requerir índices compuestos ni paginar con saltos.</li>
 *   <li>Crear/cancelar = 1 escritura por operación. Cancelar usa
 *       {@code update()} sobre un campo (más barato y seguro que reescribir el
 *       documento entero) y, en modo local, se difiere hasta la reconexión.</li>
 * </ul>
 */
public class FirestoreAppointmentRepository {

    private static final String TAG = "FirestoreApptRepo";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /** Citas de un usuario. Orden descendente por fecha de creación (en memoria). */
    public void getUserAppointments(String userId,
                                    final RepositoryCallback<List<Appointment>> callback) {
        db.collection(FirestoreContract.COL_APPOINTMENTS)
                .whereEqualTo(FirestoreContract.F_USER_ID, userId)
                .limit(FirestoreContract.PAGE_SIZE)
                .get(Source.DEFAULT)
                .addOnSuccessListener(snap -> {
                    List<Appointment> list = map(snap);
                    Collections.sort(list,
                            (a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> fail("citas", e, callback));
    }

    /** Crea una cita (1 escritura). Devuelve el id del documento creado. */
    public void create(Appointment appointment, String userId,
                       final RepositoryCallback<String> callback) {
        DocumentReference ref = db.collection(FirestoreContract.COL_APPOINTMENTS).document();
        ref.set(FirestoreMapper.fromAppointment(appointment, userId))
                .addOnSuccessListener(unused -> callback.onSuccess(ref.getId()))
                .addOnFailureListener(e -> fail("crear cita", e, callback));
    }

    /** Cancela una cita cambiando SÓLO el campo status (1 escritura mínima). */
    public void cancel(String appointmentId, final RepositoryCallback<Void> callback) {
        db.collection(FirestoreContract.COL_APPOINTMENTS)
                .document(appointmentId)
                .update(FirestoreContract.F_STATUS, AppointmentStatus.CANCELLED.getValue())
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> fail("cancelar cita", e, callback));
    }

    // ----------------------------- HELPERS -----------------------------
    private List<Appointment> map(QuerySnapshot snap) {
        List<Appointment> list = new ArrayList<>();
        for (QueryDocumentSnapshot doc : snap) {
            list.add(FirestoreMapper.toAppointment(doc));
        }
        return list;
    }

    private <T> void fail(String what, Exception e, RepositoryCallback<T> callback) {
        Log.e(TAG, "Error en " + what, e);
        callback.onError("No se pudo completar (" + what + "): " + e.getMessage());
    }
}
