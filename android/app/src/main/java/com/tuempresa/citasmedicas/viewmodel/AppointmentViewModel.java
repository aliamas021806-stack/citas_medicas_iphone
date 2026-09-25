package com.tuempresa.citasmedicas.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.tuempresa.citasmedicas.data.local.AppointmentEntity;
import com.tuempresa.citasmedicas.data.repository.AppointmentRepository;
import com.tuempresa.citasmedicas.data.repository.RepositoryCallback;
import com.tuempresa.citasmedicas.model.Appointment;
import com.tuempresa.citasmedicas.util.Resource;

import java.util.List;

/**
 * ViewModel de citas médicas. Observa Room (SQLite) y gestiona crear/cancelar.
 */
public class AppointmentViewModel extends AndroidViewModel {

    private final AppointmentRepository repository;

    private final LiveData<List<AppointmentEntity>> appointments;
    private final MutableLiveData<Resource<Void>> actionState = new MutableLiveData<>();
    /**
     * Evento de un solo disparo que indica que la cita se creó correctamente.
     * Se usa Boolean (no Void) porque un LiveData<Void> emite siempre {@code null}
     * y un observador con {@code if (value != null)} nunca se activaría.
     */
    private final MutableLiveData<Boolean> navigateBack = new MutableLiveData<>();

    public AppointmentViewModel(@NonNull Application application) {
        super(application);
        repository = new AppointmentRepository(application);
        appointments = repository.observeAll();
        // Sincroniza UNA vez desde Firestore (1 lectura acotada, sin listeners)
        // hacia la caché local Room que alimenta la UI.
        repository.syncFromRemote(USER_ID);
    }

    /** Id de usuario de la sesión de demo (login simulado). */
    private static final String USER_ID = "user_01";

    /** Vuelve a sincronizar desde Firestore (p. ej. al hacer "swipe to refresh"). */
    public void refresh() {
        repository.syncFromRemote(USER_ID);
    }

    public LiveData<List<AppointmentEntity>> getAppointments() {
        return appointments;
    }

    public LiveData<Resource<Void>> getActionState() {
        return actionState;
    }

    public LiveData<Boolean> getNavigateBack() {
        return navigateBack;
    }

    public void createAppointment(Appointment appointment) {
        actionState.setValue(Resource.loading());
        repository.create(appointment, new RepositoryCallback<Long>() {
            @Override
            public void onSuccess(Long data) {
                actionState.setValue(Resource.success(null));
                // true = éxito de creación (evento observable por la Activity)
                navigateBack.setValue(Boolean.TRUE);
            }

            @Override
            public void onError(String message) {
                actionState.setValue(Resource.error(message));
            }
        });
    }

    public void cancelAppointment(long appointmentId) {
        actionState.setValue(Resource.loading());
        repository.cancel(appointmentId, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                actionState.setValue(Resource.success(null));
            }

            @Override
            public void onError(String message) {
                actionState.setValue(Resource.error(message));
            }
        });
    }

    public void clearActionState() {
        actionState.setValue(null);
    }

    /** Consume el evento de navegación para evitar que se repita (p. ej. al rotar). */
    public void clearNavigateBack() {
        navigateBack.setValue(Boolean.FALSE);
    }
}
