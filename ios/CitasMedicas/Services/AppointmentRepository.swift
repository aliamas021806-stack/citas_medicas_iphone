//
//  AppointmentRepository.swift
//  CitasMedicas
//
//  Repositorio de citas médicas con PERSISTENCIA LOCAL (archivo JSON).
//  Mantiene las citas en memoria como caché y delega la escritura en el
//  AppointmentStore. Expone operaciones asíncronas compatibles con MVVM.
//

import Foundation

enum AppointmentError: LocalizedError {
    case overlapping
    case notCancellable

    var errorDescription: String? {
        switch self {
        case .overlapping:   return "Ya tienes una cita agendada a esa misma hora"
        case .notCancellable: return "Esta cita ya no se puede cancelar"
        }
    }
}

final class AppointmentRepository {

    private let store: AppointmentStore
    private(set) var appointments: [Appointment]

    init(store: AppointmentStore = FileAppointmentStore()) {
        self.store = store
        self.appointments = store.load()
        seedIfEmpty()
    }

    // MARK: - Lectura

    /// Citas ordenadas por fecha y hora.
    func fetchAll() -> [Appointment] {
        appointments.sorted { lhs, rhs in
            if lhs.date == rhs.date { return lhs.time < rhs.time }
            return lhs.date < rhs.date
        }
    }

    // MARK: - Escritura

    /// Crea una cita validando que no se solape con otra activa.
    @discardableResult
    func create(_ appointment: Appointment) throws -> Appointment {
        let overlaps = appointments.contains {
            $0.date == appointment.date &&
            $0.time == appointment.time &&
            $0.status != .cancelled
        }
        guard !overlaps else { throw AppointmentError.overlapping }

        appointments.append(appointment)
        persist()
        return appointment
    }

    /// Cancela una cita (cambia su estado a `cancelled`).
    func cancel(id: String) throws {
        guard let index = appointments.firstIndex(where: { $0.id == id }) else { return }
        guard appointments[index].isCancellable else { throw AppointmentError.notCancellable }
        appointments[index].status = .cancelled
        persist()
    }

    /// Elimina una cita definitivamente.
    func delete(id: String) {
        appointments.removeAll { $0.id == id }
        persist()
    }

    // MARK: - Sembrado inicial

    /// Inserta citas de ejemplo la primera vez que se abre la app
    /// (equivale al SEED_CALLBACK de Room).
    private func seedIfEmpty() {
        guard appointments.isEmpty else { return }
        let dates = MockData.shared.selectableDates()
        guard dates.count >= 4 else { return }

        func dateString(_ index: Int) -> String {
            MockData.dateFormatter.string(from: dates[min(index, dates.count - 1)])
        }

        let seeded: [Appointment] = [
            Appointment(doctorId: "doc_01", doctorName: "Dr. Carlos Ramírez",
                        specialtyName: "Cardiología", patientName: "Laura Jiménez",
                        patientAge: 34, date: dateString(1), time: "10:00",
                        status: .confirmed, reason: "Chequeo cardiológico anual"),
            Appointment(doctorId: "doc_02", doctorName: "Dra. María González",
                        specialtyName: "Pediatría", patientName: "Laura Jiménez",
                        patientAge: 34, date: dateString(3), time: "16:00",
                        status: .pending, reason: "Control de crecimiento"),
            Appointment(doctorId: "doc_03", doctorName: "Dra. Andrea López",
                        specialtyName: "Dermatología", patientName: "Laura Jiménez",
                        patientAge: 34, date: dateString(0), time: "09:00",
                        status: .cancelled, reason: "Revisión de lunar")
        ]
        appointments = seeded
        persist()
    }

    private func persist() {
        store.save(appointments)
    }
}
