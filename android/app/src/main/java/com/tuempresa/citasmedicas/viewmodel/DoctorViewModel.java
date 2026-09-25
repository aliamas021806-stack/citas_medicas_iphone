package com.tuempresa.citasmedicas.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.tuempresa.citasmedicas.data.firebase.FirebaseManager;
import com.tuempresa.citasmedicas.data.firebase.FirestoreDoctorRepository;
import com.tuempresa.citasmedicas.data.repository.DoctorRepository;
import com.tuempresa.citasmedicas.data.repository.RepositoryCallback;
import com.tuempresa.citasmedicas.model.Doctor;
import com.tuempresa.citasmedicas.model.Specialty;
import com.tuempresa.citasmedicas.model.TimeSlot;
import com.tuempresa.citasmedicas.util.Resource;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel que expone especialidades, doctores y horarios disponibles.
 *
 * <p>Fuente de datos conmutable: si Firebase está disponible ({@code google-services.json}
 * presente), usa {@link FirestoreDoctorRepository} (optimizado en costos); en caso
 * contrario cae al {@link DoctorRepository} local (mock/Retrofit) para poder
 * ejecutar en la PC sin credenciales. La UI no cambia.
 */
public class DoctorViewModel extends ViewModel {

    private final DoctorRepository localRepository = new DoctorRepository();
    private final FirestoreDoctorRepository firestoreRepository = new FirestoreDoctorRepository();

    private final MutableLiveData<Resource<List<Specialty>>> specialties = new MutableLiveData<>();
    private final MutableLiveData<Resource<List<Doctor>>> doctors = new MutableLiveData<>();
    private final MutableLiveData<Resource<List<TimeSlot>>> slots = new MutableLiveData<>();

    private List<Doctor> allDoctors = new ArrayList<>();

    public LiveData<Resource<List<Specialty>>> getSpecialties() {
        return specialties;
    }

    public LiveData<Resource<List<Doctor>>> getDoctors() {
        return doctors;
    }

    public LiveData<Resource<List<TimeSlot>>> getSlots() {
        return slots;
    }

    public void loadHome() {
        loadSpecialties();
        loadDoctors();
    }

    public void loadSpecialties() {
        specialties.setValue(Resource.loading());
        RepositoryCallback<List<Specialty>> cb = new RepositoryCallback<List<Specialty>>() {
            @Override
            public void onSuccess(List<Specialty> data) {
                specialties.setValue(Resource.success(data));
            }

            @Override
            public void onError(String message) {
                specialties.setValue(Resource.error(message));
            }
        };
        if (FirebaseManager.isAvailable()) {
            firestoreRepository.getSpecialties(cb);
        } else {
            localRepository.getSpecialties(cb);
        }
    }

    public void loadDoctors() {
        doctors.setValue(Resource.loading());
        RepositoryCallback<List<Doctor>> cb = new RepositoryCallback<List<Doctor>>() {
            @Override
            public void onSuccess(List<Doctor> data) {
                allDoctors = data != null ? data : new ArrayList<>();
                doctors.setValue(Resource.success(allDoctors));
            }

            @Override
            public void onError(String message) {
                doctors.setValue(Resource.error(message));
            }
        };
        if (FirebaseManager.isAvailable()) {
            firestoreRepository.getDoctors(cb);
        } else {
            localRepository.getDoctors(cb);
        }
    }

    public void loadDoctorsBySpecialty(String specialtyId) {
        doctors.setValue(Resource.loading());
        RepositoryCallback<List<Doctor>> cb = new RepositoryCallback<List<Doctor>>() {
            @Override
            public void onSuccess(List<Doctor> data) {
                allDoctors = data != null ? data : new ArrayList<>();
                doctors.setValue(Resource.success(allDoctors));
            }

            @Override
            public void onError(String message) {
                doctors.setValue(Resource.error(message));
            }
        };
        if (FirebaseManager.isAvailable()) {
            firestoreRepository.getDoctorsBySpecialty(specialtyId, cb);
        } else {
            localRepository.getDoctorsBySpecialty(specialtyId, cb);
        }
    }

    public void filterDoctors(String query) {
        if (query == null || query.trim().isEmpty()) {
            doctors.setValue(Resource.success(allDoctors));
            return;
        }
        String q = query.toLowerCase().trim();
        List<Doctor> filtered = new ArrayList<>();
        for (Doctor d : allDoctors) {
            if (d.getFullName().toLowerCase().contains(q)
                    || d.getSpecialtyName().toLowerCase().contains(q)) {
                filtered.add(d);
            }
        }
        doctors.setValue(Resource.success(filtered));
    }

    public void loadSlots(String doctorId) {
        slots.setValue(Resource.loading());
        RepositoryCallback<List<TimeSlot>> cb = new RepositoryCallback<List<TimeSlot>>() {
            @Override
            public void onSuccess(List<TimeSlot> data) {
                slots.setValue(Resource.success(data));
            }

            @Override
            public void onError(String message) {
                slots.setValue(Resource.error(message));
            }
        };
        if (FirebaseManager.isAvailable()) {
            firestoreRepository.getAvailableSlots(doctorId, cb);
        } else {
            localRepository.getAvailableSlots(doctorId, cb);
        }
    }
}
