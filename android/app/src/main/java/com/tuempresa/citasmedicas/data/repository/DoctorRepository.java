package com.tuempresa.citasmedicas.data.repository;

import android.os.Handler;
import android.os.Looper;

import com.tuempresa.citasmedicas.data.remote.ApiClient;
import com.tuempresa.citasmedicas.data.remote.MedicalApiService;
import com.tuempresa.citasmedicas.model.Doctor;
import com.tuempresa.citasmedicas.model.Specialty;
import com.tuempresa.citasmedicas.model.TimeSlot;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repositorio de doctores y especialidades.
 * <p>
 * Consume {@link MedicalApiService}, cuyas respuestas están simuladas por el
 * MockInterceptor. Toda la asincronía se reenvía al hilo principal.
 */
public class DoctorRepository {

    private final MedicalApiService api;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public DoctorRepository() {
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

    public void getSpecialties(RepositoryCallback<List<Specialty>> callback) {
        enqueue(api.getSpecialties(), callback);
    }

    public void getDoctors(RepositoryCallback<List<Doctor>> callback) {
        enqueue(api.getDoctors(), callback);
    }

    public void getDoctorsBySpecialty(String specialtyId, RepositoryCallback<List<Doctor>> callback) {
        enqueue(api.getDoctorsBySpecialty(specialtyId), callback);
    }

    public void getDoctorById(String doctorId, RepositoryCallback<Doctor> callback) {
        enqueue(api.getDoctorById(doctorId), callback);
    }

    public void getAvailableSlots(String doctorId, RepositoryCallback<List<TimeSlot>> callback) {
        enqueue(api.getSlots(doctorId), callback);
    }
}
