//
//  Resource.swift
//  CitasMedicas
//
//  Envoltorio genérico para representar estados de carga / éxito / error,
//  equivalente a la clase Resource<T> del proyecto Android.
//

import Foundation

enum ResourceStatus {
    case loading
    case success
    case error
}

struct Resource<T> {
    let status: ResourceStatus
    let data: T?
    let message: String?

    static func loading() -> Resource<T> {
        Resource(status: .loading, data: nil, message: nil)
    }

    static func success(_ data: T) -> Resource<T> {
        Resource(status: .success, data: data, message: nil)
    }

    static func error(_ message: String, data: T? = nil) -> Resource<T> {
        Resource(status: .error, data: data, message: message)
    }

    var isLoading: Bool { status == .loading }
    var isSuccess: Bool { status == .success }
    var isError: Bool { status == .error }
}
