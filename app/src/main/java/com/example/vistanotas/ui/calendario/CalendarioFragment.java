package com.example.vistanotas.ui.calendario;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.vistanotas.databinding.FragmentCalendarioBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Fragment que muestra un selector de fecha y los eventos correspondientes a la fecha seleccionada.
 */
public class CalendarioFragment extends Fragment {

    private FragmentCalendarioBinding binding;
    private int selectedYear, selectedMonth, selectedDay;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Opcional: mantener ViewModel si tienes lógica adicional
        new ViewModelProvider(this).get(CalendarioViewModel.class);

        binding = FragmentCalendarioBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Acceder a las vistas a través del binding
        TextView selectedDateText = binding.selectedDateText;
        Button expandCalendarButton = binding.expandCalendarButton;
        LinearLayout eventContainer = binding.eventContainer;

        // Fecha inicial: hoy
        Calendar calendar = Calendar.getInstance();
        selectedYear = calendar.get(Calendar.YEAR);
        selectedMonth = calendar.get(Calendar.MONTH);
        selectedDay = calendar.get(Calendar.DAY_OF_MONTH);

        updateSelectedDateText(selectedDateText);
        showEventosForSelectedDate(eventContainer, selectedDateText.getContext());

        expandCalendarButton.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        selectedYear = year;
                        selectedMonth = month;
                        selectedDay = dayOfMonth;
                        updateSelectedDateText(selectedDateText);
                        showEventosForSelectedDate(eventContainer, selectedDateText.getContext());
                    },
                    selectedYear, selectedMonth, selectedDay
            );
            datePickerDialog.show();
        });

        return root;
    }

    /**
     * Actualiza el TextView con la fecha seleccionada.
     */
    private void updateSelectedDateText(TextView selectedDateText) {
        selectedDateText.setText("Fecha seleccionada: " + selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear);
    }

    /**
     * Muestra los eventos correspondientes a la fecha seleccionada.
     */
    private void showEventosForSelectedDate(LinearLayout eventContainer, Context context) {
        eventContainer.removeAllViews(); // limpiar

        List<String> eventos = getEventosParaFecha(selectedYear, selectedMonth, selectedDay);

        if (eventos.isEmpty()) {
            TextView noEventosView = new TextView(context);
            noEventosView.setText("No hay eventos para esta fecha.");
            noEventosView.setPadding(16, 16, 16, 16);
            noEventosView.setTextSize(16);
            eventContainer.addView(noEventosView);
        } else {
            for (String evento : eventos) {
                TextView eventoView = new TextView(context);
                eventoView.setText(evento);
                eventoView.setPadding(30, 30, 30, 30);
                eventoView.setBackgroundColor(0xFFE0E0E0);
                eventoView.setTextSize(18);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 0, 0, 16);
                eventoView.setLayoutParams(params);
                eventContainer.addView(eventoView);
            }
        }
    }

    /**
     * Devuelve una lista de eventos de ejemplo para la fecha dada.
     */
    private List<String> getEventosParaFecha(int year, int month, int dayOfMonth) {
        List<String> eventos = new ArrayList<>();

        // Ejemplos predeterminados:
        if (year == 2025 && month == Calendar.JUNE && dayOfMonth == 7) {
            eventos.add("Curso: Programación en Java - 09:00 - 10:30");
            eventos.add("Curso: Diseño de Software - 11:00 - 12:30");
        } else if (year == 2025 && month == Calendar.JUNE && dayOfMonth == 8) {
            eventos.add("Curso: Redes de Computadoras - 08:00 - 10:00");
            eventos.add("Curso: Seguridad Informática - 10:30 - 12:00");
        } else if (year == 2017 && month == Calendar.MARCH && dayOfMonth == 16) {
            eventos.add("Curso: Calidad de Servicio de TI - 10:00 - 11:30");
            eventos.add("Curso: Inteligencia de Negocios - 12:00 - 13:30");
        }

        return eventos;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
