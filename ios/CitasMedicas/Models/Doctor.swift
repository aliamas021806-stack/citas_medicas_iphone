//
//  Doctor.swift
//  CitasMedicas
//
//  Profesional de la salud. Incluye la EDAD como campo expuesto por la API mock.
//

import Foundation

struct Doctor: Codable, Identifiable, Equatable, Hashable {
    var id: String
    var fullName: String
    var specialtyId: String
    var specialtyName: String
    var bio: String
    var rating: Double
    var yearsExperience: Int
    var age: Int
    var consultationFee: Double
    var hospital: String
    var photoURL: String?

    /// Prefijo del título profesional (Dr./Dra.) según el nombre.
    var displayName: String {
        let firstName = fullName.split(separator: " ").first.map(String.init) ?? ""
        let female = firstName.lowercased().hasSuffix("a")
        return (female ? "Dra. " : "Dr. ") + fullName
    }

    var initials: String {
        let parts = fullName.split(separator: " ").map(String.init)
        guard let first = parts.first, let firstChar = first.first else { return "?" }
        var result = String(firstChar).uppercased()
        if let last = parts.last, parts.count > 1, let lastChar = last.first {
            result += String(lastChar).uppercased()
        }
        return result
    }

    /// Tarifa de consulta formateada en EUR.
    var formattedFee: String {
        let formatter = NumberFormatter()
        formatter.numberStyle = .currency
        formatter.currencyCode = "EUR"
        formatter.locale = Locale(identifier: "es_ES")
        return formatter.string(from: NSNumber(value: consultationFee)) ?? "\(consultationFee) €"
    }
}
