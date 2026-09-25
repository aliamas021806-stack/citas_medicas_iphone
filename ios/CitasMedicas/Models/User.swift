//
//  User.swift
//  CitasMedicas
//
//  Usuario (paciente) del flujo de autenticación simulado.
//  Incluye la EDAD como campo de primera clase.
//

import Foundation

struct User: Codable, Identifiable, Equatable {
    var id: String
    var fullName: String
    var email: String
    var phone: String
    var age: Int

    init(id: String = "user_01",
         fullName: String,
         email: String,
         phone: String = "+34 600 123 456",
         age: Int = 0) {
        self.id = id
        self.fullName = fullName
        self.email = email
        self.phone = phone
        self.age = age
    }

    /// Iniciales para avatar. Ej: "Laura Jiménez" -> "LJ"
    var initials: String {
        let parts = fullName.trimmingCharacters(in: .whitespacesAndNewlines)
            .split(separator: " ")
            .map(String.init)
        guard let first = parts.first, let firstChar = first.first else { return "?" }
        var result = String(firstChar).uppercased()
        if let last = parts.last, parts.count > 1, let lastChar = last.first {
            result += String(lastChar).uppercased()
        }
        return result
    }

    static let placeholder = User(fullName: "Paciente", email: "")
}
