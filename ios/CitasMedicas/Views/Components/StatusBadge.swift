//
//  StatusBadge.swift
//  CitasMedicas
//
//  Badge tipo píldora con punto indicador, usado para el estado de una cita.
//

import SwiftUI

struct StatusBadge: View {
    let status: AppointmentStatus
    var compact: Bool = false

    var body: some View {
        HStack(spacing: 6) {
            Circle()
                .fill(foreground)
                .frame(width: 6, height: 6)
            Text(status.displayName)
                .font(AppFont.labelMD())
                .foregroundStyle(foreground)
        }
        .padding(.horizontal, compact ? 8 : 10)
        .padding(.vertical, compact ? 4 : 5)
        .background(background)
        .clipShape(Capsule())
    }

    private var foreground: Color {
        switch status {
        case .confirmed: return AppColor.confirmed
        case .pending:   return AppColor.pending
        case .cancelled: return AppColor.canceled
        }
    }

    private var background: Color {
        switch status {
        case .confirmed: return AppColor.confirmedFill
        case .pending:   return AppColor.pendingFill
        case .cancelled: return AppColor.canceledFill
        }
    }
}

/// Chip genérico de etiqueta (especialidad, hospital, etc.).
struct InfoChip: View {
    let icon: String?
    let text: String
    var tint: Color = AppColor.primaryTextOnSoft

    var body: some View {
        HStack(spacing: 5) {
            if let icon {
                Image(systemName: icon).font(.system(size: 12, weight: .semibold))
            }
            Text(text).font(AppFont.labelMD())
        }
        .foregroundStyle(tint)
        .padding(.horizontal, 10)
        .padding(.vertical, 5)
        .background(tint.opacity(0.10))
        .clipShape(Capsule())
    }
}
