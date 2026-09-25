//
//  NewAppointmentView.swift
//  CitasMedicas
//
//  Flujo "Nueva Cita" en pasos (Paciente -> Médico -> Horario -> Detalles)
//  con barra de progreso y modalidad. Fusiona el patrón de la pantalla
//  "nueva_cita_médica" del ZIP de diseño con la lógica del repositorio.
//

import SwiftUI

struct NewAppointmentView: View {

    @EnvironmentObject private var env: AppEnvironment
    @EnvironmentObject private var appointmentVM: AppointmentViewModel
    @EnvironmentObject private var doctorVM: DoctorViewModel
    @Environment(\.dismiss) private var dismiss

    @State private var step = 1
    @State private var selectedSpecialtyId: String?
    @State private var selectedDoctor: Doctor?
    @State private var selectedDate: Date?
    @State private var selectedSlot: TimeSlot?
    @State private var reason = ""
    @State private var modality: Modality = .inPerson
    @State private var reminderOn = true
    @State private var showSuccess = false

    @StateObject private var slotLoader = DoctorViewModel()

    private var user: User { env.session.currentUser ?? .placeholder }
    private var selectableDates: [Date] { MockData.shared.selectableDates() }

    private var filteredDoctors: [Doctor] {
        guard let id = selectedSpecialtyId else { return doctorVM.doctors }
        return doctorVM.doctors.filter { $0.specialtyId == id }
    }

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                stepProgress
                    .padding(.horizontal, AppSpacing.margin)
                    .padding(.vertical, AppSpacing.sm)

                ScrollView {
                    VStack(alignment: .leading, spacing: AppSpacing.md) {
                        switch step {
                        case 1:
                            patientCard
                            specialtyPicker
                            doctorPicker
                        case 2:
                            doctorSummary
                            dateSection
                            timeSection
                        default:
                            detailsSection
                        }
                    }
                    .padding(.horizontal, AppSpacing.margin)
                    .padding(.bottom, AppSpacing.md)
                }

