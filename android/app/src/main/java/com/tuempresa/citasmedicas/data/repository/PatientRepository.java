package com.tuempresa.citasmedicas.data.repository;

import android.os.Handler;
import android.os.Looper;

import com.tuempresa.citasmedicas.data.remote.ApiClient;
import com.tuempresa.citasmedicas.data.remote.MedicalApiService;
import com.tuempresa.citasmedicas.model.Patient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repositorio de PACIENTES.
 * <p>
 * Obtiene la información del paciente (incluida su EDAD) desde la API mock,
 * cuyos datos son generados por IA en {@code MockData}.
 */
public class PatientRepository {

    private final MedicalApiService api;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public PatientRepository() {
        this.api = ApiClient.getApi();
    }

    private <T> void enqueue(Call<T> call, RepositoryCallback<T> callback) {
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> c, Response<T> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mainHandler.post(() -> callback.onSuccess(response.body()));
                } else {
                    mainHandler.post(() -> callback.onError("Respuesta inválida del servidor"));
                }
            }

            @Override
            public void onFailure(Call<T> c, Throwable t) {
                mainHandler.post(() -> callback.onError("Error de red: " + t.getMessage()));
            }
        });
    }

    public void getPatients(RepositoryCallback<List<Patient>> callback) {
        enqueue(api.getPatients(), callback);
    }

    public void getPatientById(String patientId, RepositoryCallback<Patient> callback) {
        enqueue(api.getPatientById(patientId), callback);
    }
}
