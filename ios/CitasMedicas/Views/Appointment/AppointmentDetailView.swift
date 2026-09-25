//
//  AppointmentDetailView.swift
//  CitasMedicas
//
//  Detalle de una cita médica. Muestra estado, fecha/hora, doctor asignado,
//  motivo, resumen y acciones (iniciar consulta / cancelar).
//

import SwiftUI

struct AppointmentDetailView: View {

    let appointment: Appointment

    @EnvironmentObject private var appointmentVM: AppointmentViewModel
    @Environment(\.dismiss) private var dismiss

    @State private var showCancelConfirm = false

    private var doctor: Doctor? { MockData.shared.doctor(byId: appointment.doctorId) }

    var body: some View {
        ScrollView {
            VStack(spacing: AppSpacing.md) {
                headerCard
                doctorCard
                if !appointment.reason.isEmpty { reasonCard }
                infoCard
            }
            .padding(.horizontal, AppSpacing.margin)
            .padding(.top, AppSpacing.sm)
            .padding(.bottom, 120)
        }
        .background(AppColor.background)
        .navigationTitle("Detalle de Cita")
        .navigationBarTitleDisplayMode(.inline)
        .safeAreaInset(edge: .bottom) { actionBar }
        .confirmationDialog("¿Cancelar esta cita?", isPresented: $showCancelConfirm, titleVisibility: .visible) {
            Button("Sí, cancelar", role: .destructive) {
                appointmentVM.cancel(appointment)
                dismiss()
            }
            Button("Mantener", role: .cancel) {}
        } message: {
            Text("\(appointment.doctorName) · \(appointment.timeRange)")
        }
    }

    // MARK: - Header

    private var headerCard: some View {
        VStack(alignment: .leading, spacing: AppSpacing.sm) {
            HStack {
                StatusBadge(status: appointment.status)
                Spacer()
                Text("#\(appointment.id.prefix(8).uppercased())")
                    .labelSM()
            }

            Text(longDate)
                .headlineLGMobile()

            HStack(spacing: 6) {
                Image(systemName: "clock").font(.system(size: 14, weight: .semibold))
                    .foregroundStyle(AppColor.primary)
                Text(appointment.timeRange)
                    .font(AppFont.labelLG(AppColor.textPrimary))
                    .monospacedDigit()
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .cardStyle()
        .overlay(alignment: .leading) {
            RoundedRectangle(cornerRadius: 3)
                .fill(accentColor).frame(width: 4)
                .padding(.vertical, 14).padding(.leading, 1)
        }
    }

    // MARK: - Doctor

    private var doctorCard: some View {
        VStack(alignment: .leading, spacing: AppSpacing.sm) {
            Text("ESPECIALISTA ASIGNADO").labelSM()

            HStack(spacing: AppSpacing.sm) {
                AvatarView(initials: initials(from: appointment.doctorName),
                           size: 56, tint: AppColor.primary, showOnlineDot: true)
                VStack(alignment: .leading, spacing: 3) {
                    Text(appointment.doctorName).headlineMD()
                    Text(appointment.specialtyName).bodyMD(AppColor.primary)
                    if let doctor {
                        Text("\(doctor.hospital) · \(doctor.yearsExperience) años exp.")
                            .bodySM()
                    }
                }
                Spacer()
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .cardStyle()
    }

    // MARK: - Motivo

    private var reasonCard: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Motivo de consulta").headlineMD()
            Text(appointment.reason)
                .bodyMD(AppColor.textBody)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .cardStyle()
    }

    // MARK: - Info

    private var infoCard: some View {
        VStack(alignment: .leading, spacing: AppSpacing.sm) {
            Text("Datos del paciente").headlineMD()
            infoRow(icon: "person.fill", label: "Paciente", value: appointment.patientName)
            Divider().overlay(AppColor.border)
            infoRow(icon: "calendar", label: "Edad", value: "\(appointment.patientAge) años")
            if let doctor {
                Divider().overlay(AppColor.border)
                infoRow(icon: "eurosign.circle.fill", label: "Tarifa", value: doctor.formattedFee)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .cardStyle()
    }

    private func infoRow(icon: String, label: String, value: String) -> some View {
        HStack(spacing: AppSpacing.sm) {
            Image(systemName: icon).font(.system(size: 15, weight: .medium))
                .foregroundStyle(AppColor.primary).frame(width: 24)
            Text(label).bodyMD(AppColor.textMuted)
            Spacer()
            Text(value).labelLG(AppColor.textPrimary)
        }
    }

    // MARK: - Acciones

    private var actionBar: some View {
        VStack(spacing: AppSpacing.sm) {
            if appointment.isCancellable {
                HStack(spacing: AppSpacing.sm) {
                    if appointment.status != .cancelled {
                        PrimaryButton(title: "Iniciar Consulta", icon: "video.fill") { }
                    }
                    Button {
                        showCancelConfirm = true
                    } label: {
                        Text("Cancelar cita")
                            .font(AppFont.labelLG(AppColor.danger))
                            .frame(maxWidth: .infinity).frame(height: 52)
                            .background(AppColor.canceledFill)
                            .clipShape(RoundedRectangle(cornerRadius: AppRadius.md, style: .continuous))
                    }
                    .pressable()
                }
            } else {
                Text("Esta cita está \(appointment.status.displayName.lowercased())")
                    .bodyMD(AppColor.textMuted)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 12)
                    .background(AppColor.surfaceLevel2)
                    .clipShape(RoundedRectangle(cornerRadius: AppRadius.md, style: .continuous))
            }
        }
        .padding(.horizontal, AppSpacing.margin)
        .padding(.vertical, AppSpacing.sm)
        .background(.ultraThinMaterial)
        .overlay(alignment: .top) { Divider().overlay(AppColor.border) }
    }

    private var accentColor: Color {
        switch appointment.status {
        case .confirmed: return AppColor.mint
        case .pending:   return Color(hex: 0xF59E0B)
        case .cancelled: return AppColor.danger
        }
    }

    private var longDate: String {
        guard let date = appointment.dateValue else { return appointment.date }
        let f = DateFormatter()
        f.locale = Locale(identifier: "es_ES")
        f.dateFormat = "EEEE d MMMM yyyy"
        return f.string(from: date).capitalized
    }

    private func initials(from name: String) -> String {
        let cleaned = name.replacingOccurrences(of: "Dr. ", with: "")
            .replacingOccurrences(of: "Dra. ", with: "")
        let parts = cleaned.split(separator: " ").map(String.init)
        guard let first = parts.first, let c1 = first.first else { return "?" }
        var result = String(c1).uppercased()
        if let last = parts.last, parts.count > 1, let c2 = last.first {
            result += String(c2).uppercased()
        }
        return result
    }
}