                bottomBar
            }
            .background(AppColor.background)
            .navigationTitle("Nueva Cita")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cerrar") { dismiss() }
                }
            }
            .task { if doctorVM.doctors.isEmpty { doctorVM.loadHome() } }
            .alert("¡Cita confirmada!", isPresented: $showSuccess) {
                Button("Listo") {
                    appointmentVM.consumeCreateEvent()
                    dismiss()
                }
            } message: {
                if let doctor = selectedDoctor {
                    Text("\(doctor.displayName)\n\(formattedDate) · \(selectedSlot?.time ?? "")")
                }
            }
        }
    }

    // MARK: - Progreso

    private var stepProgress: some View {
        HStack(spacing: 6) {
            stepDot(index: 1, label: "Médico")
            stepLine(active: step > 1)
            stepDot(index: 2, label: "Horario")
            stepLine(active: step > 2)
            stepDot(index: 3, label: "Detalles")
        }
        .cardStyle(padding: AppSpacing.sm)
    }

    private func stepDot(index: Int, label: String) -> some View {
        let done = step > index
        let active = step == index
        return VStack(spacing: 4) {
            ZStack {
                Circle()
                    .fill(done ? AppColor.mint : (active ? AppColor.primary : AppColor.surfaceLevel2))
                    .frame(width: 30, height: 30)
                if done {
                    Image(systemName: "checkmark").font(.system(size: 13, weight: .bold)).foregroundStyle(.white)
                } else {
                    Text("\(index)").font(AppFont.labelMD(active ? .white : AppColor.textMuted))
                }
            }
            Text(label).font(AppFont.labelSM(active ? AppColor.primary : AppColor.textMuted))
        }
    }

    private func stepLine(active: Bool) -> some View {
        Rectangle().fill(active ? AppColor.mint : AppColor.border)
            .frame(height: 2).frame(maxWidth: .infinity)
            .padding(.bottom, 16)
    }

    // MARK: - Paso 1

    private var patientCard: some View {
        HStack(spacing: AppSpacing.sm) {
            AvatarView(initials: user.initials, size: 46, tint: AppColor.primary)
            VStack(alignment: .leading, spacing: 2) {
                Text("PACIENTE VERIFICADO").labelSM(AppColor.textMuted)
                Text(user.fullName).headlineMD()
                if user.age > 0 { Text("\(user.age) años").bodySM() }
            }
            Spacer()
            Image(systemName: "checkmark.seal.fill").foregroundStyle(AppColor.mint)
        }
        .cardStyle()
    }

    private var specialtyPicker: some View {
        VStack(alignment: .leading, spacing: AppSpacing.sm) {
            Text("Especialidad").headlineMD()
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: AppSpacing.sm) {
                    ForEach(doctorVM.specialties) { specialty in
                        let selected = selectedSpecialtyId == specialty.id
                        Button {
                            withAnimation(.easeOut(duration: 0.2)) {
                                selectedSpecialtyId = specialty.id
                                selectedDoctor = nil
                            }
                        } label: {
                            HStack(spacing: 6) {
                                Text(specialty.iconEmoji)
                                Text(specialty.name).font(AppFont.labelMD())
                            }
                            .foregroundStyle(selected ? .white : AppColor.textBody)
                            .padding(.horizontal, 14).padding(.vertical, 9)
                            .background(selected ? AppColor.primary : AppColor.surface)
                            .clipShape(Capsule())
                            .overlay(Capsule().stroke(selected ? .clear : AppColor.border, lineWidth: 1))
                        }
                        .pressable()
                    }
                }
                .padding(.horizontal, 2)
            }
        }
    }

    private var doctorPicker: some View {
        VStack(alignment: .leading, spacing: AppSpacing.sm) {
            Text("Especialista").headlineMD()
            if filteredDoctors.isEmpty {
                EmptyStateView(icon: "stethoscope", title: "Selecciona una especialidad",
                               message: "Elige una especialidad para ver los médicos disponibles.")
            } else {
                LazyVStack(spacing: AppSpacing.sm) {
                    ForEach(filteredDoctors) { doctor in
                        Button {
                            withAnimation(.easeOut(duration: 0.2)) {
                                selectedDoctor = doctor
                                selectedSlot = nil
                                slotLoader.loadSlots(doctorId: doctor.id)
                            }
                        } label: {
                            doctorSelectRow(doctor, selected: selectedDoctor?.id == doctor.id)
                        }
                        .buttonStyle(.plain)
                    }
                }
            }
        }
    }

    private func doctorSelectRow(_ doctor: Doctor, selected: Bool) -> some View {
        HStack(spacing: AppSpacing.sm) {
            AvatarView(initials: doctor.initials, size: 46, tint: AppColor.primary)
            VStack(alignment: .leading, spacing: 2) {
                Text(doctor.displayName).headlineMD()
                Text("\(doctor.specialtyName) · \(doctor.hospital)").bodySM()
            }
            Spacer()
            Image(systemName: selected ? "checkmark.circle.fill" : "circle")
                .foregroundStyle(selected ? AppColor.primary : AppColor.textMuted)
                .font(.system(size: 20))
        }
        .cardStyle(padding: AppSpacing.sm + 4)
        .overlay(
            RoundedRectangle(cornerRadius: AppRadius.lg, style: .continuous)
                .stroke(selected ? AppColor.primary : .clear, lineWidth: 1.5)
        )
    }

    // MARK: - Paso 2

    private var doctorSummary: some View {
        Group {
            if let doctor = selectedDoctor {
                HStack(spacing: AppSpacing.sm) {
                    AvatarView(initials: doctor.initials, size: 42, tint: AppColor.primary)
                    VStack(alignment: .leading, spacing: 2) {
                        Text("ESPECIALISTA ASIGNADO").labelSM(AppColor.textMuted)
                        Text(doctor.displayName).headlineMD()
                    }
                    Spacer()
                    Button("Cambiar") { withAnimation { step = 1 } }
                        .font(AppFont.labelMD(AppColor.primary))
                }
                .cardStyle()
            }
        }
    }

    private var dateSection: some View {
        VStack(alignment: .leading, spacing: AppSpacing.sm) {
            Text("Fecha").headlineMD()
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: AppSpacing.sm) {
                    ForEach(selectableDates, id: \.self) { date in
                        let selected = Calendar.current.isDate(date, inSameDayAs: selectedDate ?? .distantPast)
                        let weekday = Self.weekday.string(from: date)
                        let day = Self.day.string(from: date)
                        Button {
                            withAnimation(.easeOut(duration: 0.2)) {
                                selectedDate = date
                                selectedSlot = nil
                            }
                        } label: {
                            VStack(spacing: 5) {
                                Text(weekday.uppercased()).font(AppFont.labelSM(selected ? .white : AppColor.textMuted))
                                Text(day).font(AppFont.headlineMD(selected ? .white : AppColor.textPrimary)).monospacedDigit()
                            }
                            .frame(width: 58, height: 64)
                            .background(selected ? AppColor.primary : AppColor.surface)
                            .clipShape(RoundedRectangle(cornerRadius: AppRadius.md, style: .continuous))
                            .overlay(RoundedRectangle(cornerRadius: AppRadius.md, style: .continuous)
                                .stroke(selected ? .clear : AppColor.border, lineWidth: 1))
                        }
                        .pressable()
                    }
                }
                .padding(.horizontal, 2)
            }
        }
    }

    private var timeSection: some View {
        VStack(alignment: .leading, spacing: AppSpacing.sm) {
            Text("Horario disponible").headlineMD()

            if selectedDoctor == nil {
                Text("Selecciona primero un especialista.").bodySM()
            } else if (slotLoader.slotsState.isLoading) {
                LoadingView(message: "Buscando horarios...")
            } else {
                let slotsForDay = filteredSlots
                if slotsForDay.isEmpty {
                    Text("No hay horarios para esta fecha.").bodySM()
                } else {
                    ForEach(SlotPeriod.allCases) { period in
                        let slots = slotsForDay.filter { $0.period == period }
                        if !slots.isEmpty {
                            HStack(spacing: 6) {
                                Image(systemName: period.icon)
                                    .font(.system(size: 13, weight: .semibold))
                                    .foregroundStyle(AppColor.primary)
                                Text(period.rawValue).labelLG()
                            }
                            .padding(.top, 2)

                            LazyVGrid(columns: [GridItem(.adaptive(minimum: 84), spacing: AppSpacing.sm)],
                                      spacing: AppSpacing.sm) {
                                ForEach(slots) { slot in
                                    let selected = selectedSlot?.id == slot.id
                                    Button {
                                        guard slot.available else { return }
                                        withAnimation(.easeOut(duration: 0.15)) { selectedSlot = slot }
                                    } label: {
                                        Text(slot.time)
                                            .font(AppFont.labelLG(selected ? .white : (slot.available ? AppColor.textBody : AppColor.textMuted)))
                                            .frame(maxWidth: .infinity).frame(height: 44)
                                            .background(selected ? AppColor.primary : (slot.available ? AppColor.surface : AppColor.surfaceLevel2))
                                            .clipShape(RoundedRectangle(cornerRadius: AppRadius.md, style: .continuous))
                                            .overlay(RoundedRectangle(cornerRadius: AppRadius.md, style: .continuous)
                                                .stroke(selected ? .clear : AppColor.border,
                                                        style: slot.available ? StrokeStyle(lineWidth: 1) : StrokeStyle(lineWidth: 1, dash: [4, 3])))
                                    }
                                    .disabled(!slot.available)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private var filteredSlots: [TimeSlot] {
        guard let date = selectedDate else { return slotLoader.slots }
        let dateString = MockData.dateFormatter.string(from: date)
        return slotLoader.slots.filter { $0.date == dateString }
    }

    // MARK: - Paso 3

    private var detailsSection: some View {
        VStack(alignment: .leading, spacing: AppSpacing.md) {
            VStack(alignment: .leading, spacing: AppSpacing.sm) {
                Text("Modalidad").headlineMD()
                HStack(spacing: AppSpacing.sm) {
                    modalityButton(.inPerson)
                    modalityButton(.telemedicine)
                }
            }

            VStack(alignment: .leading, spacing: AppSpacing.sm) {
                HStack {
                    Text("Motivo de consulta").headlineMD()
                    Spacer()
                    Text("Opcional").labelSM()
                }
                TextEditor(text: $reason)
                    .font(AppFont.bodyMD())
                    .scrollContentBackground(.hidden)
                    .frame(height: 100)
                    .padding(10)
                    .background(AppColor.surface)
                    .clipShape(RoundedRectangle(cornerRadius: AppRadius.md, style: .continuous))
                    .overlay(RoundedRectangle(cornerRadius: AppRadius.md, style: .continuous)
                        .stroke(AppColor.border, lineWidth: 1))
            }

            Toggle(isOn: $reminderOn) {
                VStack(alignment: .leading, spacing: 2) {
                    Text("Recordatorio digital").labelLG()
                    Text("Notificación 24 h antes").labelSM()
                }
            }
            .tint(AppColor.mint)
            .padding(AppSpacing.sm)
            .background(AppColor.surface)
            .clipShape(RoundedRectangle(cornerRadius: AppRadius.md, style: .continuous))
            .overlay(RoundedRectangle(cornerRadius: AppRadius.md, style: .continuous)
                .stroke(AppColor.border, lineWidth: 1))

            summaryCard
        }
    }

    private func modalityButton(_ modality: Modality) -> some View {
        let selected = self.modality == modality
        return Button {
            withAnimation(.easeOut(duration: 0.2)) { self.modality = modality }
        } label: {
            VStack(spacing: 6) {
                Image(systemName: modality.icon).font(.system(size: 20, weight: .semibold))
                Text(modality.title).font(AppFont.labelMD())
            }
            .frame(maxWidth: .infinity).frame(height: 76)
            .foregroundStyle(selected ? .white : AppColor.textBody)
            .background(selected ? AppColor.primary : AppColor.surface)
            .clipShape(RoundedRectangle(cornerRadius: AppRadius.md, style: .continuous))
            .overlay(RoundedRectangle(cornerRadius: AppRadius.md, style: .continuous)
                .stroke(selected ? .clear : AppColor.border, lineWidth: 1))
        }
        .pressable()
    }

    private var summaryCard: some View {
        VStack(alignment: .leading, spacing: AppSpacing.sm) {
            Text("Resumen").headlineMD()
            if let doctor = selectedDoctor {
                summaryRow(icon: "stethoscope", text: doctor.displayName)
                summaryRow(icon: "calendar", text: "\(formattedDate) · \(selectedSlot?.time ?? "--:--")")
                summaryRow(icon: modality.icon, text: modality.title)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .cardStyle()
    }

    private func summaryRow(icon: String, text: String) -> some View {
        HStack(spacing: AppSpacing.sm) {
            Image(systemName: icon).font(.system(size: 14, weight: .medium))
                .foregroundStyle(AppColor.primary).frame(width: 22)
            Text(text).bodyMD(AppColor.textPrimary)
            Spacer()
        }
    }

    // MARK: - Barra inferior

    private var bottomBar: some View {
        HStack(spacing: AppSpacing.sm) {
            if step > 1 {
                SecondaryButton(title: "Atrás", icon: "chevron.left") {
                    withAnimation(.easeOut(duration: 0.2)) { step -= 1 }
                }
                .frame(maxWidth: 120)
            }

            PrimaryButton(
                title: step < 3 ? "Continuar" : "Confirmar y Reservar",
                icon: step < 3 ? "arrow.right" : "checkmark.circle.fill",
                isEnabled: canAdvance
            ) {
                if step < 3 {
                    withAnimation(.easeOut(duration: 0.2)) { step += 1 }
                } else {
                    confirm()
                }
            }
        }
        .padding(.horizontal, AppSpacing.margin)
        .padding(.vertical, AppSpacing.sm)
        .background(.ultraThinMaterial)
        .overlay(alignment: .top) { Divider().overlay(AppColor.border) }
    }

    private var canAdvance: Bool {
        switch step {
        case 1: return selectedDoctor != nil
        case 2: return selectedSlot != nil
        default: return selectedDoctor != nil && selectedSlot != nil
        }
    }

    // MARK: - Lógica

    private func confirm() {
        guard let doctor = selectedDoctor, let slot = selectedSlot else { return }
        let success = appointmentVM.create(
            doctor: doctor, date: slot.date, time: slot.time,
            patient: user, reason: reason.trimmingCharacters(in: .whitespacesAndNewlines))
        if success { showSuccess = true }
    }

    private var formattedDate: String {
        guard let selectedDate else { return "—" }
        let f = DateFormatter()
        f.locale = Locale(identifier: "es_ES")
        f.dateFormat = "EEE d MMM"
        return f.string(from: selectedDate).capitalized
    }

    private static let weekday: DateFormatter = {
        let f = DateFormatter(); f.locale = Locale(identifier: "es_ES"); f.dateFormat = "EEE"; return f
    }()
    private static let day: DateFormatter = {
        let f = DateFormatter(); f.locale = Locale(identifier: "es_ES"); f.dateFormat = "d"; return f
    }()
}

enum Modality: CaseIterable, Identifiable {
    case inPerson, telemedicine
    var id: String { title }
    var title: String { self == .inPerson ? "Presencial" : "Telemedicina" }
    var icon: String { self == .inPerson ? "building.2.fill" : "video.fill" }
}
