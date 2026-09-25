//
//  StateViews.swift
//  CitasMedicas
//
//  Vistas de estado reutilizables: carga, error (con reintento) y vacío.
//

import SwiftUI

/// Indicador de carga a pantalla de sección.
struct LoadingView: View {
    var message: String = "Cargando..."

    var body: some View {
        VStack(spacing: AppSpacing.md) {
            ProgressView()
                .tint(AppColor.primary)
                .scaleEffect(1.2)
            Text(message).bodyMD(AppColor.textMuted)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, AppSpacing.xl)
    }
}

/// Vista de error con icono y botón de reintento.
struct ErrorView: View {
    let message: String
    var onRetry: (() -> Void)? = nil

    var body: some View {
        VStack(spacing: AppSpacing.md) {
            Image(systemName: "exclamationmark.triangle.fill")
                .font(.system(size: 40))
                .foregroundStyle(AppColor.danger)
            Text(message)
                .bodyMD(AppColor.textBody)
                .multilineTextAlignment(.center)
            if let onRetry {
                Button(action: onRetry) {
                    Text("Reintentar").font(AppFont.labelLG()).foregroundStyle(.white)
                        .padding(.horizontal, 20).padding(.vertical, 10)
                        .background(AppColor.primary)
                        .clipShape(Capsule())
                }
                .pressable()
            }
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, AppSpacing.xl)
    }
}

/// Vista de estado vacío.
struct EmptyStateView: View {
    let icon: String
    let title: String
    let message: String

    var body: some View {
        VStack(spacing: AppSpacing.md) {
            ZStack {
                Circle().fill(AppColor.primarySoft).frame(width: 88, height: 88)
                Image(systemName: icon)
                    .font(.system(size: 36, weight: .medium))
                    .foregroundStyle(AppColor.primary)
            }
            Text(title).headlineMD()
            Text(message)
                .bodyMD(AppColor.textMuted)
                .multilineTextAlignment(.center)
                .padding(.horizontal, AppSpacing.lg)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, AppSpacing.xl)
    }
}
