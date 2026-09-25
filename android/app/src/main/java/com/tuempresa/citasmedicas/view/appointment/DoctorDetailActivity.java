package com.tuempresa.citasmedicas.view.appointment;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.tuempresa.citasmedicas.R;
import com.tuempresa.citasmedicas.data.mock.MockData;
import com.tuempresa.citasmedicas.model.Appointment;
import com.tuempresa.citasmedicas.model.AppointmentStatus;
import com.tuempresa.citasmedicas.model.Doctor;
import com.tuempresa.citasmedicas.model.Patient;
import com.tuempresa.citasmedicas.model.TimeSlot;
import com.tuempresa.citasmedicas.model.User;
import com.tuempresa.citasmedicas.util.Resource;
import com.tuempresa.citasmedicas.util.SessionManager;
import com.tuempresa.citasmedicas.viewmodel.AppointmentViewModel;
import com.tuempresa.citasmedicas.viewmodel.DoctorViewModel;
import com.tuempresa.citasmedicas.viewmodel.PatientViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;

/**
 * Detalle del doctor + selección de fecha/hora para agendar la cita.
 *
 * <p><b>FIX del flujo doctor -> fecha -> horario -> confirmar.</b> Los cambios
 * clave respecto al original son:
 * <ul>
 *   <li>{@link #slotAdapter} se crea UNA sola vez en {@link #setupLists()} con su
 *       listener real; {@link #renderSlots(Resource)} ahora actualiza sus datos
 *       con {@link SlotAdapter#setData(List)} en lugar de crear un adaptador
 *       nuevo con una lambda vacía. Antes, cada emisión del LiveData sustituía
 *       el adaptador y borraba la selección, dejando el botón de confirmar
 *       "bloqueado" para siempre.</li>
 *   <li>{@link #onSlotSelected(TimeSlot)} habilita/actualiza la pista visual y el
 *       botón en cuanto hay un horario marcado.</li>
 *   <li>{@link #onConfirmClicked()} conserva la validación defensiva: si no hay
 *       slot, avisa con un mensaje claro en vez de fallar en silencio.</li>
 * </ul>
 */
public class DoctorDetailActivity extends AppCompatActivity {

    public static final String EXTRA_DOCTOR = "extra_doctor";
    /** Paciente por defecto usado para recuperar la edad desde la API mock. */
    private static final String DEFAULT_PATIENT_ID = "pat_01";

    private Doctor doctor;
    private User sessionUser;

    private DateAdapter dateAdapter;
    private SlotAdapter slotAdapter;

    private DoctorViewModel doctorViewModel;
    private AppointmentViewModel appointmentViewModel;
    private PatientViewModel patientViewModel;

    private RecyclerView rvDates, rvSlots;
    private TextInputEditText etReason;
    private ProgressBar progressBar;
    private TextView tvDoctorAge;
    private TextView tvSlotHint;
    private TextView tvSlotsEmpty;
    private MaterialButton btnConfirm;

    /** Evita crear la cita dos veces si el usuario toca varias veces el botón. */
    private boolean isSubmitting = false;

