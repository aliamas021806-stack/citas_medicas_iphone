package com.tuempresa.citasmedicas.data.remote;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.tuempresa.citasmedicas.data.mock.MockData;
import com.tuempresa.citasmedicas.model.Doctor;
import com.tuempresa.citasmedicas.model.Patient;
import com.tuempresa.citasmedicas.model.Specialty;
import com.tuempresa.citasmedicas.model.TimeSlot;

import java.io.IOException;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Interceptor OkHttp que simula un servidor REST real.
 * <p>
 * Inspecciona la ruta solicitada por {@link MedicalApiService} y devuelve el
 * JSON correspondiente generado a partir de {@link MockData} (doctores,
 * especialidades, horarios y pacientes con su EDAD).
 */
public class MockInterceptor implements Interceptor {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private final Gson gson = new Gson();

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        String path = request.url().encodedPath();
        String json = route(path);
        return buildResponse(request, json);
    }

    private String route(String path) {
        // /specialties
        if (path.endsWith("specialties")) {
            List<Specialty> specialties = MockData.getSpecialties();
            return gson.toJson(specialties);
        }

        // /patients/{id}
        if (path.contains("patients/") && !path.endsWith("patients")) {
            String id = path.substring(path.lastIndexOf("patients/") + "patients/".length());
            Patient patient = MockData.getPatientById(id);
            return gson.toJson(patient);
        }

        // /patients
        if (path.endsWith("patients")) {
            List<Patient> patients = MockData.getPatients();
            return gson.toJson(patients);
        }

        // /doctors/specialty/{id}
        if (path.contains("doctors/specialty/")) {
            String id = path.substring(path.indexOf("doctors/specialty/") + "doctors/specialty/".length());
            return gson.toJson(MockData.getDoctorsBySpecialty(id));
        }

        // /doctors/{id}
        if (path.contains("doctors/") && !path.endsWith("doctors")) {
            String id = path.substring(path.lastIndexOf("doctors/") + "doctors/".length());
            Doctor doctor = MockData.getDoctorById(id);
            return gson.toJson(doctor);
        }

        // /slots/{doctorId}
        if (path.contains("slots/")) {
            String id = path.substring(path.lastIndexOf("slots/") + "slots/".length());
            List<TimeSlot> slots = MockData.getAvailableSlots(id);
            return gson.toJson(slots);
        }

        // /doctors
        if (path.endsWith("doctors")) {
            return gson.toJson(MockData.getDoctors());
        }

        // Fallback
        return "[]";
    }

    private Response buildResponse(Request request, String json) {
        return new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK (mock)")
                .body(ResponseBody.create(json, JSON))
                .addHeader("content-type", "application/json")
                .build();
    }
}
