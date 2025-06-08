package com.example.vistanotas.ui.pagos;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import com.example.vistanotas.databinding.FragmentPagosBinding;
import com.example.vistanotas.interfaces.ApiService;
import com.example.vistanotas.models.pagos.Pago;
import com.example.vistanotas.models.pagos.PagosResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PagosFragment extends Fragment {

    private FragmentPagosBinding binding;

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

        SharedPreferences preferences = requireContext().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        String token = preferences.getString("token", null);

        if (token == null) {
            Toast.makeText(requireContext(), "No hay token de acceso. Por favor inicia sesión.", Toast.LENGTH_SHORT).show();
            return;
        }

        token = "Bearer " + token;

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        Call<PagosResponse> call = apiService.obtenerPagos(token);

        call.enqueue(new Callback<PagosResponse>() {
            @Override
            public void onResponse(Call<PagosResponse> call, Response<PagosResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Pago> pagos = response.body().getPagos();

                    if (pagos == null || pagos.isEmpty()) {
                        agregarTexto(eventContainer, "No hay datos de pago disponibles");
                    } else {
                        for (Pago p : pagos) {
                            Button btnPago = new Button(requireContext());

                            String texto = p.getDescripcion() + " - S/ " + p.getMonto() + " - Vence: " + p.getVencimiento() + " - " + p.getEstado();
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

                            eventContainer.addView(btnPago);
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Error al cargar los pagos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PagosResponse> call, Throwable t) {
                Toast.makeText(requireContext(), "Fallo en la conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("PagosAPI", "Error: ", t);
            }
        });
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
