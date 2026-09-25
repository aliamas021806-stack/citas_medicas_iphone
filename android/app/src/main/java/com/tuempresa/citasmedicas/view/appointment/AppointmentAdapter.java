package com.tuempresa.citasmedicas.view.appointment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.tuempresa.citasmedicas.R;
import com.tuempresa.citasmedicas.data.local.AppointmentEntity;
import com.tuempresa.citasmedicas.model.AppointmentStatus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Adaptador de "Mis Citas Medicas". Muestra la EDAD del paciente en cada tarjeta
 * y permite cancelar la cita.
 */
public class AppointmentAdapter
        extends ListAdapter<AppointmentEntity, AppointmentAdapter.AppointmentViewHolder> {

    public interface OnAppointmentActionListener {
        void onCancelAppointment(AppointmentEntity appointment);
    }

    private final OnAppointmentActionListener listener;

    private static final String[] MONTHS = {
            "ene", "feb", "mar", "abr", "may", "jun",
            "jul", "ago", "sep", "oct", "nov", "dic"
    };

    public AppointmentAdapter(OnAppointmentActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<AppointmentEntity> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<AppointmentEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull AppointmentEntity a,
                                               @NonNull AppointmentEntity b) {
                    return a.id == b.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull AppointmentEntity a,
                                                  @NonNull AppointmentEntity b) {
                    return a.id == b.id
                            && a.status != null && a.status.equals(b.status)
                            && a.patientAge == b.patientAge
                            && safeEquals(a.date, b.date)
                            && safeEquals(a.time, b.time);
                }
            };

    private static boolean safeEquals(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appointment, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDoctorName, tvSpecialty, tvPatientAge, tvStatus;
        private final TextView tvDate, tvTime, tvReason;
        private final MaterialButton btnCancel;

        AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDoctorName = itemView.findViewById(R.id.tvDoctorName);
            tvSpecialty = itemView.findViewById(R.id.tvSpecialty);
            tvPatientAge = itemView.findViewById(R.id.tvPatientAge);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvReason = itemView.findViewById(R.id.tvReason);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }

        void bind(AppointmentEntity item, OnAppointmentActionListener listener) {
            tvDoctorName.setText(name(item));
            tvSpecialty.setText(safe(item.specialtyName));
            tvPatientAge.setText("\ud83c\udf82 Paciente: " + safe(item.patientName)
                    + " \u00b7 " + item.patientAge + " a\u00f1os");
            tvDate.setText("\ud83d\udcc5 " + formatDate(item.date));
            tvTime.setText("\ud83d\udd52 " + safe(item.time));

            if (item.reason != null && !item.reason.trim().isEmpty()) {
                tvReason.setVisibility(View.VISIBLE);
                tvReason.setText("Motivo: " + item.reason.trim());
            } else {
                tvReason.setVisibility(View.GONE);
            }

            AppointmentStatus status = AppointmentStatus.fromValue(item.status);
            applyStatus(tvStatus, status);

            boolean cancellable = status != AppointmentStatus.CANCELLED;
            btnCancel.setVisibility(cancellable ? View.VISIBLE : View.GONE);
            btnCancel.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCancelAppointment(item);
                }
            });
        }

        private void applyStatus(TextView view, AppointmentStatus status) {
            switch (status) {
                case CANCELLED:
                    view.setText(R.string.status_cancelled);
                    view.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.status_cancelled));
                    break;
                case PENDING:
                    view.setText(R.string.status_pending);
                    view.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.status_pending));
                    break;
                case CONFIRMED:
                default:
                    view.setText(R.string.status_confirmed);
                    view.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.status_confirmed));
                    break;
            }
        }

        private String name(AppointmentEntity item) {
            String doctor = safe(item.doctorName);
            if (doctor.isEmpty()) {
                return "Doctor";
            }
            String first = doctor.trim().split("\\s+")[0];
            boolean female = first.toLowerCase().endsWith("a");
            return (female ? "Dra. " : "Dr. ") + doctor;
        }

        private String safe(String value) {
            return value != null ? value : "";
        }

        private String formatDate(String date) {
            if (date == null || date.isEmpty()) {
                return "";
            }
            try {
                SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date d = in.parse(date);
                if (d == null) {
                    return date;
                }
                SimpleDateFormat out = new SimpleDateFormat("dd", Locale.getDefault());
                SimpleDateFormat month = new SimpleDateFormat("MM", Locale.getDefault());
                int m = Integer.parseInt(month.format(d)) - 1;
                String monthName = (m >= 0 && m < MONTHS.length) ? MONTHS[m] : "";
                return out.format(d) + " " + monthName;
            } catch (ParseException e) {
                return date;
            }
        }
    }
}
