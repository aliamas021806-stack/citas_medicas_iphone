//
//  HomeView.swift
//  CitasMedicas
//
//  Pantalla de inicio del paciente. Saludo contextual, métricas de citas,
//  barra de búsqueda, filtros por especialidad y listado de doctores.
//

import SwiftUI

struct HomeView: View {

    var onBook: () -> Void

    @EnvironmentObject private var env: AppEnvironment
    @EnvironmentObject private var doctorVM: DoctorViewModel
    @EnvironmentObject private var appointmentVM: AppointmentViewModel

    private var user: User { env.session.currentUser ?? .placeholder }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: AppSpacing.md) {
                greeting
                metrics
                SearchBar(text: Binding(
                    get: { doctorVM.searchQuery },
                    set: { doctorVM.search($0) }),
                    placeholder: "Buscar médico o especialidad...")
                specialtyFilters
                doctorSection
            }
            .padding(.horizontal, AppSpacing.margin)
            .padding(.top, AppSpacing.md)
            .padding(.bottom, 120)
        }
        .background(AppColor.background)
        .refreshable { doctorVM.loadHome() }
        .task { if doctorVM.doctors.isEmpty { doctorVM.loadHome() } }
    }

    // MARK: - Saludo

    private var greeting: some View {
        HStack(alignment: .center, spacing: AppSpacing.sm) {
            VStack(alignment: .leading, spacing: 4) {
                HStack(spacing: 6) {
                    Image(systemName: "calendar")
                        .font(.system(size: 13, weight: .semibold))
                        .foregroundStyle(AppColor.primary)
                    Text(Self.longDate).labelSM()
                }
                Text("Hola, \(user.fullName.split(separator: " ").first.map(String.init) ?? "Paciente")")
                    .headlineLGMobile()
                Text("¿Agendamos tu próxima consulta?")
                    .bodySM()
            }
            Spacer()
            Button(action: onBook) {
                HStack(spacing: 6) {
                    Image(systemName: "plus").font(.system(size: 15, weight: .bold))
                    Text("Nueva Cita").font(AppFont.labelMD())
                }
                .foregroundStyle(.white)
                .padding(.horizontal, 14).padding(.vertical, 11)
                .background(AppColor.primary)
                .clipShape(RoundedRectangle(cornerRadius: AppRadius.md, style: .continuous))
                .shadow(color: AppShadow.primaryButton.color, radius: 8, x: 0, y: 2)
            }
            .pressable()
        }
    }

    private static var longDate: String {
        let f = DateFormatter()
        f.locale = Locale(identifier: "es_ES")
        f.dateFormat = "EEEE, d MMMM"
        return f.string(from: Date()).capitalized
    }

    // MARK: - Métricas

    private var metrics: some View {
        HStack(spacing: AppSpacing.sm) {
            MetricCard(title: "Confirmadas", value: "\(appointmentVM.confirmedCount)",
                       icon: "checkmark.seal.fill", tint: AppColor.mint)
            MetricCard(title: "Pendientes", value: "\(appointmentVM.pendingCount)",
                       icon: "clock.fill", tint: Color(hex: 0xF59E0B))
            MetricCard(title: "Total", value: "\(appointmentVM.appointments.count)",
                       icon: "calendar", tint: AppColor.primary)
        }
    }

    // MARK: - Filtros de especialidad

    private var specialtyFilters: some View {
        VStack(alignment: .leading, spacing: AppSpacing.sm) {
            SectionHeader(title: "Especialidades")

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: AppSpacing.sm) {
                    specialtyChip(id: nil, label: "Todas", emoji: "🩺")

                    ForEach(doctorVM.specialties) { specialty in
                        specialtyChip(id: specialty.id,
                                      label: specialty.name,
                                      emoji: specialty.iconEmoji)
                    }
                }
                .padding(.horizontal, 2)
            }
        }
    }

    private func specialtyChip(id: String?, label: String, emoji: String) -> some View {
        let selected = doctorVM.selectedSpecialtyId == id
        return Button {
            withAnimation(.easeOut(duration: 0.2)) { doctorVM.selectSpecialty(id) }
        } label: {
            HStack(spacing: 6) {
                Text(emoji)
                Text(label).font(AppFont.labelMD())
            }
            .foregroundStyle(selected ? .white : AppColor.textBody)
            .padding(.horizontal, 14).padding(.vertical, 9)
            .background(selected ? AppColor.primary : AppColor.surface)
            .clipShape(Capsule())
            .overlay(
                Capsule().stroke(selected ? Color.clear : AppColor.border, lineWidth: 1)
            )
        }
        .pressable()
    }

    // MARK: - Doctores

    private var doctorSection: some View {
        VStack(alignment: .leading, spacing: AppSpacing.sm) {
            SectionHeader(title: "Médicos disponibles")
                .padding(.top, 4)

            switch doctorVM.doctorsState.status {
            case .loading:
                LoadingView(message: "Cargando médicos...")
            case .error:
                ErrorView(message: doctorVM.doctorsState.message ?? "Error al cargar",
                          onRetry: { doctorVM.loadDoctors() })
            case .success:
                if doctorVM.doctors.isEmpty {
                    EmptyStateView(icon: "magnifyingglass", title: "Sin resultados",
                                   message: "Prueba con otra especialidad o término de búsqueda.")
                } else {
                    LazyVStack(spacing: AppSpacing.sm) {
                        ForEach(doctorVM.doctors) { doctor in
                            NavigationLink {
                                DoctorDetailView(doctor: doctor)
                                    .environmentObject(env)
                                    .environmentObject(appointmentVM)
                            } label: {
                                DoctorRow(doctor: doctor)
                            }
                            .buttonStyle(.plain)
                        }
                    }
                }
            }
        }
    }
}

// MARK: - Subvistas

private struct MetricCard: View {
    let title: String
    let value: String
    let icon: String
    let tint: Color

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Image(systemName: icon)
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundStyle(tint)
                    .frame(width: 28, height: 28)
                    .background(tint.opacity(0.12))
                    .clipShape(Circle())
                Spacer()
            }
            Text(value)
                .font(AppFont.headlineLG())
                .foregroundStyle(AppColor.textPrimary)
                .monospacedDigit()
            Text(title).labelSM()
        }
        .padding(AppSpacing.sm + 2)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(AppColor.surface)
        .clipShape(RoundedRectangle(cornerRadius: AppRadius.md, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: AppRadius.md, style: .continuous)
                .stroke(AppColor.border, lineWidth: 1)
        )
    }
}

struct DoctorRow: View {
    let doctor: Doctor

    var body: some View {
        HStack(spacing: AppSpacing.sm) {
            AvatarView(initials: doctor.initials, size: 52, tint: AppColor.primary)

            VStack(alignment: .leading, spacing: 3) {
                Text(doctor.displayName)
                    .headlineMD()
                Text(doctor.specialtyName)
                    .bodySM(AppColor.primary)
                HStack(spacing: 10) {
                    Label(String(format: "%.1f", doctor.rating), systemImage: "star.fill")
                        .font(AppFont.labelSM(Color(hex: 0xF59E0B)))
                    Label("\(doctor.yearsExperience) años", systemImage: "briefcase.fill")
                        .font(AppFont.labelSM())
                }
            }
            Spacer()

            Image(systemName: "chevron.right")
                .font(.system(size: 14, weight: .semibold))
                .foregroundStyle(AppColor.textMuted)
        }
        .cardStyle(padding: AppSpacing.sm + 4)
    }
}
