//
//  MockData.swift
//  CitasMedicas
//
//  Fuente de datos sintéticos (Mock Data) equivalente a MockData.java.
//  Simula la respuesta de un backend real: especialidades, doctores
//  (con EDAD), pacientes "generados por IA" y horarios disponibles.
//

import Foundation

final class MockData {

    static let shared = MockData()
    private init() {}

    // MARK: - Formateadores

    static let dateFormatter: DateFormatter = {
        let f = DateFormatter()
        f.dateFormat = "yyyy-MM-dd"
        f.locale = Locale(identifier: "en_US_POSIX")
        f.timeZone = .current
        return f
    }()

    // MARK: - Especialidades

    let specialties: [Specialty] = [
        Specialty(id: "esp_01", name: "Cardiología", iconEmoji: "🫀",
                  description: "Diagnóstico y tratamiento de enfermedades del corazón", doctorCount: 3),
        Specialty(id: "esp_02", name: "Pediatría", iconEmoji: "👶",
                  description: "Atención médica integral para niños y adolescentes", doctorCount: 2),
        Specialty(id: "esp_03", name: "Dermatología", iconEmoji: "🧴",
                  description: "Cuidado de la piel, cabello y uñas", doctorCount: 2),
        Specialty(id: "esp_04", name: "Traumatología", iconEmoji: "🦴",
                  description: "Lesiones del sistema músculo-esquelético", doctorCount: 2),
        Specialty(id: "esp_05", name: "Neurología", iconEmoji: "🧠",
                  description: "Trastornos del sistema nervioso central y periférico", doctorCount: 2),
        Specialty(id: "esp_06", name: "Medicina General", iconEmoji: "🩺",
                  description: "Atención primaria y consulta general", doctorCount: 3),
        Specialty(id: "esp_07", name: "Ginecología", iconEmoji: "🌸",
                  description: "Salud reproductiva y femenina", doctorCount: 2),
        Specialty(id: "esp_08", name: "Oftalmología", iconEmoji: "👁️",
                  description: "Cuidado de la visión y enfermedades oculares", doctorCount: 2)
    ]

    func specialty(byId id: String) -> Specialty? {
        specialties.first { $0.id == id }
    }

    // MARK: - Doctores (con EDAD)

    let doctors: [Doctor] = [
        Doctor(id: "doc_01", fullName: "Carlos Ramírez", specialtyId: "esp_01", specialtyName: "Cardiología",
               bio: "Cardiólogo intervencionista con amplia experiencia en arritmias y prevención cardiovascular.",
               rating: 4.8, yearsExperience: 15, age: 52, consultationFee: 45.0,
               hospital: "Hospital Central Vitalis", photoURL: nil),
        Doctor(id: "doc_02", fullName: "María González", specialtyId: "esp_02", specialtyName: "Pediatría",
               bio: "Pediatra dedicada al desarrollo infantil y vacunación. Paciente y cercana con los más pequeños.",
               rating: 4.9, yearsExperience: 12, age: 44, consultationFee: 35.0,
               hospital: "Clínica Infantil Aurora", photoURL: nil),
        Doctor(id: "doc_03", fullName: "Andrea López", specialtyId: "esp_03", specialtyName: "Dermatología",
               bio: "Especialista en dermatología clínica y estética, tratamiento del acné y cuidado del melanoma.",
               rating: 4.7, yearsExperience: 10, age: 39, consultationFee: 40.0,
               hospital: "Centro DermaSalud", photoURL: nil),
        Doctor(id: "doc_04", fullName: "Javier Moreno", specialtyId: "esp_04", specialtyName: "Traumatología",
               bio: "Traumatólogo especializado en lesiones deportivas y cirugía de rodilla.",
               rating: 4.6, yearsExperience: 14, age: 47, consultationFee: 50.0,
               hospital: "Hospital Central Vitalis", photoURL: nil),
        Doctor(id: "doc_05", fullName: "Lucía Fernández", specialtyId: "esp_05", specialtyName: "Neurología",
               bio: "Neuróloga con enfoque en epilepsia, cefaleas y trastornos del movimiento.",
               rating: 4.9, yearsExperience: 18, age: 55, consultationFee: 55.0,
               hospital: "Instituto NeuroVida", photoURL: nil),
        Doctor(id: "doc_06", fullName: "Diego Torres", specialtyId: "esp_06", specialtyName: "Medicina General",
               bio: "Médico general orientado a la prevención y seguimiento de enfermedades crónicas.",
               rating: 4.5, yearsExperience: 8, age: 36, consultationFee: 25.0,
               hospital: "Centro Médico Familiar", photoURL: nil),
        Doctor(id: "doc_07", fullName: "Sofía Herrera", specialtyId: "esp_07", specialtyName: "Ginecología",
               bio: "Ginecóloga y obstetra, control prenatal y salud reproductiva de la mujer.",
               rating: 4.8, yearsExperience: 13, age: 45, consultationFee: 42.0,
               hospital: "Clínica Mujer & Vida", photoURL: nil),
        Doctor(id: "doc_08", fullName: "Pablo Castro", specialtyId: "esp_08", specialtyName: "Oftalmología",
               bio: "Oftalmólogo especializado en cataratas, glaucoma y cirugía refractiva láser.",
               rating: 4.7, yearsExperience: 16, age: 49, consultationFee: 48.0,
               hospital: "Instituto Ocular Visio", photoURL: nil),
        Doctor(id: "doc_09", fullName: "Elena Ruiz", specialtyId: "esp_01", specialtyName: "Cardiología",
               bio: "Cardióloga clínica enfocada en hipertensión y rehabilitación cardíaca.",
               rating: 4.6, yearsExperience: 9, age: 38, consultationFee: 43.0,
               hospital: "Hospital del Corazón", photoURL: nil),
        Doctor(id: "doc_10", fullName: "Miguel Ángel Díaz", specialtyId: "esp_06", specialtyName: "Medicina General",
               bio: "Medicina general y urgencias, atención rápida y diagnóstico oportuno.",
               rating: 4.4, yearsExperience: 6, age: 34, consultationFee: 22.0,
               hospital: "Centro Médico Familiar", photoURL: nil)
    ]

