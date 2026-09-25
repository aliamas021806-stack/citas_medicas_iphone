//
//  MainTabView.swift
//  CitasMedicas
//
//  Contenedor principal con barra de navegación inferior (estilo glassmorphism).
//  Pestañas: Inicio, Mis Citas, Perfil.
//

import SwiftUI

enum MainTab: CaseIterable {
    case home, appointments, profile

    var title: String {
        switch self {
        case .home:         return "Inicio"
        case .appointments: return "Mis Citas"
        case .profile:      return "Perfil"
        }
    }
    var icon: String {
        switch self {
        case .home:         return "house.fill"
        case .appointments: return "calendar.badge.clock"
        case .profile:      return "person.crop.circle.fill"
        }
    }
}

struct MainTabView: View {

    var onLogout: () -> Void

    @EnvironmentObject private var env: AppEnvironment
    @State private var selectedTab: MainTab = .home
    @State private var showNewAppointment = false

    var body: some View {
        ZStack(alignment: .bottom) {
            AppColor.background.ignoresSafeArea()

            Group {
                switch selectedTab {
                case .home:
                    NavigationStack {
                        HomeView(onBook: { showNewAppointment = true })
                    }
                case .appointments:
                    AppointmentListView(onBook: { showNewAppointment = true })
                case .profile:
                    ProfileView(onLogout: onLogout)
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)

            FloatingTabBar(selected: $selectedTab)
        }
        .sheet(isPresented: $showNewAppointment) {
            NewAppointmentView()
                .environmentObject(env)
                .environmentObject(env.appointments)
                .environmentObject(env.doctors)
        }
    }
}

/// Barra inferior flotante con fondo translúcido y desenfoque.
private struct FloatingTabBar: View {
    @Binding var selected: MainTab

    var body: some View {
        HStack(spacing: 0) {
            ForEach(MainTab.allCases, id: \.self) { tab in
                Button {
                    withAnimation(.spring(response: 0.3, dampingFraction: 0.7)) {
                        selected = tab
                    }
                } label: {
                    VStack(spacing: 4) {
                        ZStack {
                            if selected == tab {
                                Capsule()
                                    .fill(AppColor.primarySoft)
                                    .frame(width: 56, height: 30)
                            }
                            Image(systemName: tab.icon)
                                .font(.system(size: 18, weight: .semibold))
                                .foregroundStyle(selected == tab ? AppColor.primary : AppColor.textMuted)
                        }
                        .frame(height: 30)
                        Text(tab.title)
                            .font(AppFont.labelSM(selected == tab ? AppColor.primary : AppColor.textMuted))
                    }
                    .frame(maxWidth: .infinity)
                }
                .buttonStyle(.plain)
            }
        }
        .padding(.horizontal, AppSpacing.sm)
        .padding(.top, 8)
        .padding(.bottom, 4)
        .background(.ultraThinMaterial)
        .clipShape(RoundedRectangle(cornerRadius: AppRadius.xl, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: AppRadius.xl, style: .continuous)
                .stroke(AppColor.border, lineWidth: 1)
        )
        .shadow(color: Color(hex: 0x0F172A, alpha: 0.08), radius: 20, x: 0, y: 8)
        .padding(.horizontal, AppSpacing.margin)
        .padding(.bottom, AppSpacing.sm)
    }
}
