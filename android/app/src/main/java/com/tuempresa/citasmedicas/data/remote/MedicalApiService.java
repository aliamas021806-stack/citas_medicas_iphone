package com.tuempresa.citasmedicas.data.remote;

import com.tuempresa.citasmedicas.model.Doctor;
import com.tuempresa.citasmedicas.model.Patient;
import com.tuempresa.citasmedicas.model.Specialty;
import com.tuempresa.citasmedicas.model.TimeSlot;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Definición de la API REST (Retrofit).
 * <p>
 * En un backend real apuntaría a un servidor; aquí las respuestas las
 * intercepta {@link MockInterceptor}, que devuelve JSON sintético (generado
 * por IA). Incluye los endpoints de PACIENTES con su información y EDAD.
 */
public interface MedicalApiService {

    // ----------------------- Catálogo médico -----------------------
    @GET("specialties")
    Call<List<Specialty>> getSpecialties();

    @GET("doctors")
    Call<List<Doctor>> getDoctors();

    @GET("doctors/specialty/{specialtyId}")
    Call<List<Doctor>> getDoctorsBySpecialty(@Path("specialtyId") String specialtyId);

    @GET("doctors/{doctorId}")
    Call<Doctor> getDoctorById(@Path("doctorId") String doctorId);

    @GET("slots/{doctorId}")
    Call<List<TimeSlot>> getSlots(@Path("doctorId") String doctorId);

    // ----------------------- Pacientes (info + EDAD) -----------------------
    @GET("patients")
    Call<List<Patient>> getPatients();

    @GET("patients/{patientId}")
    Call<Patient> getPatientById(@Path("patientId") String patientId);
}
