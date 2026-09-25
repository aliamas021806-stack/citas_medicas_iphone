//
//  Specialty.swift
//  CitasMedicas
//
//  Especialidad médica (Cardiología, Pediatría, etc.).
//

import Foundation

struct Specialty: Codable, Identifiable, Equatable, Hashable {
    var id: String
    var name: String
    var iconEmoji: String
    var description: String
    var doctorCount: Int
}
