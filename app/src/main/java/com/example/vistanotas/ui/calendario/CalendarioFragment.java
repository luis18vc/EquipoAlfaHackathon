package com.example.vistanotas.ui.calendario;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.vistanotas.ApiClient;
import com.example.vistanotas.databinding.FragmentCalendarioBinding;
import com.example.vistanotas.interfaces.ApiService;
import com.example.vistanotas.models.calendario.CalendarioRequest;
import com.example.vistanotas.models.calendario.CalendarioResponse;
import com.example.vistanotas.models.calendario.Clase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CalendarioFragment extends Fragment {

    private FragmentCalendarioBinding binding;
    private int selectedYear, selectedMonth, selectedDay;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        new ViewModelProvider(this).get(CalendarioViewModel.class);

        binding = FragmentCalendarioBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        TextView selectedDateText = binding.selectedDateText;
        Button expandCalendarButton = binding.expandCalendarButton;
        LinearLayout eventContainer = binding.eventContainer;

        Calendar calendar = Calendar.getInstance();
        selectedYear = calendar.get(Calendar.YEAR);
        selectedMonth = calendar.get(Calendar.MONTH);
        selectedDay = calendar.get(Calendar.DAY_OF_MONTH);

        updateSelectedDateText(selectedDateText);
        obtenerClasesDesdeApi(eventContainer, selectedDateText.getContext());

        expandCalendarButton.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        selectedYear = year;
                        selectedMonth = month;
                        selectedDay = dayOfMonth;
                        updateSelectedDateText(selectedDateText);
                        obtenerClasesDesdeApi(eventContainer, selectedDateText.getContext());
                    },
                    selectedYear, selectedMonth, selectedDay
            );
            datePickerDialog.show();
        });

        return root;
    }

    private void updateSelectedDateText(TextView selectedDateText) {
        selectedDateText.setText("Fecha seleccionada: " + selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear);
    }

    private void obtenerClasesDesdeApi(LinearLayout eventContainer, Context context) {
        eventContainer.removeAllViews();

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        // Obtener token guardado en SharedPreferences
        SharedPreferences preferences = context.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        String token = preferences.getString("token", null);

        if (token == null) {
            Toast.makeText(context, "No se encontró token de acceso. Por favor, inicia sesión.", Toast.LENGTH_SHORT).show();
            return; // No seguir si no hay token
        }

        // Agregar el prefijo "Bearer " que espera la API
        String bearerToken = "Bearer " + token;
        String fechaFormateada = selectedYear + "-" +
                String.format("%02d", selectedMonth + 1) + "-" +
                String.format("%02d", selectedDay);

        // Llamamos al método POST que recibe token y fecha en body
        Call<CalendarioResponse> call = apiService.obtenerCalendario(token, new CalendarioRequest(fechaFormateada));

        call.enqueue(new Callback<CalendarioResponse>() {
            @Override
            public void onResponse(Call<CalendarioResponse> call, Response<CalendarioResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CalendarioResponse calendario = response.body();

                    List<Clase> clases = calendario.getClases();

                    if (clases == null || clases.isEmpty()) {
                        agregarTexto(eventContainer, context, "No hay clases para esta fecha.");
                    } else {
                        for (Clase clase : clases) {
                            String textoClase = "Curso: " + clase.getCurso() + "\n"
                                    + "Profesor: " + clase.getProfesor() + "\n"
                                    + "Salón: " + clase.getSalon() + "\n"
                                    + "Horario: " + clase.getInicio() + " - " + clase.getFin();
                            agregarTexto(eventContainer, context, textoClase);
                        }
                    }
                } else {
                    Toast.makeText(context, "Error al cargar calendario", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CalendarioResponse> call, Throwable t) {
                Toast.makeText(context, "Fallo en la conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("CalendarioAPI", "Error: ", t);
            }
        });
    }
    private void agregarTexto(LinearLayout container, Context context, String texto) {
        TextView textView = new TextView(context);
        textView.setText(texto);
        textView.setPadding(30, 30, 30, 30);
        textView.setTextSize(16);
        textView.setBackgroundColor(0xFFE0E0E0);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 16);
        textView.setLayoutParams(params);
        container.addView(textView);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