    func doctors(bySpecialty specialtyId: String) -> [Doctor] {
        doctors.filter { $0.specialtyId == specialtyId }
    }

    func doctor(byId id: String) -> Doctor? {
        doctors.first { $0.id == id }
    }

    // MARK: - Pacientes (datos sintéticos "generados por IA")

    let patients: [Patient] = [
        Patient(id: "pat_01", fullName: "Laura Jiménez", email: "laura.jimenez@example.com",
                phone: "+34 611 220 331", age: 34, bloodType: "O+", gender: "Femenino",
                address: "Calle Mayor 12, Madrid", insurance: "SaludPlus",
                allergies: "Penicilina", chronicConditions: "Ninguna",
                heightCm: 165, weightKg: 62, emergencyContact: "Jorge Jiménez"),
        Patient(id: "pat_02", fullName: "Andrés Molina", email: "andres.molina@example.com",
                phone: "+34 622 331 442", age: 47, bloodType: "A+", gender: "Masculino",
                address: "Av. Libertad 88, Sevilla", insurance: "VidaTotal",
                allergies: "Ninguna", chronicConditions: "Hipertensión",
                heightCm: 178, weightKg: 85, emergencyContact: "María Molina"),
        Patient(id: "pat_03", fullName: "Carmen Delgado", email: "carmen.delgado@example.com",
                phone: "+34 633 442 553", age: 29, bloodType: "B-", gender: "Femenino",
                address: "Calle Sol 3, Valencia", insurance: "SaludPlus",
                allergies: "Polen", chronicConditions: "Asma leve",
                heightCm: 160, weightKg: 55, emergencyContact: "Luis Delgado"),
        Patient(id: "pat_04", fullName: "Roberto Sánchez", email: "roberto.sanchez@example.com",
                phone: "+34 644 553 664", age: 61, bloodType: "AB+", gender: "Masculino",
                address: "Paseo Mar 45, Málaga", insurance: "MediSeguro",
                allergies: "Ibuprofeno", chronicConditions: "Diabetes tipo 2",
                heightCm: 172, weightKg: 80, emergencyContact: "Ana Sánchez"),
        Patient(id: "pat_05", fullName: "Elena Navarro", email: "elena.navarro@example.com",
                phone: "+34 655 664 775", age: 38, bloodType: "O-", gender: "Femenino",
                address: "Calle Luna 7, Bilbao", insurance: "VidaTotal",
                allergies: "Ninguna", chronicConditions: "Ninguna",
                heightCm: 168, weightKg: 60, emergencyContact: "Pedro Navarro"),
        Patient(id: "pat_06", fullName: "Javier Ortega", email: "javier.ortega@example.com",
                phone: "+34 666 775 886", age: 52, bloodType: "A-", gender: "Masculino",
                address: "Ronda Norte 21, Zaragoza", insurance: "SaludPlus",
                allergies: "Mariscos", chronicConditions: "Colesterol alto",
                heightCm: 180, weightKg: 90, emergencyContact: "Rosa Ortega"),
        Patient(id: "pat_07", fullName: "Patricia Vega", email: "patricia.vega@example.com",
                phone: "+34 677 886 997", age: 42, bloodType: "B+", gender: "Femenino",
                address: "Av. Europa 5, Alicante", insurance: "MediSeguro",
                allergies: "Ninguna", chronicConditions: "Migraña crónica",
                heightCm: 163, weightKg: 58, emergencyContact: "Diego Vega"),
        Patient(id: "pat_08", fullName: "Fernando Lozano", email: "fernando.lozano@example.com",
                phone: "+34 688 997 108", age: 25, bloodType: "O+", gender: "Masculino",
                address: "Calle Río 9, Granada", insurance: "VidaTotal",
                allergies: "Látex", chronicConditions: "Ninguna",
                heightCm: 175, weightKg: 72, emergencyContact: "Sara Lozano"),
        Patient(id: "pat_09", fullName: "Isabel Ramírez", email: "isabel.ramirez@example.com",
                phone: "+34 699 108 219", age: 70, bloodType: "A+", gender: "Femenino",
                address: "Plaza Centro 2, Murcia", insurance: "SaludPlus",
                allergies: "Aspirina", chronicConditions: "Artrosis",
                heightCm: 158, weightKg: 65, emergencyContact: "Mario Ramírez"),
        Patient(id: "pat_10", fullName: "Tomás Iglesias", email: "tomas.iglesias@example.com",
                phone: "+34 610 219 320", age: 33, bloodType: "AB-", gender: "Masculino",
                address: "Camino Viejo 14, Valladolid", insurance: "MediSeguro",
                allergies: "Ninguna", chronicConditions: "Ninguna",
                heightCm: 183, weightKg: 78, emergencyContact: "Lucía Iglesias")
    ]

