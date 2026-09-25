//
//  CitasMedicasApp.swift
//  CitasMedicas
//
//  Punto de entrada de la aplicación. Inyecta el contenedor de dependencias
//  y decide la pantalla inicial (Login o Main) según la sesión persistida.
//

import SwiftUI

@main
struct CitasMedicasApp: App {

    @StateObject private var env = AppEnvironment()

    var body: some Scene {
        WindowGroup {
            RootView()
                .environmentObject(env)
                .environmentObject(env.appointments)
                .environmentObject(env.doctors)
                .tint(AppColor.primary)
                .preferredColorScheme(.light)
        }
    }
}

/// Decide la rama inicial según exista sesión activa.
struct RootView: View {

    @EnvironmentObject private var env: AppEnvironment
    @State private var loggedIn: Bool = SessionManager.shared.isLoggedIn

    var body: some View {
        Group {
            if loggedIn {
                MainTabView(onLogout: { loggedIn = false })
                    .transition(.opacity)
            } else {
                LoginView(onLoggedIn: { loggedIn = true })
                    .transition(.opacity)
            }
        }
        .animation(.easeInOut(duration: 0.3), value: loggedIn)
    }
}
