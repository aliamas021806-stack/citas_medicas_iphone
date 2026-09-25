package com.tuempresa.citasmedicas.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.tuempresa.citasmedicas.data.repository.PatientRepository;
import com.tuempresa.citasmedicas.data.repository.RepositoryCallback;
import com.tuempresa.citasmedicas.model.Patient;
import com.tuempresa.citasmedicas.util.Resource;

import java.util.List;

/**
 * ViewModel del PACIENTE. Carga la información del paciente (incluida su EDAD)
 * desde la API mock de datos generados por IA.
 */
public class PatientViewModel extends ViewModel {

    private final PatientRepository repository = new PatientRepository();

    private final MutableLiveData<Resource<Patient>> patient = new MutableLiveData<>();
    private final MutableLiveData<Resource<List<Patient>>> patients = new MutableLiveData<>();

    public LiveData<Resource<Patient>> getPatient() {
        return patient;
    }

    public LiveData<Resource<List<Patient>>> getPatients() {
        return patients;
    }

    public void loadPatient(String patientId) {
        patient.setValue(Resource.loading());
        repository.getPatientById(patientId, new RepositoryCallback<Patient>() {
            @Override
            public void onSuccess(Patient data) {
                patient.setValue(Resource.success(data));
            }

            @Override
            public void onError(String message) {
                patient.setValue(Resource.error(message));
            }
        });
    }

    public void loadPatients() {
        patients.setValue(Resource.loading());
        repository.getPatients(new RepositoryCallback<List<Patient>>() {
            @Override
            public void onSuccess(List<Patient> data) {
                patients.setValue(Resource.success(data));
            }

            @Override
            public void onError(String message) {
                patients.setValue(Resource.error(message));
            }
        });
    }
}
