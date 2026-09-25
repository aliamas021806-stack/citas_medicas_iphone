package com.tuempresa.citasmedicas.view.appointment;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.tuempresa.citasmedicas.R;
import com.tuempresa.citasmedicas.model.TimeSlot;

import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador vertical de horarios (slots) de un doctor.
 *
 * <p>FIX Error 2 y 3. El problema original era doble:
 * <ol>
 *   <li>El layout {@code item_slot.xml} tenía {@code focusable="true"}, por lo
 *       que el primer toque solo "enfocaba" el ítem y no disparaba el clic.</li>
 *   <li>{@code DoctorDetailActivity} creaba un NUEVO adaptador en cada render
 *       del LiveData, con una lambda de escucha VACÍA. Como el botón de
 *       confirmar leía {@code slotAdapter.getSelectedSlot()} del adaptador
 *       recién creado (sin selección), la reserva siempre quedaba bloqueada.</li>
 * </ol>
 *
 * <p>Este adaptador expone {@link #setData(List)} para ACTUALIZAR los datos sin
 * recrearlo, conservando la instancia (y por tanto la selección) que la Activity
 * controla. Solo se puede seleccionar un slot AVAILABLE.
 */
public class SlotAdapter extends RecyclerView.Adapter<SlotAdapter.SlotViewHolder> {

    public interface OnSlotSelectedListener {
        void onSlotSelected(TimeSlot slot);
    }

    private final List<TimeSlot> slots = new ArrayList<>();
    private OnSlotSelectedListener listener;
    private int selectedIndex = -1;

    public SlotAdapter() {
    }

    public SlotAdapter(List<TimeSlot> initial, OnSlotSelectedListener listener) {
        if (initial != null) {
            slots.addAll(initial);
        }
        this.listener = listener;
    }

    public void setListener(OnSlotSelectedListener listener) {
        this.listener = listener;
    }

    /**
     * Reemplaza el contenido conservando la instancia del adaptador.
     * Si el slot previamente seleccionado sigue existiendo y está disponible,
     * se mantiene la selección; en caso contrario se limpia.
     */
    public void setData(List<TimeSlot> newSlots) {
        String previousId = getSelectedSlot() != null ? getSelectedSlot().getId() : null;

        slots.clear();
        if (newSlots != null) {
            slots.addAll(newSlots);
        }

        selectedIndex = -1;
        if (previousId != null) {
            for (int i = 0; i < slots.size(); i++) {
                TimeSlot s = slots.get(i);
                if (previousId.equals(s.getId()) && s.isAvailable()) {
                    selectedIndex = i;
                    break;
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_slot, parent, false);
        return new SlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SlotViewHolder holder, int position) {
        TimeSlot slot = slots.get(position);
        holder.bind(slot, position == selectedIndex, () -> select(holder.getBindingAdapterPosition()));
    }

    /**
     * Selecciona el slot en la posición indicada. Devuelve {@code true} si la
     * selección cambió a un slot DISPONIBLE. Es el único punto de verdad para la
     * selección (lo invocan tanto el clic del ítem como el gesto táctil del
     * RecyclerView en la Activity).
     */
    public boolean select(int position) {
        if (position < 0 || position >= slots.size()) {
            return false;
        }
        TimeSlot clicked = slots.get(position);
        if (!clicked.isAvailable()) {
            return false;
        }
        int previous = selectedIndex;
        selectedIndex = position;
        if (previous >= 0) {
            notifyItemChanged(previous);
        }
        notifyItemChanged(selectedIndex);
        if (listener != null) {
            listener.onSlotSelected(clicked);
        }
        return true;
    }

    @Override
    public int getItemCount() {
        return slots.size();
    }

    /** Devuelve el slot seleccionado o {@code null} si todavía no hay ninguno. */
    public TimeSlot getSelectedSlot() {
        if (selectedIndex < 0 || selectedIndex >= slots.size()) {
            return null;
        }
        return slots.get(selectedIndex);
    }

    static class SlotViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTime;
        private final TextView tvAvailability;

        SlotViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setClickable(true);
            itemView.setFocusable(false);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvAvailability = itemView.findViewById(R.id.tvAvailability);
        }

        interface OnClickCallback {
            void onClick();
        }

        void bind(TimeSlot slot, boolean selected, OnClickCallback callback) {
            tvTime.setText(slot.getTime());

            boolean available = slot.isAvailable();
            itemView.setSelected(selected);
            itemView.setEnabled(available);
            itemView.setAlpha(available ? 1f : 0.5f);

            tvTime.setPaintFlags(available
                    ? tvTime.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG
                    : tvTime.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

            int color = ContextCompat.getColor(itemView.getContext(), R.color.text_primary);
            if (!available) {
                tvAvailability.setText("No disponible");
                tvAvailability.setTextColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.text_secondary));
                color = ContextCompat.getColor(itemView.getContext(), R.color.text_secondary);
            } else if (selected) {
                tvAvailability.setText("Seleccionado");
                tvAvailability.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
                color = ContextCompat.getColor(itemView.getContext(), R.color.white);
            } else {
                tvAvailability.setText("Disponible");
                tvAvailability.setTextColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.status_confirmed));
            }
            tvTime.setTextColor(color);

            // El clic solo se procesa si el slot está disponible.
            itemView.setOnClickListener(v -> {
                if (available && callback != null) {
                    callback.onClick();
                }
            });
        }
    }
}
