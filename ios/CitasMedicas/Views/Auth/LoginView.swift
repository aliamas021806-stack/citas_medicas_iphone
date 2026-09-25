//
//  LoginView.swift
//  CitasMedicas
//
//  Pantalla de Login / Registro simulado. Adapta el flujo de LoginActivity
//  (Android) a SwiftUI, con validación y estado de carga.
//

import SwiftUI

/// Pin de una píldora/servicio destacado (sobre el logo).
private struct BrandPill: View {
    let icon: String
    let text: String
    var body: some View {
        HStack(spacing: 6) {
            Image(systemName: icon).font(.system(size: 13, weight: .semibold))
            Text(text).font(AppFont.labelMD())
        }
        .foregroundStyle(AppColor.primaryTextOnSoft)
        .padding(.horizontal, 12).padding(.vertical, 7)
        .background(AppColor.primarySoft)
        .clipShape(Capsule())
    }
}

struct LoginView: View {

    var onLoggedIn: () -> Void

    @EnvironmentObject private var env: AppEnvironment
    @StateObject private var viewModel: AuthViewModel

    init(onLoggedIn: @escaping () -> Void) {
        self.onLoggedIn = onLoggedIn
        // Se reemplaza en .onAppear con el repositorio del entorno;
        // inicialización segura por defecto.
        _viewModel = StateObject(wrappedValue: AuthViewModel())
    }

    var body: some View {
        ZStack {
            AppColor.background.ignoresSafeArea()

            ScrollView {
                VStack(spacing: AppSpacing.lg) {
                    header
                    formCard
                    footerToggle
                }
                .padding(.horizontal, AppSpacing.margin)
                .padding(.top, AppSpacing.xl)
                .padding(.bottom, AppSpacing.lg)
            }
            .scrollDismissesKeyboard(.interactively)
        }
        .onChange(of: viewModel.authState?.isSuccess) { _, success in
            if success == true { onLoggedIn() }
        }
    }

    // MARK: - Header

    private var header: some View {
        VStack(spacing: AppSpacing.md) {
            ZStack {
                Circle().fill(AppColor.primarySoft).frame(width: 96, height: 96)
                Image(systemName: "cross.case.fill")
                    .font(.system(size: 40, weight: .semibold))
                    .foregroundStyle(AppColor.primary)
                Circle()
                    .fill(AppColor.mint)
                    .frame(width: 26, height: 26)
                    .overlay(
                        Image(systemName: "plus")
                            .font(.system(size: 13, weight: .bold))
                            .foregroundStyle(.white)
                    )
                    .offset(x: 30, y: -30)
            }
            .padding(.top, 8)

            VStack(spacing: 6) {
                Text("MediCare")
                    .font(AppFont.headlineXL())
                    .foregroundStyle(AppColor.textPrimary)
                Text(viewModel.isRegisterMode ? "Crea tu cuenta de paciente"
                                              : "Tu salud, a un toque de distancia")
                    .bodyMD(AppColor.textMuted)
                    .multilineTextAlignment(.center)
            }

            HStack(spacing: AppSpacing.sm) {
                BrandPill(icon: "checkmark.seal.fill", text: "Clínica Central")
                BrandPill(icon: "clock.fill", text: "Citas online")
            }
        }
    }

    // MARK: - Formulario

    private var formCard: some View {
        VStack(alignment: .leading, spacing: AppSpacing.md) {
            Text(viewModel.isRegisterMode ? "Crear cuenta" : "Iniciar sesión")
                .headlineLGMobile()

            if viewModel.isRegisterMode {
                FormField(label: "Nombre completo", placeholder: "Ej. Laura Jiménez",
                          text: $viewModel.fullName, icon: "person.fill",
                          capitalization: .words)
            }

            FormField(label: "Correo electrónico", placeholder: "tucorreo@ejemplo.com",
                      text: $viewModel.email, icon: "envelope.fill",
                      keyboard: .emailAddress, capitalization: .never)

            FormField(label: "Contraseña", placeholder: "Mínimo 4 caracteres",
                      text: $viewModel.password, icon: "lock.fill", isSecure: true)

            if viewModel.isRegisterMode {
                FormField(label: "Edad", placeholder: "Ej. 34", text: $viewModel.ageText,
                          icon: "calendar", keyboard: .numberPad)
            }

            if let error = viewModel.errorMessage {
                HStack(spacing: 8) {
                    Image(systemName: "exclamationmark.circle.fill")
                    Text(error).font(AppFont.labelMD())
                }
                .foregroundStyle(AppColor.error)
                .padding(10)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(AppColor.errorContainer.opacity(0.5))
                .clipShape(RoundedRectangle(cornerRadius: AppRadius.sm, style: .continuous))
            }

            PrimaryButton(
                title: viewModel.isRegisterMode ? "Registrarme" : "Entrar",
                icon: viewModel.isRegisterMode ? "person.badge.plus" : "arrow.right",
                isLoading: viewModel.isLoading,
                isEnabled: !viewModel.email.isEmpty && !viewModel.password.isEmpty
            ) {
                viewModel.submit()
            }
            .padding(.top, 4)
        }
        .padding(AppSpacing.md)
        .background(AppColor.surface)
        .clipShape(RoundedRectangle(cornerRadius: AppRadius.lg, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: AppRadius.lg, style: .continuous)
                .stroke(AppColor.border, lineWidth: 1)
        )
        .shadow(color: AppShadow.card.color, radius: AppShadow.card.radius,
                x: AppShadow.card.x, y: AppShadow.card.y)
    }

    // MARK: - Toggle

    private var footerToggle: some View {
        HStack(spacing: 4) {
            Text(viewModel.isRegisterMode ? "¿Ya tienes cuenta?" : "¿No tienes cuenta?")
                .bodyMD(AppColor.textMuted)
            Button {
                withAnimation(.easeInOut(duration: 0.2)) { viewModel.toggleMode() }
            } label: {
                Text(viewModel.isRegisterMode ? "Inicia sesión" : "Regístrate")
                    .font(AppFont.labelLG(AppColor.primary))
            }
        }
    }
}
