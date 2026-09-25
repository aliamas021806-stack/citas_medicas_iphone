package com.tuempresa.citasmedicas.view.main;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.textfield.TextInputEditText;
import com.tuempresa.citasmedicas.R;
import com.tuempresa.citasmedicas.model.Doctor;
import com.tuempresa.citasmedicas.model.Specialty;
import com.tuempresa.citasmedicas.model.User;
import com.tuempresa.citasmedicas.util.Resource;
import com.tuempresa.citasmedicas.util.SessionManager;
import com.tuempresa.citasmedicas.view.appointment.DoctorDetailActivity;
import com.tuempresa.citasmedicas.viewmodel.DoctorViewModel;

import java.util.List;

/**
 * Fragmento de inicio: especialidades + doctores con buscador.
 * Muestra en la cabecera el saludo del paciente con su EDAD.
 */
public class DoctorsFragment extends Fragment {

    private DoctorViewModel viewModel;
    private SpecialtyAdapter specialtyAdapter;
    private DoctorAdapter doctorAdapter;
    private ProgressBar progressBar;
    private TextView tvEmpty, tvGreeting, tvPatientHeaderAge;
    private SwipeRefreshLayout swipeRefresh;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_doctors, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DoctorViewModel.class);

        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        tvGreeting = view.findViewById(R.id.tvGreeting);
        tvPatientHeaderAge = view.findViewById(R.id.tvPatientHeaderAge);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);

        User user = SessionManager.getInstance(requireContext()).getUser();
        if (user != null) {
            tvGreeting.setText("Hola, " + user.getFullName() + " 👋");
            String ageText = user.getAge() > 0
                    ? "🎂 " + user.getAge() + " años · Encuentra a tu especialista"
                    : "Encuentra a tu especialista";
            tvPatientHeaderAge.setText(ageText);
        }

        RecyclerView rvSpecialties = view.findViewById(R.id.rvSpecialties);
        rvSpecialties.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false));
        specialtyAdapter = new SpecialtyAdapter(this::onSpecialtySelected);
        rvSpecialties.setAdapter(specialtyAdapter);

        RecyclerView rvDoctors = view.findViewById(R.id.rvDoctors);
        rvDoctors.setLayoutManager(new LinearLayoutManager(getContext()));
        doctorAdapter = new DoctorAdapter(this::onDoctorSelected);
        rvDoctors.setAdapter(doctorAdapter);

        TextInputEditText etSearch = view.findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.filterDoctors(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        swipeRefresh.setColorSchemeResources(R.color.primary);
        swipeRefresh.setOnRefreshListener(() -> {
            viewModel.loadHome();
            swipeRefresh.setRefreshing(false);
        });

        observeViewModel();
        viewModel.loadHome();
    }

    private void observeViewModel() {
        viewModel.getSpecialties().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                specialtyAdapter.submitList(resource.data);
            }
        });

        viewModel.getDoctors().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case LOADING:
                    progressBar.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    progressBar.setVisibility(View.GONE);
                    List<Doctor> doctors = resource.data;
                    doctorAdapter.submitList(doctors);
                    tvEmpty.setVisibility(doctors == null || doctors.isEmpty()
                            ? View.VISIBLE : View.GONE);
                    break;
                case ERROR:
                    progressBar.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText(resource.message);
                    break;
            }
        });
    }

    private void onSpecialtySelected(Specialty specialty) {
        // Resalta el chip activo y filtra los doctores por la especialidad elegida.
        specialtyAdapter.setSelectedId(specialty.getId());
        viewModel.loadDoctorsBySpecialty(specialty.getId());
    }

    private void onDoctorSelected(Doctor doctor) {
        Intent intent = new Intent(requireContext(), DoctorDetailActivity.class);
        intent.putExtra(DoctorDetailActivity.EXTRA_DOCTOR, doctor);
        startActivity(intent);
    }
}