    func patient(byId id: String) -> Patient? {
        patients.first { $0.id == id }
    }

    /// Edad media de los pacientes (estadística generada).
    var averagePatientAge: Double {
        guard !patients.isEmpty else { return 0 }
        let sum = patients.reduce(0) { $0 + $1.age }
        return ((Double(sum) / Double(patients.count)) * 10).rounded() / 10
    }

    // MARK: - Horarios (slots)

    private let hours = ["08:00", "09:00", "10:00", "11:00", "12:00",
                          "15:00", "16:00", "17:00", "18:00"]

    /// Devuelve 7 días de slots (domingos excluidos), reproduciendo
    /// la lógica de MockData.getAvailableSlots.
    func availableSlots(for doctorId: String) -> [TimeSlot] {
        var slots: [TimeSlot] = []
        var cal = Calendar.current
        cal.timeZone = .current
        var dayCursor = cal.startOfDay(for: Date())

        var dayIndex = 0
        var createdDays = 0
        while createdDays < 7 {
            if cal.component(.weekday, from: dayCursor) == 1 { // Domingo
                dayCursor = cal.date(byAdding: .day, value: 1, to: dayCursor)!
                dayIndex += 1
                continue
            }
            let date = MockData.dateFormatter.string(from: dayCursor)
            for (i, hour) in hours.enumerated() {
                let available = ((dayIndex * 7 + i) % 4) != 0
                slots.append(TimeSlot(
                    id: "slot_\(doctorId)_\(date)_\(hour.replacingOccurrences(of: ":", with: ""))",
                    doctorId: doctorId,
                    date: date,
                    time: hour,
                    available: available))
            }
            dayCursor = cal.date(byAdding: .day, value: 1, to: dayCursor)!
            dayIndex += 1
            createdDays += 1
        }
        return slots
    }

    func slots(for doctorId: String, on date: String) -> [TimeSlot] {
        availableSlots(for: doctorId).filter { $0.date == date }
    }

    /// Fechas seleccionables (7 días laborables).
    func selectableDates() -> [Date] {
        var dates: [Date] = []
        var cal = Calendar.current
        cal.timeZone = .current
        var cursor = cal.startOfDay(for: Date())
        while dates.count < 7 {
            if cal.component(.weekday, from: cursor) != 1 { // no domingo
                dates.append(cursor)
            }
            cursor = cal.date(byAdding: .day, value: 1, to: cursor)!
        }
        return dates
    }
}
