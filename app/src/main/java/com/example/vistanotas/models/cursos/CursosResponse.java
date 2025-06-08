package com.example.vistanotas.models.cursos;

import java.util.List;

public class CursosResponse {
    private Resumen resumen;
    private List<Curso> cursos;

    public Resumen getResumen() {
        return resumen;
    }

    public void setResumen(Resumen resumen) {
        this.resumen = resumen;
    }

    public List<Curso> getCursos() {
        return cursos;
    }

    public void setCursos(List<Curso> cursos) {
        this.cursos = cursos;
    }
}

