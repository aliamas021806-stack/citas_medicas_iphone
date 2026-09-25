//
//  Persistence.swift
//  CitasMedicas
//
//  Persistencia local de citas. Implementada sobre el sistema de archivos
//  (JSON) — análoga a Room/SQLite del proyecto Android — para que las citas
//  sobrevivan al cierre de la app. Se ofrece además un protocolo
//  `AppointmentStore` para poder sustituir la implementación por CoreData sin
//  tocar los ViewModels.
//

import Foundation

/// Abstracción del almacén de citas (permite sustituir por CoreData).
protocol AppointmentStore {
    func load() -> [Appointment]
    func save(_ appointments: [Appointment])
}

/// Almacén local basado en archivo JSON en el directorio de documentos.
final class FileAppointmentStore: AppointmentStore {

    private let fileURL: URL
    private let encoder: JSONEncoder
    private let decoder: JSONDecoder

    init(fileName: String = "appointments.json") {
        let dir = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
        self.fileURL = dir.appendingPathComponent(fileName)

        encoder = JSONEncoder()
        encoder.dateEncodingStrategy = .iso8601
        encoder.outputFormatting = [.prettyPrinted]

        decoder = JSONDecoder()
        decoder.dateDecodingStrategy = .iso8601
    }

    func load() -> [Appointment] {
        guard let data = try? Data(contentsOf: fileURL) else { return [] }
        return (try? decoder.decode([Appointment].self, from: data)) ?? []
    }

    func save(_ appointments: [Appointment]) {
        guard let data = try? encoder.encode(appointments) else { return }
        try? data.write(to: fileURL, options: [.atomic])
    }
}
