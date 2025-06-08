package com.example.vistanotas.ui.infocursos;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.vistanotas.ApiClient;
import com.example.vistanotas.R;
import com.example.vistanotas.interfaces.ApiService;
import com.example.vistanotas.models.Curso;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InfoCursoFragment extends Fragment {

    private TextView tvCursos;
    private TextView tvPromedio;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_info_curso, container, false);

        tvCursos = view.findViewById(R.id.tvCursos);
        tvPromedio = view.findViewById(R.id.tvPromedio);

        obtenerCursosDesdeApi();

        return view;
    }

    private void obtenerCursosDesdeApi() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        Call<List<Curso>> call = apiService.obtenerCursos();

        call.enqueue(new Callback<List<Curso>>() {
            @Override
            public void onResponse(Call<List<Curso>> call, Response<List<Curso>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Curso> cursos = response.body();
                    mostrarCursos(cursos);
                    double promedio = calcularPromedioPonderado(cursos);
                    tvPromedio.setText("Promedio: " + promedio);
                } else {
                    Toast.makeText(requireContext(), "Error en la respuesta de la API", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Curso>> call, Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("API_ERROR", "onFailure: ", t);
            }
        });
    }

    private void mostrarCursos(List<Curso> cursos) {
        StringBuilder sb = new StringBuilder();
        for (Curso curso : cursos) {
            sb.append("Curso: ").append(curso.getNombre())
                    .append(" - Nota: ").append(curso.getNota())
                    .append(" - %: ").append(curso.getPorcentaje()).append("\n");
        }
        tvCursos.setText(sb.toString());
    }

    private double calcularPromedioPonderado(List<Curso> cursos) {
        double total = 0;
        int sumaPesos = 0;

        for (Curso curso : cursos) {
            total += curso.getNota() * curso.getPorcentaje();
            sumaPesos += curso.getPorcentaje();
        }

        return sumaPesos > 0 ? total / sumaPesos : 0;
    }
}