//
//  Patient.swift
//  CitasMedicas
//
//  Información clínica completa de un paciente (datos sintéticos "generados
//  por IA"). Incluye la EDAD y campos clínicos adicionales.
//

import Foundation

struct Patient: Codable, Identifiable, Equatable {
    var id: String
    var fullName: String
    var email: String
    var phone: String
    var age: Int
    var bloodType: String
    var gender: String
    var address: String
    var insurance: String
    var allergies: String
    var chronicConditions: String
    var heightCm: Double
    var weightKg: Double
    var emergencyContact: String
    var createdAt: Date

    init(id: String,
         fullName: String,
         email: String,
         phone: String,
         age: Int,
         bloodType: String,
         gender: String,
         address: String,
         insurance: String,
         allergies: String,
         chronicConditions: String,
         heightCm: Double,
         weightKg: Double,
         emergencyContact: String,
         createdAt: Date = Date()) {
        self.id = id
        self.fullName = fullName
        self.email = email
        self.phone = phone
        self.age = age
        self.bloodType = bloodType
        self.gender = gender
        self.address = address
        self.insurance = insurance
        self.allergies = allergies
        self.chronicConditions = chronicConditions
        self.heightCm = heightCm
        self.weightKg = weightKg
        self.emergencyContact = emergencyContact
        self.createdAt = createdAt
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

    /// Índice de masa corporal (IMC) calculado.
    var bmi: Double {
        guard heightCm > 0 else { return 0 }
        let meters = heightCm / 100.0
        return ((weightKg / (meters * meters)) * 10).rounded() / 10
    }
}
