package com.example.vistanotas.ui.cursos;

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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.vistanotas.ApiClient;
import com.example.vistanotas.R;
import com.example.vistanotas.databinding.FragmentCursosBinding;
import com.example.vistanotas.interfaces.ApiService;
import com.example.vistanotas.models.cursos.Curso;
import com.example.vistanotas.models.cursos.CursosResponse;
import com.example.vistanotas.models.notas.Nota;
import com.example.vistanotas.models.notas.NotasResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CursosFragment extends Fragment {

    private FragmentCursosBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentCursosBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        LinearLayout layoutCursos = binding.layoutCursos;
        obtenerCursosDesdeApi(layoutCursos, requireContext());

        return root;
    }

    private void obtenerCursosDesdeApi(LinearLayout container, Context context) {
        container.removeAllViews();

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        SharedPreferences preferences = context.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        String token = preferences.getString("token", null);

        if (token == null) {
            Toast.makeText(context, "No se encontró token. Inicia sesión.", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<CursosResponse> call = apiService.obtenerCursos("Bearer " + token);

        final boolean[] responded = {false};  // Para controlar si ya hubo respuesta

        // Manejador para el timeout
        new android.os.Handler().postDelayed(() -> {
            if (!responded[0]) {
                Toast.makeText(context, "La señal es lenta, cargando datos guardados...", Toast.LENGTH_SHORT).show();
                mostrarCursosLocalesSiExisten(container, context);
                // Cancelar la llamada API para liberar recursos
                call.cancel();
            }
        }, 3000);
        call.enqueue(new Callback<CursosResponse>() {
            @Override
            public void onResponse(Call<CursosResponse> call, Response<CursosResponse> response) {
                responded[0] = true;
                if (response.isSuccessful() && response.body() != null) {
                    List<Curso> cursos = response.body().getCursos();

                    if (cursos == null || cursos.isEmpty()) {
                        agregarTexto(container, context, "No hay cursos matriculados.");
                    } else {
                        guardarCursosLocalmente(context, cursos);
                        mostrarCursos(container, context, cursos);

                        // Por cada curso, obtener y guardar notas localmente
                        for (Curso curso : cursos) {
                            obtenerNotasApiYGuardarLocal(context, curso.getIdCurso(), token);
                        }
                    }
                } else {
                    Toast.makeText(context, "Error al cargar cursos", Toast.LENGTH_SHORT).show();
                    mostrarCursosLocalesSiExisten(container, context);
                }
            }

            @Override
            public void onFailure(Call<CursosResponse> call, Throwable t) {
                responded[0] = true;
                Toast.makeText(context, "Fallo en conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("CursosAPI", "Error: ", t);
                mostrarCursosLocalesSiExisten(container, context);
            }
        });
    }

    private void obtenerNotasApiYGuardarLocal(Context context, String cursoId, String token) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        Call<NotasResponse> call = apiService.obtenerNotas(cursoId, "Bearer " + token);
        call.enqueue(new Callback<NotasResponse>() {
            @Override
            public void onResponse(Call<NotasResponse> call, Response<NotasResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Nota> notas = response.body().getNotas();
                    guardarNotasLocalmente(context, cursoId, notas);
                }
            }

            @Override
            public void onFailure(Call<NotasResponse> call, Throwable t) {
                Log.e("NotasAPI", "No se pudo cargar notas para curso " + cursoId + ": " + t.getMessage());
            }
        });
    }

    private void guardarNotasLocalmente(Context context, String cursoId, List<Nota> notas) {
        Gson gson = new Gson();
        String notasJson = gson.toJson(notas);

        SharedPreferences.Editor editor = context.getSharedPreferences("NotasPrefs", Context.MODE_PRIVATE).edit();
        editor.putString("notas_" + cursoId, notasJson);
        editor.apply();
    }

    public List<Nota> obtenerNotasLocales(Context context, String cursoId) {
        SharedPreferences preferences = context.getSharedPreferences("NotasPrefs", Context.MODE_PRIVATE);
        String json = preferences.getString("notas_" + cursoId, null);

        if (json != null) {
            Type listType = new TypeToken<List<Nota>>() {}.getType();
            return new Gson().fromJson(json, listType);
        }
        return null;
    }

    private void mostrarCursos(LinearLayout container, Context context, List<Curso> cursos) {
        for (Curso c : cursos) {
            Button btnCurso = new Button(context);
            btnCurso.setText(c.getTitulo());
            btnCurso.setAllCaps(false);
            btnCurso.setTextColor(0xFFFFFFFF);
            btnCurso.setBackgroundColor(0xFF2196F3);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 24, 0, 0);
            btnCurso.setLayoutParams(params);

            btnCurso.setOnClickListener(v -> {
                Toast.makeText(context, "Curso: " + c.getTitulo(), Toast.LENGTH_SHORT).show();

                Bundle bundle = new Bundle();
                bundle.putString("idCurso", c.getIdCurso());

                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
                navController.navigate(R.id.action_cursosFragment_to_notasFragment, bundle);
            });

            container.addView(btnCurso);
        }
    }

    private void guardarCursosLocalmente(Context context, List<Curso> cursos) {
        Gson gson = new Gson();
        String cursosJson = gson.toJson(cursos);

        SharedPreferences.Editor editor = context.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE).edit();
        editor.putString("json_cursos", cursosJson);
        editor.apply();
    }

    private List<Curso> obtenerCursosLocales(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        String json = preferences.getString("json_cursos", null);

        if (json != null) {
            Type listType = new TypeToken<List<Curso>>() {}.getType();
            return new Gson().fromJson(json, listType);
        }
        return null;
    }

    private void mostrarCursosLocalesSiExisten(LinearLayout container, Context context) {
        List<Curso> cursosGuardados = obtenerCursosLocales(context);
        if (cursosGuardados != null && !cursosGuardados.isEmpty()) {
            agregarTexto(container, context, "Mostrando cursos guardados:");
            mostrarCursos(container, context, cursosGuardados);
        } else {
            agregarTexto(container, context, "Sin conexión y sin datos guardados.");
        }
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
