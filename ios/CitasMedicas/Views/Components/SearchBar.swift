//
//  SearchBar.swift
//  CitasMedicas
//
//  Barra de búsqueda con icono, botón de limpiar y acción de filtro opcional.
//

import SwiftUI

struct SearchBar: View {
    @Binding var text: String
    var placeholder: String = "Buscar..."
    var onFilterTap: (() -> Void)? = nil

    var body: some View {
        HStack(spacing: AppSpacing.sm) {
            HStack(spacing: 10) {
                Image(systemName: "magnifyingglass")
                    .font(.system(size: 16, weight: .medium))
                    .foregroundStyle(AppColor.textMuted)

                TextField(placeholder, text: $text)
                    .font(AppFont.bodyMD())
                    .foregroundStyle(AppColor.textPrimary)
                    .textInputAutocapitalization(.never)
                    .autocorrectionDisabled()

                if !text.isEmpty {
                    Button {
                        text = ""
                    } label: {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundStyle(AppColor.textMuted)
                    }
                }
            }
            .padding(.horizontal, 14)
            .frame(height: 48)
            .background(AppColor.surface)
            .clipShape(RoundedRectangle(cornerRadius: AppRadius.md, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: AppRadius.md, style: .continuous)
                    .stroke(AppColor.border, lineWidth: 1)
            )

            if let onFilterTap {
                Button(action: onFilterTap) {
                    Image(systemName: "slider.horizontal.3")
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundStyle(AppColor.primary)
                        .frame(width: 48, height: 48)
                        .background(AppColor.surface)
                        .clipShape(RoundedRectangle(cornerRadius: AppRadius.md, style: .continuous))
                        .overlay(
                            RoundedRectangle(cornerRadius: AppRadius.md, style: .continuous)
                                .stroke(AppColor.border, lineWidth: 1)
                        )
                }
                .pressable()
            }
        }
    }
}

/// Encabezado de sección con título y acción opcional a la derecha.
struct SectionHeader: View {
    let title: String
    var actionTitle: String? = nil
    var onAction: (() -> Void)? = nil

    var body: some View {
        HStack {
            Text(title).headlineMD()
            Spacer()
            if let actionTitle, let onAction {
                Button(action: onAction) {
                    Text(actionTitle).font(AppFont.labelMD(AppColor.primary))
                }
            }
        }
    }
}
