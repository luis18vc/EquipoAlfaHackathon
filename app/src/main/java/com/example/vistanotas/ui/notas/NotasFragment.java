package com.example.vistanotas.ui.notas;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

        SharedPreferences preferences = requireContext().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        String token = preferences.getString("token", null);

        if (token == null) {
            Toast.makeText(requireContext(), "No se encontró token de acceso. Por favor, inicia sesión.", Toast.LENGTH_SHORT).show();
            return view;  // Retorna la vista aunque no haya token
        }

        String idCurso = getArguments() != null ? getArguments().getString("idCurso") : null;
        obtenerNotasDesdeApi(idCurso, token);

        return view;
    }

    private void obtenerNotasDesdeApi(String cursoId, String token) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        if (token == null) {
            Toast.makeText(requireContext(), "No se encontró token de acceso. Por favor, inicia sesión.", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<NotasResponse> call = apiService.obtenerNotas(cursoId, "Bearer " + token);

        call.enqueue(new Callback<NotasResponse>() {
            @Override
            public void onResponse(@NonNull Call<NotasResponse> call, @NonNull Response<NotasResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Nota> notas = response.body().getNotas();
                    notasAdapter = new NotasAdapter(notas);
                    recyclerViewNotas.setAdapter(notasAdapter);
                } else {
                    Toast.makeText(requireContext(), "Error en la respuesta de la API", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<NotasResponse> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("API_ERROR", "onFailure: ", t);
            }
        });
    }
}
