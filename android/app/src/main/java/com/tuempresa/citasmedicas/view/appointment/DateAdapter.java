package com.tuempresa.citasmedicas.view.appointment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tuempresa.citasmedicas.R;
import com.tuempresa.citasmedicas.data.mock.MockData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adaptador horizontal de fechas seleccionables (formato interno {@code yyyy-MM-dd}).
 *
 * <p>FIX Error 2: el layout {@code item_date.xml} tenía {@code focusable="true"}
 * (roba el primer toque) y los TextView hijos no propagaban el estado, por lo que
 * la fecha "no parecía" seleccionarse. Aquí, además, se usan índices seguros
 * ({@link RecyclerView.ViewHolder#getBindingAdapterPosition()}) y se expone
 * {@link #setData(List)} para poder refrescar sin perder la instancia.
 */
public class DateAdapter extends RecyclerView.Adapter<DateAdapter.DateViewHolder> {

    public interface OnDateSelectedListener {
        void onDateSelected(String date);
    }

    private final List<String> dates = new ArrayList<>();
    private OnDateSelectedListener listener;
    private int selectedIndex = 0;

    private static final String[] DAY_NAMES = {
            "Dom", "Lun", "Mar", "Mi\u00e9", "Jue", "Vie", "S\u00e1b"
    };

    public DateAdapter(List<String> initial, OnDateSelectedListener listener) {
        if (initial != null) {
            dates.addAll(initial);
        }
        this.listener = listener;
    }

    public void setListener(OnDateSelectedListener listener) {
        this.listener = listener;
    }

    /** Refresca las fechas sin recrear el adaptador. */
    public void setData(List<String> newDates) {
        String previous = getSelectedDate();
        dates.clear();
        if (newDates != null) {
            dates.addAll(newDates);
        }
        selectedIndex = 0;
        if (previous != null) {
            int idx = dates.indexOf(previous);
            if (idx >= 0) {
                selectedIndex = idx;
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_date, parent, false);
        return new DateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        holder.bind(dates.get(position), position == selectedIndex,
                () -> select(holder.getBindingAdapterPosition()));
    }

    /**
     * Selecciona la fecha en la posición indicada. Devuelve {@code true} si cambió.
     * Punto único de selección (lo usan el clic del ítem y el gesto del RecyclerView).
     */
    public boolean select(int position) {
        if (position < 0 || position >= dates.size()) {
            return false;
        }
        int previous = selectedIndex;
        selectedIndex = position;
        if (previous >= 0) {
            notifyItemChanged(previous);
        }
        notifyItemChanged(selectedIndex);
        if (listener != null) {
            listener.onDateSelected(dates.get(selectedIndex));
        }
        return true;
    }

    /** Índice actualmente seleccionado (para diagnóstico/estado). */
    public int getSelectedIndex() {
        return selectedIndex;
    }

    @Override
    public int getItemCount() {
        return dates.size();
    }

    public String getSelectedDate() {
        if (dates.isEmpty() || selectedIndex < 0 || selectedIndex >= dates.size()) {
            return null;
        }
        return dates.get(selectedIndex);
    }

    static class DateViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDayName;
        private final TextView tvDayNumber;

        DateViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setClickable(true);
            itemView.setFocusable(false);
            tvDayName = itemView.findViewById(R.id.tvDayName);
            tvDayNumber = itemView.findViewById(R.id.tvDayNumber);
        }

        interface OnClickCallback {
            void onClick();
        }

        void bind(String date, boolean selected, OnClickCallback callback) {
            tvDayName.setText(dayName(date));
            tvDayNumber.setText(dayNumber(date));
            itemView.setSelected(selected);
            int color = itemView.getContext().getColor(
                    selected ? R.color.white : R.color.text_primary);
            tvDayNumber.setTextColor(color);
            tvDayName.setTextColor(itemView.getContext().getColor(
                    selected ? R.color.white : R.color.text_secondary));
            itemView.setOnClickListener(v -> {
                if (callback != null) {
                    callback.onClick();
                }
            });
        }

        private String dayName(String date) {
            Date d = parse(date);
            if (d == null) {
                return "";
            }
            Calendar c = Calendar.getInstance(Locale.getDefault());
            c.setTime(d);
            return DAY_NAMES[c.get(Calendar.DAY_OF_WEEK) - 1];
        }

        private String dayNumber(String date) {
            Date d = parse(date);
            if (d == null) {
                return "";
            }
            Calendar c = Calendar.getInstance(Locale.getDefault());
            c.setTime(d);
            return String.valueOf(c.get(Calendar.DAY_OF_MONTH));
        }

        private Date parse(String date) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(
                        MockData.DATE_FORMAT.toPattern(), Locale.getDefault());
                return sdf.parse(date);
            } catch (ParseException e) {
                return null;
            }
        }
    }
}
