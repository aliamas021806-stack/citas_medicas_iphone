//
//  AppointmentListView.swift
//  CitasMedicas
//
//  Pantalla "Mis Citas Médicas": ver, crear y cancelar citas localmente.
//  Filtros por estado y estados de carga/vacío.
//

import SwiftUI

struct AppointmentListView: View {

    var onBook: () -> Void

    @EnvironmentObject private var appointmentVM: AppointmentViewModel
    @State private var filter: AppointmentFilter = .all
    @State private var appointmentToCancel: Appointment?

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: AppSpacing.md) {
                    header
                    filterBar

                    if filtered.isEmpty {
                        EmptyStateView(icon: "calendar.badge.plus",
                                       title: "Sin citas \(filter == .all ? "" : filter.title.lowercased())",
                                       message: "Agenda una nueva cita médica para verla aquí.")
                        SecondaryButton(title: "Agendar una cita", icon: "plus") { onBook() }
                            .padding(.horizontal, AppSpacing.xl)
                    } else {
                        LazyVStack(spacing: AppSpacing.sm) {
                            ForEach(filtered) { appointment in
                                NavigationLink {
                                    AppointmentDetailView(appointment: appointment)
                                        .environmentObject(appointmentVM)
                                } label: {
                                    AppointmentCard(
                                        appointment: appointment,
                                        onCancel: appointment.isCancellable
                                            ? { appointmentToCancel = appointment } : nil,
                                        onDetail: nil)
                                }
                                .buttonStyle(.plain)
                            }
                        }
                    }
                }
                .padding(.horizontal, AppSpacing.margin)
                .padding(.top, AppSpacing.md)
                .padding(.bottom, 120)
            }
            .background(AppColor.background)
            .navigationTitle("Mis Citas")
            .navigationBarTitleDisplayMode(.large)
            .onAppear { appointmentVM.reload() }
        }
        .confirmationDialog(
            "¿Cancelar esta cita?",
            isPresented: Binding(
                get: { appointmentToCancel != nil },
                set: { if !$0 { appointmentToCancel = nil } }),
            titleVisibility: .visible
        ) {
            Button("Sí, cancelar cita", role: .destructive) {
                if let appointment = appointmentToCancel {
                    appointmentVM.cancel(appointment)
                }
                appointmentToCancel = nil
            }
            Button("Mantener", role: .cancel) { appointmentToCancel = nil }
        } message: {
            if let a = appointmentToCancel {
                Text("\(a.doctorName) · \(a.timeRange)")
            }
        }
    }

    // MARK: - Header

    private var header: some View {
        HStack(spacing: AppSpacing.sm) {
            summaryPill(title: "Confirmadas", count: appointmentVM.confirmedCount, tint: AppColor.mint)
            summaryPill(title: "Pendientes", count: appointmentVM.pendingCount, tint: Color(hex: 0xF59E0B))
            summaryPill(title: "Canceladas", count: appointmentVM.cancelledCount, tint: AppColor.danger)
        }
    }

    private func summaryPill(title: String, count: Int, tint: Color) -> some View {
        VStack(spacing: 4) {
            Text("\(count)")
                .font(AppFont.headlineLGMobile(tint))
                .monospacedDigit()
            Text(title).labelSM()
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, AppSpacing.sm)
        .background(tint.opacity(0.08))
        .clipShape(RoundedRectangle(cornerRadius: AppRadius.md, style: .continuous))
    }

    // MARK: - Filtros

    private var filterBar: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: AppSpacing.sm) {
                ForEach(AppointmentFilter.allCases) { item in
                    Button {
                        withAnimation(.easeOut(duration: 0.2)) { filter = item }
                    } label: {
                        Text(item.title)
                            .font(AppFont.labelMD())
                            .foregroundStyle(filter == item ? .white : AppColor.textBody)
                            .padding(.horizontal, 14).padding(.vertical, 8)
                            .background(filter == item ? AppColor.primary : AppColor.surface)
                            .clipShape(Capsule())
                            .overlay(Capsule().stroke(filter == item ? .clear : AppColor.border, lineWidth: 1))
                    }
                    .pressable()
                }
            }
            .padding(.horizontal, 2)
        }
    }

    // MARK: - Filtrado

    private var filtered: [Appointment] {
        switch filter {
        case .all:       return appointmentVM.appointments
        case .confirmed: return appointmentVM.appointments.filter { $0.status == .confirmed }
        case .pending:   return appointmentVM.appointments.filter { $0.status == .pending }
        case .cancelled: return appointmentVM.appointments.filter { $0.status == .cancelled }
        }
    }
}

enum AppointmentFilter: String, CaseIterable, Identifiable {
    case all, confirmed, pending, cancelled
    var id: String { rawValue }
    var title: String {
        switch self {
        case .all:       return "Todas"
        case .confirmed: return "Confirmadas"
        case .pending:   return "Pendientes"
        case .cancelled: return "Canceladas"
        }
    }
}
