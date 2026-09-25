package com.tuempresa.citasmedicas.util;

import com.tuempresa.citasmedicas.data.local.AppointmentEntity;
import com.tuempresa.citasmedicas.model.Appointment;
import com.tuempresa.citasmedicas.model.AppointmentStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapeador entre la entidad de Room y el modelo de dominio (incluye edad del paciente).
 */
public final class AppointmentMapper {

    private AppointmentMapper() {
    }

    public static Appointment toModel(AppointmentEntity e) {
        if (e == null) {
            return null;
        }
        Appointment a = new Appointment();
        a.setId(String.valueOf(e.id));
        a.setDoctorId(e.doctorId);
        a.setDoctorName(e.doctorName);
        a.setSpecialtyName(e.specialtyName);
        a.setPatientName(e.patientName);
        a.setPatientAge(e.patientAge);
        a.setDate(e.date);
        a.setTime(e.time);
        a.setStatus(AppointmentStatus.fromValue(e.status));
        a.setReason(e.reason);
        a.setCreatedAt(e.createdAt);
        return a;
    }

    public static AppointmentEntity toEntity(Appointment a) {
        AppointmentEntity e = new AppointmentEntity();
        if (a.getId() != null && !a.getId().isEmpty()) {
            try {
                e.id = Long.parseLong(a.getId());
            } catch (NumberFormatException ignored) {
                // id autogenerado
            }
        }
        e.doctorId = a.getDoctorId();
        e.doctorName = a.getDoctorName();
        e.specialtyName = a.getSpecialtyName();
        e.patientName = a.getPatientName();
        e.patientAge = a.getPatientAge();
        e.date = a.getDate();
        e.time = a.getTime();
        e.status = a.getStatus() != null ? a.getStatus().getValue() : AppointmentStatus.CONFIRMED.getValue();
        e.reason = a.getReason();
        e.createdAt = a.getCreatedAt() > 0 ? a.getCreatedAt() : System.currentTimeMillis();
        return e;
    }

    /** Copia la entidad asignando su id remoto de Firestore. */
    public static AppointmentEntity withRemoteId(AppointmentEntity e, String remoteId) {
        if (e != null) {
            e.remoteId = remoteId;
        }
        return e;
    }

    /** Convierte una cita de Firestore en entidad Room (para sincronizar). */
    public static AppointmentEntity toEntity(Appointment a, String remoteId) {
        AppointmentEntity e = toEntity(a);
        e.id = 0;
        e.remoteId = remoteId;
        return e;
    }

    public static List<Appointment> toModelList(List<AppointmentEntity> entities) {
        List<Appointment> result = new ArrayList<>();
        if (entities != null) {
            for (AppointmentEntity e : entities) {
                result.add(toModel(e));
            }
        }
        return result;
    }
}
