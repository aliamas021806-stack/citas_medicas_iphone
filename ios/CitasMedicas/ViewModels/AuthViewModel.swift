//
//  AuthViewModel.swift
//  CitasMedicas
//
//  ViewModel de autenticación (MVVM). Gestiona login y registro, exponiendo
//  un estado `Resource<User>` consumible por la vista.
//

import Foundation
import Combine

@MainActor
final class AuthViewModel: ObservableObject {

    @Published private(set) var authState: Resource<User>? = nil
    @Published var isRegisterMode = false

    // Campos del formulario (bind con la vista).
    @Published var fullName = ""
    @Published var email = ""
    @Published var password = ""
    @Published var ageText = ""

    private let repository: AuthRepository
    private let session: SessionManager

    nonisolated init(repository: AuthRepository = AuthRepository(),
                     session: SessionManager = .shared) {
        self.repository = repository
        self.session = session
    }

    var isLoading: Bool { authState?.isLoading ?? false }
    var errorMessage: String? {
        guard let state = authState, state.isError else { return nil }
        return state.message
    }

    func toggleMode() {
        isRegisterMode.toggle()
        resetState()
    }

    /// Valida y ejecuta la acción correspondiente (login o registro).
    func submit() {
        if isRegisterMode {
            register()
        } else {
            login()
        }
    }

    private func login() {
        authState = .loading()
        Task {
            do {
                let user = try await repository.login(
                    request: AuthRequest(email: email, password: password))
                session.saveSession(user)
                authState = .success(user)
            } catch {
                authState = .error(error.localizedDescription)
            }
        }
    }

    private func register() {
        authState = .loading()
        let age = Int(ageText) ?? 0
        Task {
            do {
                let user = try await repository.register(
                    request: AuthRequest(fullName: fullName, email: email,
                                         password: password, age: age))
                session.saveSession(user)
                authState = .success(user)
            } catch {
                authState = .error(error.localizedDescription)
            }
        }
    }

    func resetState() {
        authState = nil
    }
}
