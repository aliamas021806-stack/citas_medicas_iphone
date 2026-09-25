//
//  DoctorDetailView.swift
//  CitasMedicas
//
//  Detalle del doctor y selección de fecha/hora para la cita. Adapta el
//  DoctorDetailActivity (DateAdapter + SlotAdapter) a SwiftUI.
//

import SwiftUI

struct DoctorDetailView: View {

    let doctor: Doctor

    @EnvironmentObject private var env: AppEnvironment
    @EnvironmentObject private var appointmentVM: AppointmentViewModel
    @Environment(\.dismiss) private var dismiss

    @StateObject private var doctorVM: DoctorViewModel

    @State private var selectedDate: Date?
    @State private var selectedSlot: TimeSlot?
    @State private var reason: String = ""
    @State private var showConfirmation = false

    init(doctor: Doctor) {
        self.doctor = doctor
        _doctorVM = StateObject(wrappedValue: DoctorViewModel())
    }

    private var user: User { env.session.currentUser ?? .placeholder }
    private var selectableDates: [Date] { MockData.shared.selectableDates() }

    var body: some View {
        ScrollView {
            VStack(spacing: AppSpacing.md) {
                doctorHeaderCard
                bioCard
                dateSection
                timeSection
                reasonSection
            }
            .padding(.horizontal, AppSpacing.margin)
            .padding(.top, AppSpacing.sm)
            .padding(.bottom, 120)
        }
        .background(AppColor.background)
        .navigationTitle("Agendar cita")
        .navigationBarTitleDisplayMode(.inline)
        .safeAreaInset(edge: .bottom) {
            confirmBar
        }
        .task {
            doctorVM.loadSlots(doctorId: doctor.id)
            if selectedDate == nil {
                selectedDate = selectableDates.first
            }
        }
        .onChange(of: selectedDate) { _, _ in
            selectedSlot = nil
        }
        .alert("¡Cita agendada!", isPresented: $showConfirmation) {
            Button("Perfecto") { dismiss() }
        } message: {
            Text("\(doctor.displayName)\n\(formattedSelectedDate) · \(selectedSlot?.time ?? "")")
        }
    }

    // MARK: - Header

    private var doctorHeaderCard: some View {
        VStack(spacing: AppSpacing.sm) {
            AvatarView(initials: doctor.initials, size: 80, tint: AppColor.primary)
            Text(doctor.displayName).headlineLGMobile()
            Text(doctor.specialtyName)
                .bodyMD(AppColor.primary)

            HStack(spacing: AppSpacing.md) {
                statPill(icon: "star.fill", text: String(format: "%.1f", doctor.rating),
                         tint: Color(hex: 0xF59E0B))
                statPill(icon: "briefcase.fill", text: "\(doctor.yearsExperience) años",
                         tint: AppColor.primary)
                statPill(icon: "eurosign.circle.fill", text: doctor.formattedFee,
                         tint: AppColor.mint)
            }

            Label(doctor.hospital, systemImage: "building.2.fill")
                .font(AppFont.labelMD())
                .foregroundStyle(AppColor.textBody)
        }
        .frame(maxWidth: .infinity)
        .cardStyle()
    }

    private func statPill(icon: String, text: String, tint: Color) -> some View {
        HStack(spacing: 5) {
            Image(systemName: icon).font(.system(size: 12, weight: .semibold))
            Text(text).font(AppFont.labelMD())
        }
        .foregroundStyle(tint)
        .padding(.horizontal, 10).padding(.vertical, 6)
        .background(tint.opacity(0.10))
        .clipShape(Capsule())
    }

