package com.example.vistanotas.ui.inicio;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.vistanotas.databinding.FragmentInicioBinding;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

public class InicioFragment extends Fragment {

    private FragmentInicioBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentInicioBinding.inflate(inflater, container, false);

        // 1. Seguir usando tu ViewModel para el texto principal
        InicioViewModel inicioViewModel =
                new ViewModelProvider(this).get(InicioViewModel.class);
        inicioViewModel.getText().observe(getViewLifecycleOwner(),
                binding.textHome::setText);

        // 2. Lógica del código de barras
        String studentCode = "U21212121";
        binding.studentCodeText.setText(studentCode);      // arriba de la imagen
        binding.barcodeNumberText.setText(studentCode);    // debajo de la imagen

        try {
            Bitmap barcode = generateBarcode(studentCode, 600, 200); // ancho-alto a tu gusto
            binding.barcodeImageView.setImageBitmap(barcode);
        } catch (WriterException e) {
            Toast.makeText(requireContext(),
                    "Error al generar código de barras: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }

        return binding.getRoot();
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
