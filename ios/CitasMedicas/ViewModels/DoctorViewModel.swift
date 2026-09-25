//
//  DoctorViewModel.swift
//  CitasMedicas
//
//  ViewModel que expone especialidades, doctores y horarios disponibles.
//  Incluye búsqueda y filtrado por especialidad.
//

import Foundation
import Combine

@MainActor
final class DoctorViewModel: ObservableObject {

    @Published private(set) var specialtiesState: Resource<[Specialty]> = .loading()
    @Published private(set) var doctorsState: Resource<[Doctor]> = .loading()
    @Published private(set) var slotsState: Resource<[TimeSlot]> = .loading()

    /// Especialidad seleccionada (nil = "Todas").
    @Published var selectedSpecialtyId: String? = nil
    @Published var searchQuery: String = ""

    private let repository: DoctorRepository
    private var allDoctors: [Doctor] = []

    nonisolated init(repository: DoctorRepository = DoctorRepository()) {
        self.repository = repository
    }

    var specialties: [Specialty] { specialtiesState.data ?? [] }
    var doctors: [Doctor] { doctorsState.data ?? [] }
    var slots: [TimeSlot] { slotsState.data ?? [] }

    // MARK: - Carga

    func loadHome() {
        Task {
            async let specs = repository.fetchSpecialties()
            async let docs = repository.fetchDoctors()
            specialtiesState = .success(await specs)
            let fetched = await docs
            allDoctors = fetched
            doctorsState = .success(fetched)
        }
    }

    func loadDoctors() {
        doctorsState = .loading()
        Task {
            let fetched = await repository.fetchDoctors()
            allDoctors = fetched
            doctorsState = .success(fetched)
        }
    }

    func loadSlots(doctorId: String) {
        slotsState = .loading()
        Task {
            slotsState = .success(await repository.fetchSlots(doctorId: doctorId))
        }
    }

    // MARK: - Filtrado

    func selectSpecialty(_ id: String?) {
        selectedSpecialtyId = id
        applyFilter()
    }

    func search(_ query: String) {
        searchQuery = query
        applyFilter()
    }

    private func applyFilter() {
        var result = allDoctors

        if let specialtyId = selectedSpecialtyId {
            result = result.filter { $0.specialtyId == specialtyId }
        }

        let q = searchQuery.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        if !q.isEmpty {
            result = result.filter {
                $0.fullName.lowercased().contains(q) ||
                $0.specialtyName.lowercased().contains(q) ||
                $0.hospital.lowercased().contains(q)
            }
        }

        doctorsState = .success(result)
    }

    /// Slots de un doctor agrupados por periodo del día.
    func slots(for period: SlotPeriod) -> [TimeSlot] {
        slots.filter { $0.period == period }
    }
}
