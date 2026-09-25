package com.tuempresa.citasmedicas.view.appointment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.tuempresa.citasmedicas.R;
import com.tuempresa.citasmedicas.data.local.AppointmentEntity;
import com.tuempresa.citasmedicas.model.AppointmentStatus;
import com.tuempresa.citasmedicas.util.Resource;
import com.tuempresa.citasmedicas.viewmodel.AppointmentViewModel;

import java.util.List;

/**
 * Pantalla "Mis Citas Medicas". Observa Room (LiveData) y permite cancelar citas.
 */
public class CitasFragment extends Fragment
        implements AppointmentAdapter.OnAppointmentActionListener {

    private AppointmentViewModel viewModel;
    private AppointmentAdapter adapter;
    private RecyclerView rvAppointments;
    private SwipeRefreshLayout swipeRefresh;
    private View emptyState;
    private TextView tvSummary;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_citas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvAppointments = view.findViewById(R.id.rvAppointments);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        emptyState = view.findViewById(R.id.emptyState);
        tvSummary = view.findViewById(R.id.tvSummary);

        adapter = new AppointmentAdapter(this);
        rvAppointments.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvAppointments.setAdapter(adapter);

        if (swipeRefresh != null) {
            swipeRefresh.setColorSchemeResources(R.color.primary);
            swipeRefresh.setOnRefreshListener(() -> {
                swipeRefresh.setRefreshing(false);
                // Re-sincroniza con Firestore (1 lectura acotada) y re-observa Room.
                if (viewModel != null) {
                    viewModel.refresh();
                }
                observeAppointments();
            });
        }

        viewModel = new ViewModelProvider(this).get(AppointmentViewModel.class);
        observeAppointments();
        observeActions();
    }

    private void observeAppointments() {
        viewModel.getAppointments().observe(getViewLifecycleOwner(), this::render);
    }

    private void render(@Nullable List<AppointmentEntity> appointments) {
        if (appointments == null || appointments.isEmpty()) {
            adapter.submitList(null);
            if (emptyState != null) {
                emptyState.setVisibility(View.VISIBLE);
            }
            if (rvAppointments != null) {
                rvAppointments.setVisibility(View.GONE);
            }
            if (tvSummary != null) {
                tvSummary.setText("Consulta y gestiona tus citas");
            }
            return;
        }

        if (emptyState != null) {
            emptyState.setVisibility(View.GONE);
        }
        if (rvAppointments != null) {
            rvAppointments.setVisibility(View.VISIBLE);
        }

        adapter.submitList(appointments);

        int active = 0;
        for (AppointmentEntity e : appointments) {
            if (AppointmentStatus.fromValue(e.status) != AppointmentStatus.CANCELLED) {
                active++;
            }
        }
        if (tvSummary != null) {
            tvSummary.setText(active + " cita(s) activa(s) de " + appointments.size() + " en total");
        }
    }

    private void observeActions() {
        viewModel.getActionState().observe(getViewLifecycleOwner(), this::renderAction);
    }

    private void renderAction(@Nullable Resource<Void> resource) {
        if (resource == null) {
            return;
        }
        if (resource.status == Resource.Status.SUCCESS) {
            viewModel.clearActionState();
        } else if (resource.status == Resource.Status.ERROR && resource.message != null) {
            showToast(resource.message);
            viewModel.clearActionState();
        }
    }

    @Override
    public void onCancelAppointment(AppointmentEntity appointment) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.cancel_dialog_title)
                .setMessage(R.string.cancel_dialog_message)
                .setNegativeButton(R.string.dismiss, null)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    viewModel.cancelAppointment(appointment.id);
                    showToast(getString(R.string.appointment_cancelled));
                })
                .show();
    }

    private void showToast(String message) {
        if (getContext() != null) {
            android.widget.Toast.makeText(getContext(), message, android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}
