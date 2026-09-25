package com.tuempresa.citasmedicas.view.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.tuempresa.citasmedicas.R;
import com.tuempresa.citasmedicas.model.Doctor;

/**
 * Adaptador de la lista de doctores.
 *
 * <p>FIX Error 1 (no se podía seleccionar al doctor): además de quitar
 * {@code android:focusable="true"} del layout {@code item_doctor.xml}, aquí se
 * refuerza el clic en código:
 * <ul>
 *   <li>El ítem siempre se marca {@code clickable=true} / {@code focusable=false}.</li>
 *   <li>El listener se invoca de forma defensiva (null-check) y leyendo la
 *       posición actual con {@link RecyclerView.ViewHolder#getBindingAdapterPosition()},
 *       evitando índices obsoletos tras reciclar la vista.</li>
 * </ul>
 */
public class DoctorAdapter extends ListAdapter<Doctor, DoctorAdapter.DoctorViewHolder> {

    public interface OnDoctorClickListener {
        void onDoctorClick(Doctor doctor);
    }

    private final OnDoctorClickListener listener;

    public DoctorAdapter(OnDoctorClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Doctor> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Doctor>() {
                @Override
                public boolean areItemsTheSame(@NonNull Doctor a, @NonNull Doctor b) {
                    return a.getId() != null && a.getId().equals(b.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Doctor a, @NonNull Doctor b) {
                    return eq(a.getFullName(), b.getFullName())
                            && a.getRating() == b.getRating()
                            && a.getAge() == b.getAge();
                }

                private boolean eq(String x, String y) {
                    return x == null ? y == null : x.equals(y);
                }
            };

    @NonNull
    @Override
    public DoctorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_doctor, parent, false);
        return new DoctorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class DoctorViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvAvatar, tvName, tvSpecialty, tvRating, tvAge, tvHospital, tvFee;

        DoctorViewHolder(@NonNull View itemView) {
            super(itemView);
            // Refuerzo de interacción: el propio ítem gestiona el toque y no
            // delega el foco a los hijos (evita "primer clic perdido").
            itemView.setClickable(true);
            itemView.setFocusable(false);
            // setDescendantFocusability solo existe en ViewGroup; se aplica en el XML.

            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvSpecialty = itemView.findViewById(R.id.tvSpecialty);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvAge = itemView.findViewById(R.id.tvAge);
            tvHospital = itemView.findViewById(R.id.tvHospital);
            tvFee = itemView.findViewById(R.id.tvFee);
        }

        void bind(Doctor doctor, OnDoctorClickListener listener) {
            tvAvatar.setText(doctor.getInitials());
            tvName.setText(doctor.getDisplayName());
            tvSpecialty.setText(doctor.getSpecialtyName());
            tvRating.setText("\u2b50 " + doctor.getRating());
            tvAge.setText("\ud83c\udf82 " + doctor.getAge() + " a\u00f1os");
            tvHospital.setText("\ud83c\udfe5 " + safe(doctor.getHospital()));
            tvFee.setText("$" + (int) doctor.getConsultationFee());

            // Listener defensivo: cualquier parte de la tarjeta abre el detalle.
            // Se usa el objeto 'doctor' ya ligado (evita getItem(...) desde una
            // clase estática, que no es accesible).
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDoctorClick(doctor);
                }
            });
        }

        private String safe(String s) {
            return s != null ? s : "\u2014";
        }
    }
}
