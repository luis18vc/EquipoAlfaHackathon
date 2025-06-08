package com.example.vistanotas.ui.cursos;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.vistanotas.R;
import com.example.vistanotas.databinding.FragmentCursosBinding;

public class CursosFragment extends Fragment {

    private FragmentCursosBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        CursosViewModel cursosViewModel =
                new ViewModelProvider(this).get(CursosViewModel.class);

        binding = FragmentCursosBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        LinearLayout layoutCursos = binding.layoutCursos;

        cursosViewModel.getCursos().observe(getViewLifecycleOwner(), cursos -> {
            for (String curso : cursos) {
                Button btnCurso = new Button(getContext());
                btnCurso.setText(curso);
                btnCurso.setAllCaps(false);
                btnCurso.setTextColor(getResources().getColor(android.R.color.white, null));
                btnCurso.setBackgroundTintList(getResources().getColorStateList(R.color.blue, null));

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 24, 0, 0);
                btnCurso.setLayoutParams(params);

                layoutCursos.addView(btnCurso);
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}