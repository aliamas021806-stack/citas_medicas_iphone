//
//  Theme.swift
//  CitasMedicas
//
//  Sistema de diseño "Clinical Soft Light".
//  Paleta, espaciado, radios y sombras extraídos del DESIGN.md del proyecto
//  de Stitch y adaptados a SwiftUI (modo claro, tokens semánticos).
//

import SwiftUI

// MARK: - Tokens de color

extension Color {
    /// Crea un Color a partir de un valor hexadecimal (0xRRGGBB).
    init(hex: UInt32, alpha: Double = 1.0) {
        let r = Double((hex >> 16) & 0xFF) / 255.0
        let g = Double((hex >> 8) & 0xFF) / 255.0
        let b = Double(hex & 0xFF) / 255.0
        self.init(.sRGB, red: r, green: g, blue: b, opacity: alpha)
    }
}

/// Tokens de color centrales del sistema de diseño. Nombrados por rol semántico.
enum AppColor {

    // Superficies
    static let background        = Color(hex: 0xF8FAFC)  // Canvas base
    static let surface           = Color(hex: 0xFFFFFF)  // Tarjetas (Nivel 1)
    static let surfaceLevel2     = Color(hex: 0xF1F5F9)  // Divisores / campos inactivos
    static let border            = Color(hex: 0xE2E8F0)  // Bordes y separadores
    static let surfaceTint       = Color(hex: 0xE0F2FE)  // Pastel Sky (fondos selección)

    // Texto
    static let textPrimary       = Color(hex: 0x0F172A)  // Deep Slate
    static let textBody          = Color(hex: 0x475569)
    static let textMuted         = Color(hex: 0x94A3B8)

    // Marca
    static let primary           = Color(hex: 0x2563EB)  // Azul clínico
    static let primarySoft       = Color(hex: 0xE0F2FE)  // Primary tint
    static let primaryTextOnSoft = Color(hex: 0x1D4ED8)
    static let primaryHover      = Color(hex: 0x3B82F6)

    // Estados (badges)
    static let confirmedFill     = Color(hex: 0xD1FAE5)
    static let confirmed         = Color(hex: 0x065F46)
    static let pendingFill       = Color(hex: 0xFEF3C7)
    static let pending           = Color(hex: 0x92400E)
    static let inConsultFill     = Color(hex: 0xDBEAFE)
    static let inConsult         = Color(hex: 0x1E40AF)
    static let canceledFill      = Color(hex: 0xFFE4E6)
    static let canceled          = Color(hex: 0x9F1239)

    // Acentos
    static let mint              = Color(hex: 0x10B981)
    static let mintFill          = Color(hex: 0xD1FAE5)
    static let mintDark          = Color(hex: 0x065F46)
    static let danger            = Color(hex: 0xF43F5E)

    // Semánticos
    static let error             = Color(hex: 0xBA1A1A)
    static let errorContainer    = Color(hex: 0xFFDAD6)
}

// MARK: - Espaciado (grid de 8 puntos)

enum AppSpacing {
    static let xs: CGFloat = 4
    static let sm: CGFloat = 8
    static let md: CGFloat = 16
    static let lg: CGFloat = 24
    static let xl: CGFloat = 40
    /// Margen horizontal de pantalla en móvil.
    static let margin: CGFloat = 16
}

// MARK: - Radios

enum AppRadius {
    static let sm: CGFloat = 8
    static let md: CGFloat = 12
    static let lg: CGFloat = 16
    static let xl: CGFloat = 24
    static let full: CGFloat = 999
}

// MARK: - Sombras ambientales tintadas

enum AppShadow {
    /// Sombra de tarjeta (Surface Level 1).
    static let card = ShadowSpec(color: Color(hex: 0x2563EB, alpha: 0.04),
                                 radius: 20, x: 0, y: 4)
    /// Sombra de segunda capa para profundidad.
    static let cardSecondary = ShadowSpec(color: Color(hex: 0x0F172A, alpha: 0.03),
                                          radius: 6, x: 0, y: 2)
    /// Sombra de popover / modal.
    static let popover = ShadowSpec(color: Color(hex: 0x0F172A, alpha: 0.10),
                                    radius: 36, x: 0, y: 16)
    /// Sombra de botón primario.
    static let primaryButton = ShadowSpec(color: Color(hex: 0x2563EB, alpha: 0.25),
                                          radius: 8, x: 0, y: 2)
}

struct ShadowSpec {
    let color: Color
    let radius: CGFloat
    let x: CGFloat
    let y: CGFloat
}

// MARK: - Modificadores reutilizables

extension View {
    /// Aplica una Card de Nivel 1: fondo blanco, borde hairline y sombra tintada.
    func cardStyle(padding: CGFloat = AppSpacing.md,
                   radius: CGFloat = AppRadius.lg) -> some View {
        self
            .padding(padding)
            .background(AppColor.surface)
            .clipShape(RoundedRectangle(cornerRadius: radius, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: radius, style: .continuous)
                    .stroke(AppColor.border, lineWidth: 1)
            )
            .shadow(color: AppShadow.card.color, radius: AppShadow.card.radius,
                    x: AppShadow.card.x, y: AppShadow.card.y)
    }

    /// Aplica una sombra compuesta (dos capas) para mayor realismo.
    func ambientShadow(_ spec: ShadowSpec) -> some View {
        self.shadow(color: spec.color, radius: spec.radius, x: spec.x, y: spec.y)
    }

    /// Feedback táctil sutil al pulsar (escala 0.97).
    func pressable() -> some View {
        self.buttonStyle(PressableButtonStyle())
    }
}

/// Estilo de botón con efecto de presión ergonómico.
struct PressableButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .scaleEffect(configuration.isPressed ? 0.97 : 1.0)
            .opacity(configuration.isPressed ? 0.9 : 1.0)
            .animation(.easeOut(duration: 0.15), value: configuration.isPressed)
    }
}
