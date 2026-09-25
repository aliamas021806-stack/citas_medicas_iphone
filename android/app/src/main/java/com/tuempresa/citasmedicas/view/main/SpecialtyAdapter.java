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
import com.tuempresa.citasmedicas.model.Specialty;

/**
 * Adaptador horizontal de especialidades médicas.
 *
 * <p>FIX: además de corregir el layout {@code item_specialty.xml}, aquí se
 * mantiene el estado de selección para que el chip activo se resalte de forma
 * estable (antes el clic parecía no hacer nada porque nunca se propagaba el
 * estado {@code selected}).
 */
public class SpecialtyAdapter extends ListAdapter<Specialty, SpecialtyAdapter.SpecialtyViewHolder> {

    public interface OnSpecialtyClickListener {
        void onSpecialtyClick(Specialty specialty);
    }

    private final OnSpecialtyClickListener listener;
    private String selectedId;

    public SpecialtyAdapter(OnSpecialtyClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Specialty> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Specialty>() {
                @Override
                public boolean areItemsTheSame(@NonNull Specialty a, @NonNull Specialty b) {
                    return a.getId() != null && a.getId().equals(b.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Specialty a, @NonNull Specialty b) {
                    return a.getName() == null ? b.getName() == null
                            : a.getName().equals(b.getName());
                }
            };

    @NonNull
    @Override
    public SpecialtyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_specialty, parent, false);
        return new SpecialtyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SpecialtyViewHolder holder, int position) {
        Specialty item = getItem(position);
        boolean selected = item.getId() != null && item.getId().equals(selectedId);
        holder.bind(item, selected, listener);
    }

    /** Marca la especialidad activa y refresca la lista para recolorear los chips. */
    public void setSelectedId(String specialtyId) {
        this.selectedId = specialtyId;
        notifyDataSetChanged();
    }

    static class SpecialtyViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvIcon, tvName;

        SpecialtyViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setClickable(true);
            itemView.setFocusable(false);
            tvIcon = itemView.findViewById(R.id.tvIcon);
            tvName = itemView.findViewById(R.id.tvSpecialtyName);
        }

        void bind(Specialty specialty, boolean selected, OnSpecialtyClickListener listener) {
            tvIcon.setText(specialty.getIconEmoji());
            tvName.setText(specialty.getName());
            itemView.setSelected(selected);
            // Texto/icono en blanco cuando el chip está activo (el fondo lo pinta
            // bg_chip.xml en su estado selected).
            int color = itemView.getContext().getColor(selected ? R.color.white : R.color.text_primary);
            tvName.setTextColor(color);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSpecialtyClick(specialty);
                }
            });
        }
    }
}
