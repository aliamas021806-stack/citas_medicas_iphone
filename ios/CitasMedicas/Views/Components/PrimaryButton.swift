//
//  PrimaryButton.swift
//  CitasMedicas
//
//  Botón primario y secundario del sistema de diseño.
//

import SwiftUI

/// Botón primario sólido (azul clínico) con estado de carga.
struct PrimaryButton: View {
    let title: String
    var icon: String? = nil
    var isLoading: Bool = false
    var isEnabled: Bool = true
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 8) {
                if isLoading {
                    ProgressView()
                        .progressViewStyle(.circular)
                        .tint(.white)
                        .scaleEffect(0.9)
                } else if let icon {
                    Image(systemName: icon).font(.system(size: 16, weight: .semibold))
                }
                Text(title).font(AppFont.labelLG())
            }
            .frame(maxWidth: .infinity)
            .frame(height: 52)
            .foregroundStyle(.white)
            .background(isEnabled ? AppColor.primary : AppColor.primary.opacity(0.4))
            .clipShape(RoundedRectangle(cornerRadius: AppRadius.md, style: .continuous))
            .shadow(color: isEnabled ? AppShadow.primaryButton.color : .clear,
                    radius: 8, x: 0, y: 2)
        }
        .disabled(!isEnabled || isLoading)
        .pressable()
    }
}

/// Botón secundario suave (fondo pastel).
struct SecondaryButton: View {
    let title: String
    var icon: String? = nil
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 8) {
                if let icon {
                    Image(systemName: icon).font(.system(size: 15, weight: .semibold))
                }
                Text(title).font(AppFont.labelLG())
            }
            .frame(maxWidth: .infinity)
            .frame(height: 52)
            .foregroundStyle(AppColor.primaryTextOnSoft)
            .background(AppColor.primarySoft)
            .clipShape(RoundedRectangle(cornerRadius: AppRadius.md, style: .continuous))
        }
        .pressable()
    }
}

/// Botón fantasma (texto plano).
struct GhostButton: View {
    let title: String
    var icon: String? = nil
    var tint: Color = AppColor.textBody
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 6) {
                if let icon { Image(systemName: icon).font(.system(size: 14, weight: .semibold)) }
                Text(title).font(AppFont.labelLG(tint))
            }
        }
        .pressable()
    }
}
