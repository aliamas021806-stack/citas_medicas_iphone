//
//  ProfileView.swift
//  CitasMedicas
//
//  Perfil del paciente: datos personales, información clínica y cierre de
//  sesión. Reproduce el ProfileFragment de Android con la EDAD destacada.
//

import SwiftUI

struct ProfileView: View {

    var onLogout: () -> Void

    @EnvironmentObject private var env: AppEnvironment
    @EnvironmentObject private var appointmentVM: AppointmentViewModel
    @State private var showLogoutConfirm = false

    private var user: User { env.session.currentUser ?? .placeholder }

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: AppSpacing.md) {
                    profileHeader
                    statsRow
                    personalInfoCard
                    menuCard
                }
                .padding(.horizontal, AppSpacing.margin)
                .padding(.top, AppSpacing.md)
                .padding(.bottom, 120)
            }
            .background(AppColor.background)
            .navigationTitle("Mi Perfil")
            .navigationBarTitleDisplayMode(.large)
        }
        .confirmationDialog("¿Cerrar sesión?", isPresented: $showLogoutConfirm, titleVisibility: .visible) {
            Button("Cerrar sesión", role: .destructive) {
                env.session.logout()
                onLogout()
            }
            Button("Cancelar", role: .cancel) {}
        }
    }

    private var profileHeader: some View {
        VStack(spacing: AppSpacing.sm) {
            AvatarView(initials: user.initials, size: 88, tint: AppColor.primary, showOnlineDot: true)
            Text(user.fullName).headlineLGMobile()
            Text(user.email).bodySM()
            HStack(spacing: 6) {
                Image(systemName: "checkmark.seal.fill").font(.system(size: 12))
                Text("Paciente verificado").font(AppFont.labelMD())
            }
            .foregroundStyle(AppColor.confirmed)
            .padding(.horizontal, 12).padding(.vertical, 5)
            .background(AppColor.confirmedFill)
            .clipShape(Capsule())
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, AppSpacing.md)
        .cardStyle()
    }

    private var statsRow: some View {
        HStack(spacing: AppSpacing.sm) {
            statBox(value: "\(appointmentVM.appointments.count)", label: "Citas totales", icon: "calendar")
            statBox(value: "\(user.age)", label: "Edad", icon: "birthday.cake.fill")
            statBox(value: "\(appointmentVM.confirmedCount)", label: "Activas", icon: "checkmark.circle.fill")
        }
    }

    private func statBox(value: String, label: String, icon: String) -> some View {
        VStack(spacing: 6) {
            Image(systemName: icon).font(.system(size: 16, weight: .semibold))
                .foregroundStyle(AppColor.primary)
            Text(value).font(AppFont.headlineLGMobile()).monospacedDigit()
            Text(label).labelSM()
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, AppSpacing.sm)
        .background(AppColor.surface)
        .clipShape(RoundedRectangle(cornerRadius: AppRadius.md, style: .continuous))
        .overlay(RoundedRectangle(cornerRadius: AppRadius.md, style: .continuous)
            .stroke(AppColor.border, lineWidth: 1))
    }

    private var personalInfoCard: some View {
        VStack(alignment: .leading, spacing: AppSpacing.sm) {
            Text("Información personal").headlineMD()
            infoRow(icon: "person.fill", label: "Nombre completo", value: user.fullName)
            Divider().overlay(AppColor.border)
            infoRow(icon: "envelope.fill", label: "Correo", value: user.email)
            Divider().overlay(AppColor.border)
            infoRow(icon: "phone.fill", label: "Teléfono", value: user.phone)
            Divider().overlay(AppColor.border)
            infoRow(icon: "calendar", label: "Edad", value: user.age > 0 ? "\(user.age) años" : "No especificada")
        }
        .cardStyle()
    }

    private func infoRow(icon: String, label: String, value: String) -> some View {
        HStack(spacing: AppSpacing.sm) {
            Image(systemName: icon)
                .font(.system(size: 15, weight: .medium))
                .foregroundStyle(AppColor.primary)
                .frame(width: 24)
            VStack(alignment: .leading, spacing: 2) {
                Text(label).labelSM()
                Text(value).bodyMD(AppColor.textPrimary)
            }
            Spacer()
        }
    }

    private var menuCard: some View {
        VStack(spacing: 0) {
            menuRow(icon: "bell.badge.fill", title: "Notificaciones", subtitle: "Recordatorios de citas")
            Divider().overlay(AppColor.border)
            menuRow(icon: "shield.lefthalf.filled", title: "Privacidad", subtitle: "Datos y seguridad")
            Divider().overlay(AppColor.border)
            Button {
                showLogoutConfirm = true
            } label: {
                HStack(spacing: AppSpacing.sm) {
                    Image(systemName: "rectangle.portrait.and.arrow.right")
                        .font(.system(size: 16, weight: .medium))
                        .foregroundStyle(AppColor.danger)
                        .frame(width: 24)
                    Text("Cerrar sesión").font(AppFont.labelLG(AppColor.danger))
                    Spacer()
                }
                .padding(.vertical, 14)
            }
            .pressable()
        }
        .padding(.horizontal, AppSpacing.md)
        .background(AppColor.surface)
        .clipShape(RoundedRectangle(cornerRadius: AppRadius.lg, style: .continuous))
        .overlay(RoundedRectangle(cornerRadius: AppRadius.lg, style: .continuous)
            .stroke(AppColor.border, lineWidth: 1))
    }

    private func menuRow(icon: String, title: String, subtitle: String) -> some View {
        HStack(spacing: AppSpacing.sm) {
            Image(systemName: icon)
                .font(.system(size: 16, weight: .medium))
                .foregroundStyle(AppColor.textBody)
                .frame(width: 24)
            VStack(alignment: .leading, spacing: 1) {
                Text(title).labelLG()
                Text(subtitle).labelSM()
            }
            Spacer()
            Image(systemName: "chevron.right")
                .font(.system(size: 13, weight: .semibold))
                .foregroundStyle(AppColor.textMuted)
        }
        .padding(.vertical, 14)
    }
}
