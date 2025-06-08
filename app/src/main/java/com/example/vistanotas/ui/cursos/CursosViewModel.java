package com.example.vistanotas.ui.cursos;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Arrays;
import java.util.List;

public class CursosViewModel extends ViewModel {

    private final MutableLiveData<List<String>> cursos;

    public CursosViewModel() {
        cursos = new MutableLiveData<>();
        cursos.setValue(Arrays.asList(
                "Gestión de proyectos",
                "Comunicación",
                "Programación",
                "Ciencias",
                "Economía"
        ));
    }

    public LiveData<List<String>> getCursos() {
        return cursos;
    }
}