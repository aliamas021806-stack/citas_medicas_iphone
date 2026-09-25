//
//  TimeSlot.swift
//  CitasMedicas
//
//  Horario (slot) disponible para un doctor.
//  Formato: date -> "yyyy-MM-dd", time -> "HH:mm".
//

import Foundation

struct TimeSlot: Codable, Identifiable, Equatable, Hashable {
    var id: String
    var doctorId: String
    var date: String
    var time: String
    var available: Bool

    /// Periodo del día derivado de la hora (para agrupar el picker).
    var period: SlotPeriod {
        guard let hour = Int(time.prefix(2)) else { return .morning }
        return hour < 14 ? .morning : .afternoon
    }

    /// `date` convertido a Date (zona local) para mostrarlo en la interfaz.
    var dateValue: Date? {
        MockData.dateFormatter.date(from: date)
    }
}

enum SlotPeriod: String, CaseIterable, Identifiable {
    case morning = "Turno Mañana"
    case afternoon = "Turno Tarde"

    var id: String { rawValue }
    var icon: String { self == .morning ? "sun.max" : "sunset" }
}
