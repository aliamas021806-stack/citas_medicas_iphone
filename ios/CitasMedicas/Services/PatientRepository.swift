//
//  PatientRepository.swift
//  CitasMedicas
//
//  Repositorio de PACIENTES. Obtiene la información del paciente (incluida su
//  EDAD) desde los datos sintéticos "generados por IA".
//

import Foundation

final class PatientRepository {

    private let mock = MockData.shared
    private let fakeDelay: UInt64 = 300_000_000 // 0.3 s

    func fetchPatients() async -> [Patient] {
        try? await Task.sleep(nanoseconds: fakeDelay)
        return mock.patients
    }

    func fetchPatient(id: String) async -> Patient? {
        try? await Task.sleep(nanoseconds: fakeDelay)
        return mock.patient(byId: id)
    }

    var averageAge: Double { mock.averagePatientAge }
}
