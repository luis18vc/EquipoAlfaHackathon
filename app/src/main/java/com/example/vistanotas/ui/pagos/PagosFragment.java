package com.example.vistanotas.ui.pagos;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.example.vistanotas.databinding.FragmentPagosBinding;
import com.example.vistanotas.interfaces.ApiService;
import com.example.vistanotas.models.pagos.Pago;
import com.example.vistanotas.models.pagos.PagosResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PagosFragment extends Fragment {

    private FragmentPagosBinding binding;

    private static final String PREFS_NAME = "PagosPrefs";
    private static final String KEY_PAGOS_JSON = "pagos_json";

    private Handler timeoutHandler = new Handler(Looper.getMainLooper());
    private boolean responseReceived = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        PagosViewModel pagosViewModel = new ViewModelProvider(this).get(PagosViewModel.class);
        binding = FragmentPagosBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        obtenerPagosDesdeAPI(binding.layoutContenedorPagos);

        return root;
    }

    private void obtenerPagosDesdeAPI(LinearLayout eventContainer) {
        eventContainer.removeAllViews();
        responseReceived = false;

        SharedPreferences preferences = requireContext().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        String token = preferences.getString("token", null);

        if (token == null) {
            Toast.makeText(requireContext(), "No hay token de acceso. Por favor inicia sesión.", Toast.LENGTH_SHORT).show();
            return;
        }

        token = "Bearer " + token;

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        Call<List<Pago>> call = apiService.obtenerPagos(token);

        // Timeout: si no responde en 3 segundos, cargamos pagos locales
        timeoutHandler.postDelayed(() -> {
            if (!responseReceived) {
                Toast.makeText(requireContext(), "La respuesta tarda mucho, cargando pagos locales", Toast.LENGTH_SHORT).show();
                mostrarPagosLocales(eventContainer);
                call.cancel(); // Cancelamos la llamada para no seguir esperando
            }
        }, 3000);

        call.enqueue(new Callback<List<Pago>>() {
            @Override
            public void onResponse(Call<List<Pago>> call, Response<List<Pago>> response) {
                responseReceived = true;
                timeoutHandler.removeCallbacksAndMessages(null); // Cancelamos timeout

                if (response.isSuccessful() && response.body() != null) {
                    List<Pago> pagos = response.body();

                    if (pagos == null || pagos.isEmpty()) {
                        agregarTexto(eventContainer, "No hay datos de pago disponibles");
                    } else {
                        // Guardar pagos localmente
                        guardarPagosLocalmente(pagos);

                        mostrarPagos(eventContainer, pagos);
                    }
                } else {
                    Toast.makeText(requireContext(), "Error al cargar los pagos", Toast.LENGTH_SHORT).show();
                    mostrarPagosLocales(eventContainer);
                }
            }

            @Override
            public void onFailure(Call<List<Pago>> call, Throwable t) {
                responseReceived = true;
                timeoutHandler.removeCallbacksAndMessages(null);

                Toast.makeText(requireContext(), "Fallo en la conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("PagosAPI", "Error: ", t);
                mostrarPagosLocales(eventContainer);
            }
        });
    }

    private void mostrarPagos(LinearLayout container, List<Pago> pagos) {
        container.removeAllViews();

        if (pagos == null || pagos.isEmpty()) {
            agregarTexto(container, "No hay datos de pago disponibles");
            return;
        }

        for (Pago p : pagos) {
            Button btnPago = new Button(requireContext());

            String texto = p.getDescripcion() + " - " + p.getMonto() + " - Vence: " + p.getVencimiento() + " - " + p.getEstado();
            btnPago.setText(texto);
            btnPago.setAllCaps(false);
            btnPago.setTextColor(Color.WHITE);

            switch (p.getEstado().toUpperCase()) {
                case "PAGADO":
                    btnPago.setBackgroundColor(Color.parseColor("#4CAF50")); // Verde
                    break;
                case "PENDIENTE DE PAGO":
                    btnPago.setBackgroundColor(Color.parseColor("#C8102E")); // Rojo UTP
                    break;
                default:
                    btnPago.setBackgroundColor(Color.parseColor("#003865")); // Azul UTP
                    break;
            }

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 24, 0, 0);
            btnPago.setLayoutParams(params);

            btnPago.setOnClickListener(v -> Toast.makeText(requireContext(),
                    "Pago: " + p.getDescripcion() + " - Estado: " + p.getEstado(),
                    Toast.LENGTH_SHORT).show());

            container.addView(btnPago);
        }
    }

    private void guardarPagosLocalmente(List<Pago> pagos) {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Gson gson = new Gson();
        String pagosJson = gson.toJson(pagos);

        editor.putString(KEY_PAGOS_JSON, pagosJson);
        editor.apply();
    }

    private void mostrarPagosLocales(LinearLayout container) {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String pagosJson = prefs.getString(KEY_PAGOS_JSON, null);

        if (pagosJson == null) {
            agregarTexto(container, "No hay datos locales disponibles");
            return;
        }

        Gson gson = new Gson();
        List<Pago> pagos = gson.fromJson(pagosJson, new TypeToken<List<Pago>>(){}.getType());

        mostrarPagos(container, pagos);
    }

    private void agregarTexto(LinearLayout container, String texto) {
        TextView textView = new TextView(requireContext());
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
