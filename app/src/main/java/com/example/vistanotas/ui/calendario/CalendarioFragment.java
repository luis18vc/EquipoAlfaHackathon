package com.example.vistanotas.ui.calendario;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.vistanotas.ApiClient;
import com.example.vistanotas.databinding.FragmentCalendarioBinding;
import com.example.vistanotas.interfaces.ApiService;
import com.example.vistanotas.models.calendario.CalendarioRequest;
import com.example.vistanotas.models.calendario.CalendarioResponse;
import com.example.vistanotas.models.calendario.Clase;
import com.google.gson.Gson;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CalendarioFragment extends Fragment {

    private FragmentCalendarioBinding binding;
    private int selectedYear, selectedMonth, selectedDay;

    private static final String PREFS_NAME = "CalendarioPrefs";
    private static final String KEY_CALENDARIO_HOY = "calendario_hoy";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        new ViewModelProvider(this).get(CalendarioViewModel.class);

        binding = FragmentCalendarioBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        TextView selectedDateText = binding.selectedDateText;
        Button expandCalendarButton = binding.expandCalendarButton;
        LinearLayout eventContainer = binding.eventContainer;

        // Obtener la fecha actual según zona horaria Lima/Perú
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("America/Lima"));
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
        SharedPreferences preferences = context.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        String token = preferences.getString("token", null);

        if (token == null) {
            Toast.makeText(context, "No se encontró token de acceso. Por favor, inicia sesión.", Toast.LENGTH_SHORT).show();
            return;
        }

        String bearerToken = "Bearer " + token;
        String fechaFormateada = selectedYear + "-" +
                String.format("%02d", selectedMonth + 1) + "-" +
                String.format("%02d", selectedDay);

        Call<CalendarioResponse> call = apiService.obtenerCalendario(bearerToken, new CalendarioRequest(fechaFormateada));

        call.enqueue(new Callback<CalendarioResponse>() {
            @Override
            public void onResponse(Call<CalendarioResponse> call, Response<CalendarioResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CalendarioResponse calendario = response.body();

                    // Guardar localmente solo si la fecha consultada es la fecha actual
                    if (esFechaHoy(fechaFormateada)) {
                        guardarCalendarioLocal(context, calendario);
                    }

                    mostrarClases(eventContainer, context, calendario.getClases());

                } else {
                    Toast.makeText(context, "Error al cargar calendario", Toast.LENGTH_SHORT).show();
                    cargarCalendarioLocalSiExiste(eventContainer, context);
                }
            }

            @Override
            public void onFailure(Call<CalendarioResponse> call, Throwable t) {
                Toast.makeText(context, "Fallo en la conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("CalendarioAPI", "Error: ", t);
                cargarCalendarioLocalSiExiste(eventContainer, context);
            }
        });
    }

    private void mostrarClases(LinearLayout eventContainer, Context context, List<Clase> clases) {
        eventContainer.removeAllViews(); // Limpia contenido anterior

        if (clases == null || clases.isEmpty()) {
            agregarTextoSimple(eventContainer, context, "No hay clases para esta fecha.");
        } else {
            for (Clase clase : clases) {
                CardView cardView = new CardView(context);
                LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                cardParams.setMargins(24, 16, 24, 0); // Margen externo
                cardView.setLayoutParams(cardParams);
                cardView.setRadius(20f);
                cardView.setCardElevation(8f);
                cardView.setUseCompatPadding(true);

                LinearLayout layoutInterno = new LinearLayout(context);
                layoutInterno.setOrientation(LinearLayout.VERTICAL);
                layoutInterno.setPadding(32, 24, 32, 24); // Padding interno

                TextView curso = crearTextoEstilizado(context, "📘  " + clase.getCurso(), 18, Typeface.BOLD);
                TextView profesor = crearTextoEstilizado(context, "👨‍🏫  Profesor: " + clase.getProfesor(), 16, Typeface.NORMAL);
                TextView salon = crearTextoEstilizado(context, "🏫  Salón: " + clase.getSalon(), 16, Typeface.NORMAL);
                TextView horario = crearTextoEstilizado(context, "🕒  Horario: " + clase.getInicio() + " - " + clase.getFin(), 16, Typeface.ITALIC);

                layoutInterno.addView(curso);
                layoutInterno.addView(profesor);
                layoutInterno.addView(salon);
                layoutInterno.addView(horario);

                cardView.addView(layoutInterno);
                eventContainer.addView(cardView);
            }
        }
    }
    private void agregarTextoSimple(LinearLayout container, Context context, String mensaje) {
        TextView tv = new TextView(context);
        tv.setText(mensaje);
        tv.setTextSize(16f);
        tv.setTextColor(Color.GRAY);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(30, 50, 30, 50);
        container.addView(tv);
    }

    private TextView crearTextoEstilizado(Context context, String texto, int textSize, int estilo) {
        TextView tv = new TextView(context);
        tv.setText(texto);
        tv.setTextSize(textSize);
        tv.setTypeface(null, estilo);
        tv.setTextColor(Color.parseColor("#333333")); // gris oscuro
        tv.setLineSpacing(4f, 1.2f);// interlineado
        tv.setPadding(0, 8, 0, 8);
        return tv;
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

    private void guardarCalendarioLocal(Context context, CalendarioResponse calendario) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Gson gson = new Gson();
        String jsonCalendario = gson.toJson(calendario);

        editor.putString(KEY_CALENDARIO_HOY, jsonCalendario);
        editor.apply();
    }

    private void cargarCalendarioLocalSiExiste(LinearLayout eventContainer, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String jsonGuardado = prefs.getString(KEY_CALENDARIO_HOY, null);

        if (jsonGuardado != null) {
            Gson gson = new Gson();
            CalendarioResponse calendarioLocal = gson.fromJson(jsonGuardado, CalendarioResponse.class);
            Toast.makeText(context, "Mostrando datos guardados localmente", Toast.LENGTH_SHORT).show();
            mostrarClases(eventContainer, context, calendarioLocal.getClases());
        } else {
            agregarTexto(eventContainer, context, "No hay datos disponibles sin conexión.");
        }
    }

    private boolean esFechaHoy(String fecha) {
        // fecha en formato yyyy-MM-dd
        Calendar hoy = Calendar.getInstance(TimeZone.getTimeZone("America/Lima"));
        String hoyStr = hoy.get(Calendar.YEAR) + "-" +
                String.format("%02d", hoy.get(Calendar.MONTH) + 1) + "-" +
                String.format("%02d", hoy.get(Calendar.DAY_OF_MONTH));
        return fecha.equals(hoyStr);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
