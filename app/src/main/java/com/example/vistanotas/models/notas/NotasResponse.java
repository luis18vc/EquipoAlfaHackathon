package com.example.vistanotas.models.notas;
import java.util.List;

public class NotasResponse {
    private String curso;
    private List<Nota> notas;

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public List<Nota> getNotas() {
        return notas;
    }

    public void setNotas(List<Nota> notas) {
        this.notas = notas;
    }
}
