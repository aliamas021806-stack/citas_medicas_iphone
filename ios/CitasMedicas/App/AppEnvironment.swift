//
//  AppEnvironment.swift
//  CitasMedicas
//
//  Contenedor de dependencias (Composition Root). Centraliza la creación de
//  repositorios y ViewModels de nivel app (citas y doctores), evitando
//  instancias duplicadas y facilitando el testeo.
//

import Foundation
import Combine

@MainActor
final class AppEnvironment: ObservableObject {

    // Repositorios compartidos
    let appointmentRepository: AppointmentRepository
    let doctorRepository: DoctorRepository
    let patientRepository: PatientRepository
    let authRepository: AuthRepository
    let session: SessionManager

    // ViewModels de nivel app (persisten durante toda la sesión)
    let appointments: AppointmentViewModel
    let doctors: DoctorViewModel

    init() {
        let session = SessionManager.shared
        let appointmentRepository = AppointmentRepository()
        let doctorRepository = DoctorRepository()
        let patientRepository = PatientRepository()
        let authRepository = AuthRepository()

        self.session = session
        self.appointmentRepository = appointmentRepository
        self.doctorRepository = doctorRepository
        self.patientRepository = patientRepository
        self.authRepository = authRepository
        self.appointments = AppointmentViewModel(repository: appointmentRepository)
        self.doctors = DoctorViewModel(repository: doctorRepository)
    }

    /// Crea el ViewModel de autenticación reutilizando los repositorios app.
    func makeAuthViewModel() -> AuthViewModel {
        AuthViewModel(repository: authRepository, session: session)
    }
}
