//
//  AppointmentViewModel.swift
//  CitasMedicas
//
//  ViewModel de citas médicas. Observa el repositorio local y gestiona
//  crear / cancelar, con estados de acción y un evento de navegación.
//

import Foundation
import Combine

@MainActor
final class AppointmentViewModel: ObservableObject {

    @Published private(set) var appointments: [Appointment] = []
    @Published private(set) var actionState: Resource<Void>? = nil

    /// Evento de un solo disparo: cita creada (para cerrar la pantalla).
    @Published var didCreateAppointment = false

    private let repository: AppointmentRepository

    init(repository: AppointmentRepository) {
        self.repository = repository
        reload()
    }

    // MARK: - Derivados

    var confirmedCount: Int { appointments.filter { $0.status == .confirmed }.count }
    var pendingCount: Int   { appointments.filter { $0.status == .pending }.count }
    var cancelledCount: Int { appointments.filter { $0.status == .cancelled }.count }

    /// Próxima cita futura (o la primera listada).
    var nextAppointment: Appointment? {
        let active = appointments.filter { $0.status != .cancelled }
        if let upcoming = active.first(where: { ($0.dateValue ?? .distantPast) >= Calendar.current.startOfDay(for: Date()) }) {
            return upcoming
        }
        return active.first
    }

    var actionErrorMessage: String? {
        guard let state = actionState, state.isError else { return nil }
        return state.message
    }

    var isProcessing: Bool { actionState?.isLoading ?? false }

    // MARK: - Operaciones

    func reload() {
        appointments = repository.fetchAll()
    }

    /// Crea la cita. Devuelve `true` si tuvo éxito.
    @discardableResult
    func create(doctor: Doctor, date: String, time: String,
                patient: User, reason: String) -> Bool {
        let appointment = Appointment(
            doctorId: doctor.id,
            doctorName: doctor.displayName,
            specialtyName: doctor.specialtyName,
            patientName: patient.fullName,
            patientAge: patient.age,
            date: date,
            time: time,
            status: .confirmed,
            reason: reason)

        do {
            try repository.create(appointment)
            reload()
            actionState = .success(())
            didCreateAppointment = true
            return true
        } catch {
            actionState = .error(error.localizedDescription)
            return false
        }
    }

    func cancel(_ appointment: Appointment) {
        actionState = .loading()
        do {
            try repository.cancel(id: appointment.id)
            reload()
            actionState = .success(())
        } catch {
            actionState = .error(error.localizedDescription)
        }
    }

    func delete(_ appointment: Appointment) {
        repository.delete(id: appointment.id)
        reload()
    }

    func clearActionState() {
        actionState = nil
    }

    func consumeCreateEvent() {
        didCreateAppointment = false
    }
}
