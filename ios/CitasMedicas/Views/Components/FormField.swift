//
//  FormField.swift
//  CitasMedicas
//
//  Campo de texto del sistema de diseño: etiqueta, icono, focus ring y error.
//

import SwiftUI
import UIKit

struct FormField: View {
    let label: String
    let placeholder: String
    @Binding var text: String
    var icon: String? = nil
    var keyboard: UIKeyboardType = .default
    var isSecure: Bool = false
    var errorText: String? = nil
    var capitalization: TextInputAutocapitalization = .sentences

    @FocusState private var isFocused: Bool

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(label).labelLG(AppColor.textBody)

            HStack(spacing: 10) {
                if let icon {
                    Image(systemName: icon)
                        .font(.system(size: 16, weight: .medium))
                        .foregroundStyle(isFocused ? AppColor.primary : AppColor.textMuted)
                        .frame(width: 20)
                }

                if isSecure {
                    SecureField(placeholder, text: $text)
                        .font(AppFont.bodyMD())
                        .focused($isFocused)
                } else {
                    TextField(placeholder, text: $text)
                        .font(AppFont.bodyMD())
                        .keyboardType(keyboard)
                        .textInputAutocapitalization(capitalization)
                        .autocorrectionDisabled()
                        .focused($isFocused)
                }
            }
            .foregroundStyle(AppColor.textPrimary)
            .padding(.horizontal, 14)
            .frame(height: 50)
            .background(AppColor.surface)
            .clipShape(RoundedRectangle(cornerRadius: AppRadius.md, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: AppRadius.md, style: .continuous)
                    .stroke(borderColor, lineWidth: 1)
            )
            .animation(.easeOut(duration: 0.15), value: isFocused)

            if let errorText {
                Text(errorText)
                    .font(AppFont.labelSM())
                    .foregroundStyle(AppColor.danger)
            }
        }
    }

    private var borderColor: Color {
        if errorText != nil { return AppColor.danger }
        return isFocused ? AppColor.primary : AppColor.border
    }
}
