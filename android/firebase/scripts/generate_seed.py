#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
generate_seed.py
================
Genera los JSON de datos sintéticos de prueba (compatible con el seeder de la
app Android y con el script upload_seed.py).

Salidas (en ../seed/):
    specialties.json   -> 8 especialidades
    doctors.json       -> 10 doctores (con edad, tarifa, hospital...)
    slots.json         -> horarios de los próximos 7 días de cada doctor
    appointments.json  -> citas precargadas de ejemplo
    users.json         -> usuario demo (perfil del paciente)

Los datos coinciden EXACTAMENTE con MockData.java para que la app muestre lo
mismo en modo local y en modo Firebase.

Ejecutar:  python3 generate_seed.py
"""
import json
import os
from datetime import date, timedelta

HERE = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.normpath(os.path.join(HERE, "..", "seed"))

HOURS = ["08:00", "09:00", "10:00", "11:00", "12:00",
         "15:00", "16:00", "17:00", "18:00"]


def specialties():
    return [
        {"id": "esp_01", "name": "Cardiología", "iconEmoji": "\U0001FAC0",
         "description": "Diagnóstico y tratamiento de enfermedades del corazón", "doctorCount": 3},
        {"id": "esp_02", "name": "Pediatría", "iconEmoji": "\U0001F476",
         "description": "Atención médica integral para niños y adolescentes", "doctorCount": 2},
        {"id": "esp_03", "name": "Dermatología", "iconEmoji": "\U0001F9F4",
         "description": "Cuidado de la piel, cabello y uñas", "doctorCount": 2},
        {"id": "esp_04", "name": "Traumatología", "iconEmoji": "\U0001F9B4",
         "description": "Lesiones del sistema músculo-esquelético", "doctorCount": 2},
        {"id": "esp_05", "name": "Neurología", "iconEmoji": "\U0001F9E0",
         "description": "Trastornos del sistema nervioso central y periférico", "doctorCount": 2},
        {"id": "esp_06", "name": "Medicina General", "iconEmoji": "\U0001FA7A",
         "description": "Atención primaria y consulta general", "doctorCount": 3},
        {"id": "esp_07", "name": "Ginecología", "iconEmoji": "\U0001F338",
         "description": "Salud reproductiva y femenina", "doctorCount": 2},
        {"id": "esp_08", "name": "Oftalmología", "iconEmoji": "\U0001F441",
         "description": "Cuidado de la visión y enfermedades oculares", "doctorCount": 2},
    ]


def doctors():
    # (id, nombre, espId, nombreEsp, bio, rating, años_exp, edad, tarifa, hospital)
    raw = [
        ("doc_01", "Carlos Ramírez", "esp_01", "Cardiología",
         "Cardiólogo intervencionista con amplia experiencia en arritmias y prevención cardiovascular.",
         4.8, 15, 52, 45.0, "Hospital Central Vitalis"),
        ("doc_02", "María González", "esp_02", "Pediatría",
         "Pediatra dedicada al desarrollo infantil y vacunación. Paciente y cercana con los más pequeños.",
         4.9, 12, 44, 35.0, "Clínica Infantil Aurora"),
        ("doc_03", "Andrea López", "esp_03", "Dermatología",
         "Especialista en dermatología clínica y estética, tratamiento del acné y cuidado del melanoma.",
         4.7, 10, 39, 40.0, "Centro DermaSalud"),
        ("doc_04", "Javier Moreno", "esp_04", "Traumatología",
         "Traumatólogo especializado en lesiones deportivas y cirugía de rodilla.",
         4.6, 14, 47, 50.0, "Hospital Central Vitalis"),
        ("doc_05", "Lucía Fernández", "esp_05", "Neurología",
         "Neuróloga con enfoque en epilepsia, cefaleas y trastornos del movimiento.",
         4.9, 18, 55, 55.0, "Instituto NeuroVida"),
        ("doc_06", "Diego Torres", "esp_06", "Medicina General",
         "Médico general orientado a la prevención y seguimiento de enfermedades crónicas.",
         4.5, 8, 36, 25.0, "Centro Médico Familiar"),
        ("doc_07", "Sofía Herrera", "esp_07", "Ginecología",
         "Ginecóloga y obstetra, control prenatal y salud reproductiva de la mujer.",
         4.8, 13, 45, 42.0, "Clínica Mujer & Vida"),
        ("doc_08", "Pablo Castro", "esp_08", "Oftalmología",
         "Oftalmólogo especializado en cataratas, glaucoma y cirugía refractiva láser.",
         4.7, 16, 49, 48.0, "Instituto Ocular Visio"),
        ("doc_09", "Elena Ruiz", "esp_01", "Cardiología",
         "Cardióloga clínica enfocada en hipertensión y rehabilitación cardíaca.",
         4.6, 9, 38, 43.0, "Hospital del Corazón"),
        ("doc_10", "Miguel Ángel Díaz", "esp_06", "Medicina General",
         "Medicina general y urgencias, atención rápida y diagnóstico oportuno.",
         4.4, 6, 34, 22.0, "Centro Médico Familiar"),
    ]
    out = []
    for r in raw:
        out.append({
            "id": r[0], "fullName": r[1], "specialtyId": r[2],
            "specialtyName": r[3], "bio": r[4], "rating": r[5],
            "yearsExperience": r[6], "age": r[7],
            "consultationFee": r[8], "hospital": r[9], "photoUrl": None,
        })
    return out


def selectable_dates(days=10, limit=7):
    dates = []
    d = date.today()
    i = 0
    while len(dates) < limit and i < days:
        if d.weekday() != 6:  # 6 = domingo (Monday=0)
            dates.append(d.isoformat())
        d += timedelta(days=1)
        i += 1
    return dates


def slots(docs):
    out = []
    d = date.today()
    for day in range(7):
        if d.weekday() == 6:  # domingo: sin horarios
            d += timedelta(days=1)
            continue
        dstr = d.isoformat()
        for doc in docs:
            for i, hour in enumerate(HOURS):
                available = ((day * 7 + i) % 4) != 0
                sid = "slot_%s_%s_%s" % (doc["id"], dstr, hour.replace(":", ""))
                out.append({
                    "id": sid, "doctorId": doc["id"],
                    "date": dstr, "time": hour, "available": available,
                })
        d += timedelta(days=1)
    return out


def appointments():
    dates = selectable_dates()
    d0 = dates[0]
    d1 = dates[1] if len(dates) > 1 else d0
    d3 = dates[3] if len(dates) > 3 else d1
    return [
        {"userId": "user_01", "doctorId": "doc_01", "doctorName": "Carlos Ramírez",
         "specialtyName": "Cardiología", "patientName": "Laura Jiménez", "patientAge": 34,
         "date": d1, "time": "10:00", "status": "Confirmed",
         "reason": "Chequeo cardiológico anual", "createdAt": 1700000000000},
        {"userId": "user_01", "doctorId": "doc_02", "doctorName": "María González",
         "specialtyName": "Pediatría", "patientName": "Laura Jiménez", "patientAge": 34,
         "date": d3, "time": "16:00", "status": "Confirmed",
         "reason": "Control de crecimiento", "createdAt": 1700000000001},
        {"userId": "user_01", "doctorId": "doc_03", "doctorName": "Andrea López",
         "specialtyName": "Dermatología", "patientName": "Laura Jiménez", "patientAge": 34,
         "date": d0, "time": "09:00", "status": "Cancelled",
         "reason": "Revisión de lunar", "createdAt": 1700000000002},
    ]


def users():
    return [{
        "id": "user_01", "fullName": "Laura Jiménez",
        "email": "laura.jimenez@example.com", "phone": "+34 611 220 331", "age": 34,
    }]


def write(name, data):
    os.makedirs(OUT, exist_ok=True)
    path = os.path.join(OUT, name)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
    print("escrito %-20s (%d registros)" % (name, len(data)))


def main():
    docs = doctors()
    write("specialties.json", specialties())
    write("doctors.json", docs)
    write("slots.json", slots(docs))
    write("appointments.json", appointments())
    write("users.json", users())
    print("OK -> %s" % OUT)


if __name__ == "__main__":
    main()
