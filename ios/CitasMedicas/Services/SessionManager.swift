//
//  SessionManager.swift
//  CitasMedicas
//
//  Gestiona la sesión del usuario (login simulado) con persistencia local
//  mediante UserDefaults. Almacena también la EDAD del paciente.
//

import Foundation

final class SessionManager {

    static let shared = SessionManager()
    private init() {}

    private let defaults = UserDefaults.standard

    private enum Key {
        static let logged = "cm_logged_in"
        static let user = "cm_user"
    }

    private let encoder = JSONEncoder()
    private let decoder = JSONDecoder()

    /// `true` si hay una sesión activa.
    var isLoggedIn: Bool {
        defaults.bool(forKey: Key.logged) && currentUser != nil
    }

    /// Usuario en sesión, o nil si no hay sesión.
    var currentUser: User? {
        guard let data = defaults.data(forKey: Key.user) else { return nil }
        return try? decoder.decode(User.self, from: data)
    }

    /// Guarda la sesión del usuario.
    func saveSession(_ user: User) {
        if let data = try? encoder.encode(user) {
            defaults.set(data, forKey: Key.user)
            defaults.set(true, forKey: Key.logged)
        }
    }

    /// Cierra la sesión (manteniendo las citas en el dispositivo).
    func logout() {
        defaults.removeObject(forKey: Key.user)
        defaults.set(false, forKey: Key.logged)
    }
}
