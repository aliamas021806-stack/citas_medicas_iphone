//
//  Typography.swift
//  CitasMedicas
//
//  Escala tipográfica del sistema. Usa "Plus Jakarta Sans" para titulares e
//  "Inter" para cuerpo/datos. Si las fuentes no están incluidas en el bundle,
//  se produce un fallback automático al font de sistema respetando pesos y
//  tamaños. Los datos numéricos (fechas/horas) usan cifras tabulares para
//  evitar jitter vertical — ver `.monospacedDigit()`.
//

import SwiftUI

enum AppFont {

    // MARK: Titulares (Plus Jakarta Sans)
    static func headlineXL() -> Font { .custom("PlusJakartaSans-Bold", size: 32, relativeTo: .largeTitle) }
    static func headlineXLMobile() -> Font { .custom("PlusJakartaSans-Bold", size: 26, relativeTo: .title) }
    static func headlineLG() -> Font { .custom("PlusJakartaSans-SemiBold", size: 24, relativeTo: .title2) }
    static func headlineLGMobile() -> Font { .custom("PlusJakartaSans-SemiBold", size: 20, relativeTo: .title3) }
    static func headlineMD() -> Font { .custom("PlusJakartaSans-SemiBold", size: 18, relativeTo: .headline) }

    // MARK: Cuerpo (Inter)
    static func bodyLG() -> Font { .custom("Inter-Regular", size: 16, relativeTo: .body) }
    static func bodyMD() -> Font { .custom("Inter-Regular", size: 14, relativeTo: .subheadline) }
    static func bodySM() -> Font { .custom("Inter-Regular", size: 12, relativeTo: .caption) }
    static func labelLG() -> Font { .custom("Inter-SemiBold", size: 14, relativeTo: .subheadline) }
    static func labelMD() -> Font { .custom("Inter-SemiBold", size: 12, relativeTo: .caption) }
    static func labelSM() -> Font { .custom("Inter-Medium", size: 11, relativeTo: .caption2) }
}

// MARK: - Atajos de estilo de texto

extension View {
    func headlineXL(_ color: Color = AppColor.textPrimary) -> some View {
        self.font(AppFont.headlineXL()).foregroundStyle(color)
    }
    func headlineXLMobile(_ color: Color = AppColor.textPrimary) -> some View {
        self.font(AppFont.headlineXLMobile()).foregroundStyle(color)
    }
    func headlineLG(_ color: Color = AppColor.textPrimary) -> some View {
        self.font(AppFont.headlineLG()).foregroundStyle(color)
    }
    func headlineLGMobile(_ color: Color = AppColor.textPrimary) -> some View {
        self.font(AppFont.headlineLGMobile()).foregroundStyle(color)
    }
    func headlineMD(_ color: Color = AppColor.textPrimary) -> some View {
        self.font(AppFont.headlineMD()).foregroundStyle(color)
    }
    func bodyLG(_ color: Color = AppColor.textBody) -> some View {
        self.font(AppFont.bodyLG()).foregroundStyle(color)
    }
    func bodyMD(_ color: Color = AppColor.textBody) -> some View {
        self.font(AppFont.bodyMD()).foregroundStyle(color)
    }
    func bodySM(_ color: Color = AppColor.textMuted) -> some View {
        self.font(AppFont.bodySM()).foregroundStyle(color)
    }
    func labelLG(_ color: Color = AppColor.textPrimary) -> some View {
        self.font(AppFont.labelLG()).foregroundStyle(color)
    }
    func labelMD(_ color: Color = AppColor.textBody) -> some View {
        self.font(AppFont.labelMD()).foregroundStyle(color)
    }
    func labelSM(_ color: Color = AppColor.textMuted) -> some View {
        self.font(AppFont.labelSM()).foregroundStyle(color)
    }
}