    private var bioCard: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Sobre el especialista").headlineMD()
            Text(doctor.bio).bodyMD(AppColor.textBody)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .cardStyle()
    }

    // MARK: - Selección de fecha

    private var dateSection: some View {
        VStack(alignment: .leading, spacing: AppSpacing.sm) {
            Text("Fecha y Horario").headlineMD()

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: AppSpacing.sm) {
                    ForEach(selectableDates, id: \.self) { date in
                        dateChip(date)
                    }
                }
                .padding(.horizontal, 2)
            }
        }
    }

    private func dateChip(_ date: Date) -> some View {
        let selected = isSameDay(date, selectedDate)
        let weekday = Self.weekdayFormatter.string(from: date)
        let day = Self.dayFormatter.string(from: date)

        return Button {
            withAnimation(.easeOut(duration: 0.2)) { selectedDate = date }
        } label: {
            VStack(spacing: 6) {
                Text(weekday.uppercased())
                    .font(AppFont.labelSM(selected ? .white : AppColor.textMuted))
                Text(day)
                    .font(AppFont.headlineMD())
                    .foregroundStyle(selected ? .white : AppColor.textPrimary)
                    .monospacedDigit()
            }
            .frame(width: 62, height: 68)
            .background(selected ? AppColor.primary : AppColor.surface)
            .clipShape(RoundedRectangle(cornerRadius: AppRadius.md, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: AppRadius.md, style: .continuous)
                    .stroke(selected ? .clear : AppColor.border, lineWidth: 1)
            )
        }
        .pressable()
    }

    // MARK: - Selección de hora

    private var timeSection: some View {
        VStack(alignment: .leading, spacing: AppSpacing.sm) {
            switch doctorVM.slotsState.status {
            case .loading:
                LoadingView(message: "Cargando horarios...")
            case .error:
                ErrorView(message: doctorVM.slotsState.message ?? "Error",
                          onRetry: { doctorVM.loadSlots(doctorId: doctor.id) })
            case .success:
                ForEach(SlotPeriod.allCases) { period in
                    let slots = doctorVM.slots(for: period)
                    if !slots.isEmpty {
                        HStack(spacing: 6) {
                            Image(systemName: period.icon)
                                .font(.system(size: 13, weight: .semibold))
                                .foregroundStyle(AppColor.primary)
                            Text(period.rawValue).labelLG()
                        }
                        .padding(.top, 4)

                        LazyVGrid(columns: [GridItem(.adaptive(minimum: 84), spacing: AppSpacing.sm)],
                                  spacing: AppSpacing.sm) {
                            ForEach(slots) { slot in
                                slotChip(slot)
                            }
                        }
                    }
                }
            }
        }
    }

    private func slotChip(_ slot: TimeSlot) -> some View {
        let selected = selectedSlot?.id == slot.id
        return Button {
            guard slot.available else { return }
            withAnimation(.easeOut(duration: 0.15)) { selectedSlot = slot }
        } label: {
            Text(slot.time)
                .font(AppFont.labelLG())
                .foregroundStyle(slotForeground(selected: selected, available: slot.available))
                .frame(maxWidth: .infinity)
                .frame(height: 44)
                .background(slotBackground(selected: selected, available: slot.available))
                .clipShape(RoundedRectangle(cornerRadius: AppRadius.md, style: .continuous))
                .overlay(
                    RoundedRectangle(cornerRadius: AppRadius.md, style: .continuous)
                        .stroke(selected ? .clear : AppColor.border,
                                style: slot.available ? StrokeStyle(lineWidth: 1)
                                                      : StrokeStyle(lineWidth: 1, dash: [4, 3]))
                )
                .shadow(color: selected ? AppColor.primary.opacity(0.25) : .clear,
                        radius: 6, x: 0, y: 2)
        }
        .disabled(!slot.available)
    }

    private func slotForeground(selected: Bool, available: Bool) -> Color {
        if selected { return .white }
        return available ? AppColor.textBody : AppColor.textMuted
    }

    private func slotBackground(selected: Bool, available: Bool) -> Color {
        if selected { return AppColor.primary }
        return available ? AppColor.surface : AppColor.surfaceLevel2
    }

    // MARK: - Motivo

    private var reasonSection: some View {
        VStack(alignment: .leading, spacing: AppSpacing.sm) {
            HStack {
                Text("Motivo de consulta").headlineMD()
                Spacer()
                Text("Opcional").labelSM()
            }
            TextEditor(text: $reason)
                .font(AppFont.bodyMD())
                .foregroundStyle(AppColor.textPrimary)
                .scrollContentBackground(.hidden)
                .frame(height: 96)
                .padding(10)
                .background(AppColor.surface)
                .clipShape(RoundedRectangle(cornerRadius: AppRadius.md, style: .continuous))
                .overlay(
                    RoundedRectangle(cornerRadius: AppRadius.md, style: .continuous)
                        .stroke(AppColor.border, lineWidth: 1)
                )
        }
    }

    // MARK: - Barra de confirmación

    private var confirmBar: some View {
        VStack(spacing: 8) {
            if let slot = selectedSlot {
                HStack(spacing: 6) {
                    Image(systemName: "checkmark.circle.fill").foregroundStyle(AppColor.mint)
                    Text("\(formattedSelectedDate) · \(slot.time)")
                        .font(AppFont.labelMD(AppColor.textBody))
                }
            }
            PrimaryButton(title: "Confirmar y Reservar", icon: "checkmark.circle.fill",
                          isEnabled: selectedSlot != nil) {
                confirm()
            }
        }
        .padding(.horizontal, AppSpacing.margin)
        .padding(.top, AppSpacing.sm)
        .padding(.bottom, AppSpacing.sm)
        .background(.ultraThinMaterial)
        .overlay(alignment: .top) {
            Divider().overlay(AppColor.border)
        }
    }

    private func confirm() {
        guard let slot = selectedSlot else { return }
        let success = appointmentVM.create(
            doctor: doctor,
            date: slot.date,
            time: slot.time,
            patient: user,
            reason: reason.trimmingCharacters(in: .whitespacesAndNewlines))
        if success { showConfirmation = true }
    }

    private var formattedSelectedDate: String {
        guard let selectedDate else { return "" }
        let f = DateFormatter()
        f.locale = Locale(identifier: "es_ES")
        f.dateFormat = "EEE d MMM"
        return f.string(from: selectedDate).capitalized
    }

    private func isSameDay(_ a: Date, _ b: Date?) -> Bool {
        guard let b else { return false }
        return Calendar.current.isDate(a, inSameDayAs: b)
    }

    private static let weekdayFormatter: DateFormatter = {
        let f = DateFormatter(); f.locale = Locale(identifier: "es_ES"); f.dateFormat = "EEE"; return f
    }()
    private static let dayFormatter: DateFormatter = {
        let f = DateFormatter(); f.locale = Locale(identifier: "es_ES"); f.dateFormat = "d"; return f
    }()
}
