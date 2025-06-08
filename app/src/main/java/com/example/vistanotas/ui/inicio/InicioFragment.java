package com.example.vistanotas.ui.inicio;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.vistanotas.ApiClient;
import com.example.vistanotas.databinding.FragmentInicioBinding;
import com.example.vistanotas.interfaces.ApiService;
import com.example.vistanotas.models.calendario.CalendarioRequest;
import com.example.vistanotas.models.calendario.CalendarioResponse;
import com.example.vistanotas.models.calendario.Clase;
import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InicioFragment extends Fragment {

    private FragmentInicioBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentInicioBinding.inflate(inflater, container, false);

        SharedPreferences preferences = requireContext().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);

        String studentCode = preferences.getString("user_cod", "CODIGO_NO_ENCONTRADO");
        String token = preferences.getString("token", "TOKEN_NO_ENCONTRADO");
        binding.studentCodeText.setText(studentCode);
        binding.barcodeNumberText.setText(studentCode);

        try {
            Bitmap barcode = generateBarcode(studentCode, 600, 200);
            binding.barcodeImageView.setImageBitmap(barcode);
        } catch (WriterException e) {
            Toast.makeText(requireContext(), "Error al generar código de barras", Toast.LENGTH_SHORT).show();
        }

        if (token != null && !token.equals("TOKEN_NO_ENCONTRADO")) {
            obtenerClasesDesdeApi(token);
        } else {
            Toast.makeText(requireContext(), "Token no encontrado", Toast.LENGTH_SHORT).show();
            cargarHorarioDesdeLocal();
        }

        return binding.getRoot();
    }

    private void obtenerClasesDesdeApi(String token) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("America/Lima"));
        String fechaHoy = dateFormat.format(new Date());
        Log.d("InicioFragment", "Fecha actual en Lima: " + fechaHoy);

        CalendarioRequest request = new CalendarioRequest(fechaHoy);
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<CalendarioResponse> call = apiService.obtenerCalendario("Bearer " + token, request);

        call.enqueue(new Callback<CalendarioResponse>() {
            @Override
            public void onResponse(@NonNull Call<CalendarioResponse> call, @NonNull Response<CalendarioResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Clase> clases = response.body().getClases();
                    if (clases != null && !clases.isEmpty()) {
                        guardarClasesLocalmente(response.body());
                        Clase claseProxima = encontrarClaseProxima(clases);
                        if (claseProxima != null) {
                            mostrarClaseEnVista(claseProxima);
                        } else {
                            mostrarClaseEnVista(clases.get(0));
                        }
                    } else {
                        limpiarVistaClase();
                        cargarHorarioDesdeLocal();
                    }
                } else {
                    limpiarVistaClase();
                    cargarHorarioDesdeLocal();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CalendarioResponse> call, @NonNull Throwable t) {
                Log.e("InicioFragment", "Error de red", t);
                limpiarVistaClase();
                cargarHorarioDesdeLocal();
            }
        });
    }

    private void guardarClasesLocalmente(CalendarioResponse calendario) {
        SharedPreferences.Editor editor = requireContext()
                .getSharedPreferences("ClasesPrefs", Context.MODE_PRIVATE)
                .edit();
        String json = new Gson().toJson(calendario);
        editor.putString("calendario_hoy", json);
        editor.apply();
    }

    private void cargarHorarioDesdeLocal() {
        SharedPreferences prefs = requireContext().getSharedPreferences("ClasesPrefs", Context.MODE_PRIVATE);
        String jsonGuardado = prefs.getString("calendario_hoy", null);

        if (jsonGuardado != null) {
            Gson gson = new Gson();
            CalendarioResponse calendario = gson.fromJson(jsonGuardado, CalendarioResponse.class);
            List<Clase> clases = calendario.getClases();
            if (clases != null && !clases.isEmpty()) {
                Clase claseProxima = encontrarClaseProxima(clases);
                if (claseProxima != null) {
                    mostrarClaseEnVista(claseProxima);
                } else {
                    mostrarClaseEnVista(clases.get(0));
                }
            } else {
                limpiarVistaClase();
                Log.d("InicioFragment", "No hay clases en los datos locales");
            }
        } else {
            limpiarVistaClase();
            Log.d("InicioFragment", "No hay datos locales guardados");
        }
    }

    private Clase encontrarClaseProxima(List<Clase> clases) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

        try {
            String horaActualStr = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
            Date horaActual = sdf.parse(horaActualStr);

            Clase claseProxima = null;
            long diferenciaMinima = Long.MAX_VALUE;

            for (Clase clase : clases) {
                String horaInicioStr = clase.getInicio();
                Date horaInicio = sdf.parse(horaInicioStr);

                long diff = horaInicio.getTime() - horaActual.getTime();
                if (diff >= 0 && diff < diferenciaMinima) {
                    diferenciaMinima = diff;
                    claseProxima = clase;
                }
            }
            return claseProxima;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void mostrarClaseEnVista(Clase clase) {
        binding.courseNameTv.setText(clase.getCurso());
        binding.courseTypeTv.setText("Presencial");
        binding.courseTimeTv.setText(clase.getInicio() + " - " + clase.getFin());
        binding.courseRoomTv.setText(clase.getSalon());
    }

    private void limpiarVistaClase() {
        binding.courseNameTv.setText("");
        binding.courseTypeTv.setText("");
        binding.courseTimeTv.setText("");
        binding.courseRoomTv.setText("");
    }

    private Bitmap generateBarcode(String data, int width, int height) throws WriterException {
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix matrix = writer.encode(data, BarcodeFormat.CODE_128, width, height);

        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = matrix.get(x, y) ? Color.BLACK : Color.WHITE;
            }
        }

        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmp.setPixels(pixels, 0, width, 0, 0, width, height);
        return bmp;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
