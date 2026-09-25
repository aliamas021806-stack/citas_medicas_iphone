package com.tuempresa.citasmedicas.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.tuempresa.citasmedicas.data.firebase.FirebaseManager;
import com.tuempresa.citasmedicas.data.firebase.FirestoreAppointmentRepository;
import com.tuempresa.citasmedicas.data.local.AppDatabase;
import com.tuempresa.citasmedicas.data.local.AppointmentDao;
import com.tuempresa.citasmedicas.data.local.AppointmentEntity;
import com.tuempresa.citasmedicas.model.Appointment;
import com.tuempresa.citasmedicas.util.AppointmentMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Repositorio de citas médicas.
 *
 * <p><b>Estrategia elegida (local-first + Firestore)</b>:
 * <ul>
 *   <li>La UI observa SIEMPRE Room (LiveData): lecturas locales gratuitas y
 *       funcionamiento offline. No hay listeners de Firestore alimentando la
 *       lista, por lo que no se consumen cuotas de lectura en tiempo real.</li>
 *   <li>Cada escritura (crear/cancelar) se guarda en Room y, si Firebase está
 *       disponible, se REPLICA con 1 sola escritura en Firestore.</li>
 *   <li>La sincronización al abrir la pantalla se hace con 1 lectura acotada por
 *       {@code limit(PAGE_SIZE)} y sin listeners.</li>
 * </ul>
 */
public class AppointmentRepository {

    private static final String TAG = "AppointmentRepository";

    private final AppointmentDao dao;
    private final FirestoreAppointmentRepository remote;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public AppointmentRepository(Context context) {
        this.dao = AppDatabase.getInstance(context).appointmentDao();
        this.remote = FirebaseManager.isAvailable()
                ? new FirestoreAppointmentRepository() : null;
    }

    public LiveData<List<AppointmentEntity>> observeAll() {
        return dao.observeAll();
    }

    /**
     * Sincroniza las citas del usuario desde Firestore hacia Room.
     * Coste: 1 consulta acotada (limit). Se ejecuta a demanda (p. ej. al abrir
     * "Mis Citas"), nunca en un bucle ni con listeners.
     */
    public void syncFromRemote(String userId) {
        if (remote == null) {
            return;
        }
        remote.getUserAppointments(userId, new RepositoryCallback<List<Appointment>>() {
            @Override
            public void onSuccess(List<Appointment> data) {
                List<AppointmentEntity> entities = new ArrayList<>();
                if (data != null) {
                    for (Appointment a : data) {
                        entities.add(AppointmentMapper.toEntity(a, a.getId()));
                    }
                }
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    // Reemplaza sólo las filas remotas; conserva las locales.
                    dao.clearRemote();
                    if (!entities.isEmpty()) {
                        dao.insertAll(entities);
                    }
                });
            }

            @Override
            public void onError(String message) {
                // Silencioso: la UI ya tiene datos de Room; no bloqueamos al usuario.
                Log.w(TAG, "No se pudo sincronizar citas desde Firestore: " + message);
            }
        });
    }

    public void create(Appointment appointment, RepositoryCallback<Long> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                AppointmentEntity entity = AppointmentMapper.toEntity(appointment);
                entity.id = 0;
                long newId = dao.insert(entity);

                // Réplica en Firestore (1 escritura) fuera del hilo principal.
                if (remote != null) {
                    remote.create(appointment, resolveUserId(appointment),
                            new RepositoryCallback<String>() {
                                @Override
                                public void onSuccess(String remoteId) {
                                    AppDatabase.databaseWriteExecutor.execute(() -> {
                                        AppointmentEntity row = dao.getById(newId);
                                        if (row != null) {
                                            row.remoteId = remoteId;
                                            dao.insert(row);
                                        }
                                    });
                                }

                                @Override
                                public void onError(String message) {
                                    Log.w(TAG, "Cita guardada en local, pero falló la nube: " + message);
                                }
                            });
                }

                mainHandler.post(() -> callback.onSuccess(newId));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("No se pudo agendar la cita: " + e.getMessage()));
            }
        });
    }

    public void cancel(long appointmentId, RepositoryCallback<Void> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                AppointmentEntity entity = dao.getById(appointmentId);
                dao.updateStatus(appointmentId, "Cancelled");

                if (remote != null && entity != null && entity.remoteId != null) {
                    remote.cancel(entity.remoteId, new RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void data) {
                        }

                        @Override
                        public void onError(String message) {
                            Log.w(TAG, "Cancelada en local, pero falló la nube: " + message);
                        }
                    });
                }

                mainHandler.post(() -> callback.onSuccess(null));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("No se pudo cancelar la cita: " + e.getMessage()));
            }
        });
    }

    public void delete(AppointmentEntity entity, RepositoryCallback<Void> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                dao.delete(entity);
                mainHandler.post(() -> callback.onSuccess(null));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("No se pudo eliminar la cita"));
            }
        });
    }

    private String resolveUserId(Appointment appointment) {
        // El id de usuario de sesión se fija en "user_01" (demo con login simulado).
        return "user_01";
    }
}
