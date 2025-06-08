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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.vistanotas.ApiClient;
import com.example.vistanotas.R;
import com.example.vistanotas.databinding.FragmentCursosBinding;
import com.example.vistanotas.interfaces.ApiService;
import com.example.vistanotas.models.cursos.Curso;
import com.example.vistanotas.models.cursos.CursosResponse;

import java.util.List;
import android.graphics.Color;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CursosFragment extends Fragment {

    private FragmentCursosBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentCursosBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        LinearLayout layoutCursos = binding.layoutCursos;

        // Llamamos a la API para cargar cursos y mostrarlos en botones
        obtenerCursosDesdeApi(layoutCursos, getContext());

        return root;
    }

    private void obtenerCursosDesdeApi(LinearLayout eventContainer, Context context) {
        eventContainer.removeAllViews();

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        SharedPreferences preferences = context.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        String token = preferences.getString("token", null);
        String bearerToken = "Bearer " + token;
        if (token == null) {
            Toast.makeText(context, "No se encontró token de acceso. Por favor, inicia sesión.", Toast.LENGTH_SHORT).show();
            return; // No seguir si no hay token
        }
        Call<CursosResponse> call = apiService.obtenerCursos(bearerToken);

        call.enqueue(new Callback<CursosResponse>() {
            @Override
            public void onResponse(Call<CursosResponse> call, Response<CursosResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CursosResponse res = response.body();

                    List<Curso> cursos = res.getCursos();

                    if (cursos == null || cursos.isEmpty()) {
                        agregarTexto(eventContainer, context, "No hay cursos matriculados");
                    } else {
                        for (Curso c : cursos) {
                            Button btnCurso = new Button(context);
                            btnCurso.setText(c.getTitulo());
                            btnCurso.setAllCaps(false);
                            btnCurso.setTextColor(Color.parseColor("#FFFFFF"));  // Blanco
                            btnCurso.setBackgroundColor(Color.parseColor("#2196F3"));

                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                            );
                            params.setMargins(0, 24, 0, 0);
                            btnCurso.setLayoutParams(params);

                            btnCurso.setOnClickListener(v -> {
                                Toast.makeText(context, "Curso seleccionado: " + c.getTitulo(), Toast.LENGTH_SHORT).show();

                                Bundle bundle = new Bundle();
                                bundle.putString("idCurso", c.getIdCurso());

                                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
                                navController.navigate(R.id.action_cursosFragment_to_notasFragment, bundle);
                            });

                            eventContainer.addView(btnCurso);
                        }
                    }
                } else {
                    Toast.makeText(context, "Error al cargar los cursos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CursosResponse> call, Throwable t) {
                Toast.makeText(context, "Fallo en la conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("CursosAPI", "Error: ", t);
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