    /** Edad del paciente resuelta: primero desde la API, con respaldo en la sesión. */
    private int patientAge;
    /** Nombre del paciente que se guardará en la cita. */
    private String patientName;
    /** Fecha seleccionada actualmente (yyyy-MM-dd). */
    private String selectedDate;
    /** Horarios completos recibidos; se filtran por {@link #selectedDate}. */
    private List<TimeSlot> allSlots = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_detail);

        doctor = (Doctor) getIntent().getSerializableExtra(EXTRA_DOCTOR);
        if (doctor == null) {
            finish();
            return;
        }

        sessionUser = SessionManager.getInstance(this).getUser();
        patientName = sessionUser != null && sessionUser.getFullName() != null
                ? sessionUser.getFullName() : "Paciente";
        patientAge = sessionUser != null ? sessionUser.getAge() : 0;

        bindViews();
        bindDoctor();
        setupLists();
        setupViewModels();

        loadPatientFromApi();
        loadSlotsForSelectedDate(dateAdapter.getSelectedDate());
    }

    private void bindViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarDetail);
        toolbar.setTitle(R.string.book_appointment);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvDoctorAge = findViewById(R.id.tvDoctorAge);
        rvDates = findViewById(R.id.rvDates);
        rvSlots = findViewById(R.id.rvSlots);
        etReason = findViewById(R.id.etReason);
        progressBar = findViewById(R.id.progressBar);
        tvSlotHint = findViewById(R.id.tvSlotHint);
        tvSlotsEmpty = findViewById(R.id.tvSlotsEmpty);
        btnConfirm = findViewById(R.id.btnConfirm);

        btnConfirm.setOnClickListener(v -> onConfirmClicked());
        updateConfirmState();
    }

    private void bindDoctor() {
        ((TextView) findViewById(R.id.tvDoctorAvatar)).setText(doctor.getInitials());
        ((TextView) findViewById(R.id.tvDoctorName)).setText(doctor.getDisplayName());
        ((TextView) findViewById(R.id.tvDoctorSpecialty)).setText(doctor.getSpecialtyName());
        ((TextView) findViewById(R.id.tvDoctorRating)).setText("\u2b50 " + doctor.getRating()
                + "  \u00b7  " + doctor.getYearsExperience() + " a\u00f1os de experiencia");
        tvDoctorAge.setText("\ud83c\udf82 " + doctor.getAge() + " a\u00f1os");
        ((TextView) findViewById(R.id.tvDoctorBio)).setText(doctor.getBio());
    }

    private void setupLists() {
        List<String> dates = MockData.getSelectableDates();
        dateAdapter = new DateAdapter(dates, date -> {
            selectedDate = date;
            renderFilteredSlots(allSlots);
        });
        rvDates.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvDates.setAdapter(dateAdapter);

        // Adaptador de horarios creado UNA vez, con su listener REAL (no vacío).
        slotAdapter = new SlotAdapter(new ArrayList<>(), this::onSlotSelected);
        rvSlots.setLayoutManager(new LinearLayoutManager(this));
        rvSlots.setAdapter(slotAdapter);

        // El scroll vertical lo gestiona el ScrollView padre; el RecyclerView de
        // horarios no debe capturar el gesto de desplazamiento.
        rvSlots.setNestedScrollingEnabled(false);

        // FIX DEFINITIVO de la selección: además del OnClickListener del ítem,
        // enganchamos el gesto táctil DIRECTAMENTE al RecyclerView. Así, aunque el
        // ScrollView padre intercepte el primer toque, la fila tocada se sigue
        // seleccionando de forma fiable.
        attachTapSelection(rvDates, dateAdapter::select);
        attachTapSelection(rvSlots, slotAdapter::select);
    }

    /**
     * Detecta un toque simple (ACTION_UP sin desplazamiento) sobre un RecyclerView
     * y ejecuta la selección de la fila correspondiente. Es una red de seguridad
     * que garantiza que día y hora se puedan elegir en cualquier dispositivo.
     *
     * @param callback recibe la posición tocada y devuelve true si la seleccionó.
     */
    private void attachTapSelection(final RecyclerView rv, final IntPredicate callback) {
        final int touchSlop = ViewConfiguration.get(this).getScaledTouchSlop();
        rv.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
            private float downX, downY;
            private boolean moved;

            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView view,
                                                 @NonNull MotionEvent e) {
                switch (e.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        downX = e.getX();
                        downY = e.getY();
                        moved = false;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (Math.abs(e.getX() - downX) > touchSlop
                                || Math.abs(e.getY() - downY) > touchSlop) {
                            moved = true;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (!moved) {
                            View child = view.findChildViewUnder(e.getX(), e.getY());
                            if (child != null) {
                                int pos = view.getChildAdapterPosition(child);
                                callback.test(pos);
                            }
                        }
                        break;
                    default:
                        break;
                }
                return false; // nunca bloquea el scroll del padre
            }
        });
    }

    private void setupViewModels() {
        doctorViewModel = new ViewModelProvider(this).get(DoctorViewModel.class);
        appointmentViewModel = new ViewModelProvider(this).get(AppointmentViewModel.class);
        patientViewModel = new ViewModelProvider(this).get(PatientViewModel.class);

        doctorViewModel.getSlots().observe(this, this::renderSlots);

        patientViewModel.getPatient().observe(this, this::renderPatient);

        appointmentViewModel.getActionState().observe(this, this::renderAction);
        appointmentViewModel.getNavigateBack().observe(this, created -> {
            if (Boolean.TRUE.equals(created)) {
                // Consumimos el evento para que no se vuelva a disparar al rotar la pantalla
                appointmentViewModel.clearNavigateBack();
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, R.string.appointment_confirmed, Toast.LENGTH_LONG).show();
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    /**
     * Recupera la información del paciente (incluida su EDAD) desde la API mock.
     * Si la API no responde, se conserva la edad de la sesión.
     */
    private void loadPatientFromApi() {
        patientViewModel.loadPatient(DEFAULT_PATIENT_ID);
    }

    private void renderPatient(@Nullable Resource<Patient> resource) {
        if (resource == null || resource.status != Resource.Status.SUCCESS || resource.data == null) {
            return;
        }
        Patient patient = resource.data;
        // La edad proviene de los datos generados por IA en la API mock.
        patientAge = patient.getAge();
    }

    private void loadSlotsForSelectedDate(@Nullable String date) {
        selectedDate = date;
        if (date == null) {
            return;
        }
        doctorViewModel.loadSlots(doctor.getId());
    }

    private void renderSlots(@Nullable Resource<List<TimeSlot>> resource) {
        if (resource == null) {
            return;
        }
        switch (resource.status) {
            case LOADING:
                progressBar.setVisibility(View.VISIBLE);
                break;
            case SUCCESS:
                progressBar.setVisibility(View.GONE);
                allSlots = resource.data != null ? resource.data : new ArrayList<>();
                renderFilteredSlots(allSlots);
                break;
            case ERROR:
            default:
                progressBar.setVisibility(View.GONE);
                allSlots = new ArrayList<>();
                renderFilteredSlots(allSlots);
                if (resource.message != null) {
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /** Filtra por la fecha elegida y ACTUALIZA el adaptador sin recrearlo. */
    private void renderFilteredSlots(List<TimeSlot> source) {
        List<TimeSlot> filtered = new ArrayList<>();
        if (source != null) {
            for (TimeSlot slot : source) {
                if (selectedDate == null || slot.getDate().equals(selectedDate)) {
                    filtered.add(slot);
                }
            }
        }
        slotAdapter.setData(filtered);

        // Aviso claro cuando no hay horarios para la fecha (evita parecer 'roto').
        if (tvSlotsEmpty != null) {
            tvSlotsEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        }
        updateConfirmState();
    }

    /** Se invoca cuando el usuario toca un horario disponible del adaptador. */
    private void onSlotSelected(@Nullable TimeSlot slot) {
        updateConfirmState();
        if (slot != null && tvSlotHint != null) {
            tvSlotHint.setText("\u2705 " + slot.getDate() + " \u00b7 " + slot.getTime());
        }
    }

    /** Habilita el botón solo cuando hay un horario seleccionado. */
    private void updateConfirmState() {
        if (btnConfirm == null) {
            return;
        }
        boolean hasSlot = slotAdapter != null && slotAdapter.getSelectedSlot() != null;
        btnConfirm.setEnabled(hasSlot && !isSubmitting);
        if (tvSlotHint != null) {
            if (!hasSlot) {
                tvSlotHint.setText("Selecciona un horario disponible para continuar");
            }
        }
    }

    private void renderAction(@Nullable Resource<Void> resource) {
        if (resource == null) {
            return;
        }
        if (resource.status == Resource.Status.LOADING) {
            progressBar.setVisibility(View.VISIBLE);
            setConfirmEnabled(false);
        } else if (resource.status == Resource.Status.SUCCESS) {
            // La inserción terminó bien: el cierre de pantalla lo maneja navigateBack.
            progressBar.setVisibility(View.GONE);
        } else if (resource.status == Resource.Status.ERROR) {
            progressBar.setVisibility(View.GONE);
            isSubmitting = false;
            updateConfirmState();
            if (resource.message != null) {
                Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
            }
            appointmentViewModel.clearActionState();
        }
    }

    private void setConfirmEnabled(boolean enabled) {
        if (btnConfirm != null) {
            btnConfirm.setEnabled(enabled);
        }
    }

    private void onConfirmClicked() {
        if (isSubmitting) {
            return;
        }

        TimeSlot selectedSlot = slotAdapter.getSelectedSlot();
        if (selectedSlot == null) {
            Toast.makeText(this, R.string.select_time, Toast.LENGTH_SHORT).show();
            return;
        }

        isSubmitting = true;
        setConfirmEnabled(false);

        String reason = etReason.getText() != null ? etReason.getText().toString().trim() : "";

        Appointment appointment = new Appointment();
        appointment.setDoctorId(doctor.getId());
        appointment.setDoctorName(doctor.getFullName());
        appointment.setSpecialtyName(doctor.getSpecialtyName());
        appointment.setPatientName(patientName);
        appointment.setPatientAge(patientAge);
        appointment.setDate(selectedSlot.getDate());
        appointment.setTime(selectedSlot.getTime());
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setReason(reason);
        appointment.setCreatedAt(System.currentTimeMillis());

        appointmentViewModel.createAppointment(appointment);
    }
}
