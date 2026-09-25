//
//  AuthRequest.swift
//  CitasMedicas
//
//  Credenciales para login / registro simulado. Incluye la edad del paciente.
//

import Foundation

struct AuthRequest {
    var fullName: String?
    var email: String
    var password: String
    var age: Int

    init(email: String, password: String) {
        self.fullName = nil
        self.email = email
        self.password = password
        self.age = 0
    }

    init(fullName: String, email: String, password: String, age: Int) {
        self.fullName = fullName
        self.email = email
        self.password = password
        self.age = age
    }
}
