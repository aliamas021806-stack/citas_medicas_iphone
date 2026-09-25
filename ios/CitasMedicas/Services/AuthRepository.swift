//
//  AuthRepository.swift
//  CitasMedicas
//
//  Repositorio de autenticación SIMULADO. Sin backend: valida credenciales
//  con reglas locales y responde tras un delay asíncrono para reproducir la
//  latencia de una API. Conserva la EDAD del paciente.
//

import Foundation

enum AuthError: LocalizedError {
    case invalidName
    case invalidEmail
    case shortPassword
    case invalidAge

    var errorDescription: String? {
        switch self {
        case .invalidName:    return "Ingresa tu nombre completo"
        case .invalidEmail:   return "Ingresa un correo electrónico válido"
        case .shortPassword:  return "La contraseña debe tener al menos 4 caracteres"
        case .invalidAge:     return "Ingresa una edad válida (entre 1 y 120 años)"
        }
    }
}

final class AuthRepository {

    private let fakeDelay: UInt64 = 900_000_000 // 0.9 s en nanosegundos

    private let emailPattern = #"^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$"#

    private func matchesEmail(_ email: String) -> Bool {
        email.range(of: emailPattern, options: .regularExpression) != nil
    }

    /// Login simulado: acepta email válido + contraseña de 4+ caracteres.
    func login(request: AuthRequest) async throws -> User {
        try? await Task.sleep(nanoseconds: fakeDelay)

        guard matchesEmail(request.email) else { throw AuthError.invalidEmail }
        guard request.password.count >= 4 else { throw AuthError.shortPassword }

        let name = deriveNameFromEmail(request.email)
        let age = request.age > 0 ? request.age : 30
        return User(id: "user_01", fullName: name, email: request.email,
                    phone: "+34 600 123 456", age: age)
    }

    /// Registro simulado: valida datos y devuelve el usuario creado (con edad).
    func register(request: AuthRequest) async throws -> User {
        try? await Task.sleep(nanoseconds: fakeDelay)

        let name = request.fullName?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        guard name.count >= 3 else { throw AuthError.invalidName }
        guard matchesEmail(request.email) else { throw AuthError.invalidEmail }
        guard request.password.count >= 4 else { throw AuthError.shortPassword }
        guard (1...120).contains(request.age) else { throw AuthError.invalidAge }

        return User(id: "user_01", fullName: name, email: request.email,
                    phone: "+34 600 123 456", age: request.age)
    }

    /// Deriva un nombre legible a partir del email ("ana.lopez@x.com" -> "Ana Lopez").
    private func deriveNameFromEmail(_ email: String) -> String {
        let local = email.split(separator: "@").first.map(String.init) ?? ""
        let cleaned = local.replacingOccurrences(of: ".", with: " ")
            .replacingOccurrences(of: "_", with: " ")
            .trimmingCharacters(in: .whitespacesAndNewlines)
        guard !cleaned.isEmpty else { return "Paciente" }
        return cleaned
            .split(separator: " ")
            .map { $0.prefix(1).uppercased() + $0.dropFirst().lowercased() }
            .joined(separator: " ")
    }
}
