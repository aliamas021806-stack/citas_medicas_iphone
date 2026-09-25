//
//  Appointment.swift
//  CitasMedicas
//
//  Modelo de dominio de una cita médica local.
//

import Foundation

/// Estado de una cita médica.
enum AppointmentStatus: String, Codable, CaseIterable, Identifiable {
    case confirmed = "Confirmed"
    case pending   = "Pending"
    case cancelled = "Cancelled"

    var id: String { rawValue }

    /// Etiqueta en español para la UI.
    var displayName: String {
        switch self {
        case .confirmed: return "Confirmada"
        case .pending:   return "Pendiente"
        case .cancelled: return "Cancelada"
        }
    }

    init(value: String?) {
        self = AppointmentStatus(rawValue: value ?? "") ?? .confirmed
    }
}

struct Appointment: Codable, Identifiable, Equatable {
    var id: String
    var doctorId: String
    var doctorName: String
    var specialtyName: String
    var patientName: String
    var patientAge: Int
    var date: String        // "yyyy-MM-dd"
    var time: String        // "HH:mm"
    var status: AppointmentStatus
    var reason: String
    var createdAt: Date

    init(id: String = UUID().uuidString,
         doctorId: String,
         doctorName: String,
         specialtyName: String,
         patientName: String,
         patientAge: Int,
         date: String,
         time: String,
         status: AppointmentStatus = .confirmed,
         reason: String = "",
         createdAt: Date = Date()) {
        self.id = id
        self.doctorId = doctorId
        self.doctorName = doctorName
        self.specialtyName = specialtyName
        self.patientName = patientName
        self.patientAge = patientAge
        self.date = date
        self.time = time
        self.status = status
        self.reason = reason
        self.createdAt = createdAt
    }

    /// Una cita es cancelable sólo si aún no está cancelada.
    var isCancellable: Bool {
        status == .confirmed || status == .pending
    }

    /// Fecha como Date (útil para ordenar y formatear).
    var dateValue: Date? { MockData.dateFormatter.date(from: date) }

    /// Rango horario legible, p. ej. "10:00 – 11:00".
    var timeRange: String {
        guard let hour = Int(time.prefix(2)) else { return time }
        let endHour = min(hour + 1, 23)
        return String(format: "%02d:00 – %02d:00", hour, endHour)
    }
}
