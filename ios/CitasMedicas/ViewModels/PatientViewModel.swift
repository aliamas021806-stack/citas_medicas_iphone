//
//  PatientViewModel.swift
//  CitasMedicas
//
//  ViewModel del PACIENTE. Carga la información del paciente (incluida su
//  EDAD) desde los datos sintéticos "generados por IA".
//

import Foundation
import Combine

@MainActor
final class PatientViewModel: ObservableObject {

    @Published private(set) var patientState: Resource<Patient> = .loading()
    @Published private(set) var patientsState: Resource<[Patient]> = .loading()

    private let repository: PatientRepository

    init(repository: PatientRepository = PatientRepository()) {
        self.repository = repository
    }

    var patient: Patient? { patientState.data }

    func loadPatient(id: String) {
        patientState = .loading()
        Task {
            if let p = await repository.fetchPatient(id: id) {
                patientState = .success(p)
            } else {
                patientState = .error("No se encontró el paciente")
            }
        }
    }

    func loadPatients() {
        patientsState = .loading()
        Task {
            patientsState = .success(await repository.fetchPatients())
        }
    }
}
