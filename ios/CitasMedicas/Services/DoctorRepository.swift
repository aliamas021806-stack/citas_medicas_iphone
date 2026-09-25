//
//  DoctorRepository.swift
//  CitasMedicas
//
//  Repositorio de doctores, especialidades y horarios. Consume MockData con
//  un pequeño delay para simular latencia de red (equivalente a la API mock
//  del proyecto Android, pero sin backend real).
//

import Foundation

final class DoctorRepository {

    private let mock = MockData.shared
    private let fakeDelay: UInt64 = 400_000_000 // 0.4 s

    func fetchSpecialties() async -> [Specialty] {
        try? await Task.sleep(nanoseconds: fakeDelay)
        return mock.specialties
    }

    func fetchDoctors() async -> [Doctor] {
        try? await Task.sleep(nanoseconds: fakeDelay)
        return mock.doctors.sorted { $0.rating > $1.rating }
    }

    func fetchDoctors(specialtyId: String) async -> [Doctor] {
        try? await Task.sleep(nanoseconds: fakeDelay)
        return mock.doctors(bySpecialty: specialtyId)
    }

    func fetchDoctor(id: String) async -> Doctor? {
        try? await Task.sleep(nanoseconds: fakeDelay)
        return mock.doctor(byId: id)
    }

    func fetchSlots(doctorId: String) async -> [TimeSlot] {
        try? await Task.sleep(nanoseconds: fakeDelay)
        return mock.availableSlots(for: doctorId)
    }
}
