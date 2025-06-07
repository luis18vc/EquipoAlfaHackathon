package com.example.vistanotas.ui.cursos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.vistanotas.databinding.FragmentCursosBinding;

public class CursosFragment extends Fragment {

    private FragmentCursosBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        CursosViewModel cursosViewModel =
                new ViewModelProvider(this).get(CursosViewModel.class);

        binding = FragmentCursosBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textView39;
        cursosViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}