package com.example.vistanotas.ui.notas;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vistanotas.ApiClient;
import com.example.vistanotas.R;
import com.example.vistanotas.ui.adapters.NotasAdapter;
import com.example.vistanotas.interfaces.ApiService;
import com.example.vistanotas.models.notas.Nota;
import com.example.vistanotas.models.notas.NotasResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotasFragment extends Fragment {

    private RecyclerView recyclerViewNotas;
    private NotasAdapter notasAdapter;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_info_curso, container, false);

        recyclerViewNotas = view.findViewById(R.id.recyclerNotas);
        recyclerViewNotas.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Botón para volver a cursos
        Button btnVolver = view.findViewById(R.id.btnVolverCursos);
        btnVolver.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // Obtener token desde SharedPreferences
        SharedPreferences preferences = requireContext().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        String token = preferences.getString("token", null);

        if (token == null) {
            Toast.makeText(requireContext(), "No se encontró token de acceso. Por favor, inicia sesión.", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Obtener el id del curso enviado como argumento
        String idCurso = getArguments() != null ? getArguments().getString("idCurso") : null;

        obtenerNotasDesdeApi(idCurso, token);

        return view;
    }

    private void obtenerNotasDesdeApi(String cursoId, String token) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        Call<NotasResponse> call = apiService.obtenerNotas(cursoId, "Bearer " + token);

        final boolean[] responded = {false};

        // Timeout de 3 segundos para cargar datos locales si no responde la API
        new android.os.Handler().postDelayed(() -> {
            if (!responded[0]) {
                Toast.makeText(requireContext(), "La señal es lenta, cargando notas locales...", Toast.LENGTH_SHORT).show();
                cargarNotasLocales(cursoId);
                call.cancel();
            }
        }, 3000);

        call.enqueue(new Callback<NotasResponse>() {
            @Override
            public void onResponse(@NonNull Call<NotasResponse> call, @NonNull Response<NotasResponse> response) {
                responded[0] = true;
                if (response.isSuccessful() && response.body() != null) {
                    List<Nota> notas = response.body().getNotas();
                    mostrarNotas(notas);
                    guardarNotasLocalmente(cursoId, notas);
                } else {
                    Toast.makeText(requireContext(), "Error en la respuesta de la API, cargando notas locales...", Toast.LENGTH_SHORT).show();
                    cargarNotasLocales(cursoId);
                }
            }

            @Override
            public void onFailure(@NonNull Call<NotasResponse> call, @NonNull Throwable t) {
                responded[0] = true;
                Toast.makeText(requireContext(), "Error: " + t.getMessage() + ", cargando notas locales...", Toast.LENGTH_LONG).show();
                Log.e("API_ERROR", "onFailure: ", t);
                cargarNotasLocales(cursoId);
            }
        });
    }

    private void mostrarNotas(List<Nota> notas) {
        notasAdapter = new NotasAdapter(notas);
        recyclerViewNotas.setAdapter(notasAdapter);
    }

    private void guardarNotasLocalmente(String cursoId, List<Nota> notas) {
        Gson gson = new Gson();
        String notasJson = gson.toJson(notas);

        SharedPreferences.Editor editor = requireContext().getSharedPreferences("NotasPrefs", Context.MODE_PRIVATE).edit();
        editor.putString("notas_" + cursoId, notasJson);
        editor.apply();
    }

    private void cargarNotasLocales(String cursoId) {
        SharedPreferences preferences = requireContext().getSharedPreferences("NotasPrefs", Context.MODE_PRIVATE);
        String json = preferences.getString("notas_" + cursoId, null);

        if (json != null) {
            Type listType = new TypeToken<List<Nota>>() {}.getType();
            List<Nota> notas = new Gson().fromJson(json, listType);
            mostrarNotas(notas);
        } else {
            Toast.makeText(requireContext(), "No hay notas guardadas localmente.", Toast.LENGTH_SHORT).show();
        }
    }
}
