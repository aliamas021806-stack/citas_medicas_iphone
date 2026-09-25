//
//  AppointmentCard.swift
//  CitasMedicas
//
//  Tarjeta de cita médica (listado "Mis Citas"). Muestra acento de color por
//  estado, hora tabular, doctor y acciones contextuales.
//

import SwiftUI

struct AppointmentCard: View {
    let appointment: Appointment
    var onCancel: (() -> Void)? = nil
    var onDetail: (() -> Void)? = nil

    var body: some View {
        VStack(alignment: .leading, spacing: AppSpacing.sm) {
            // Fila superior: hora + estado
            HStack {
                HStack(spacing: 6) {
                    Image(systemName: "clock")
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundStyle(AppColor.primary)
                    Text(appointment.timeRange)
                        .font(AppFont.labelLG(AppColor.textPrimary))
                        .monospacedDigit()
                }
                Spacer()
                StatusBadge(status: appointment.status)
            }

            Divider().overlay(AppColor.border)

            // Fila central: doctor + especialidad
            HStack(spacing: AppSpacing.sm) {
                AvatarView(initials: initials(from: appointment.doctorName),
                           size: 44, tint: accentColor)

                VStack(alignment: .leading, spacing: 3) {
                    Text(appointment.doctorName)
                        .headlineMD()
                    HStack(spacing: 6) {
                        Image(systemName: "stethoscope")
                            .font(.system(size: 12, weight: .medium))
                            .foregroundStyle(AppColor.textMuted)
                        Text(appointment.specialtyName)
                            .bodySM(AppColor.textBody)
                    }
                }
                Spacer()

                if appointment.isCancellable, let onDetail {
                    Button(action: onDetail) {
                        Image(systemName: "chevron.right")
                            .font(.system(size: 15, weight: .semibold))
                            .foregroundStyle(AppColor.textMuted)
                    }
                }
            }

            if !appointment.reason.isEmpty {
                Text(appointment.reason)
                    .bodySM(AppColor.textMuted)
                    .lineLimit(2)
                    .padding(.top, 2)
            }

            // Acciones
            if appointment.isCancellable, let onCancel {
                HStack(spacing: AppSpacing.sm) {
                    Spacer()
                    Button(action: onCancel) {
                        HStack(spacing: 5) {
                            Image(systemName: "xmark.circle.fill").font(.system(size: 13))
                            Text("Cancelar").font(AppFont.labelMD())
                        }
                        .foregroundStyle(AppColor.canceled)
                        .padding(.horizontal, 12).padding(.vertical, 8)
                        .background(AppColor.canceledFill)
                        .clipShape(Capsule())
                    }
                    .pressable()
                }
            }
        }
        .cardStyle(padding: AppSpacing.md)
        .overlay(alignment: .leading) {
            // Acento lateral de color por estado
            RoundedRectangle(cornerRadius: 3)
                .fill(accentColor)
                .frame(width: 4)
                .padding(.vertical, 12)
                .padding(.leading, 1)
        }
    }

    private var accentColor: Color {
        switch appointment.status {
        case .confirmed: return AppColor.mint
        case .pending:   return Color(hex: 0xF59E0B)
        case .cancelled: return AppColor.danger
        }
    }

    private func initials(from name: String) -> String {
        let cleaned = name
            .replacingOccurrences(of: "Dr. ", with: "")
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
