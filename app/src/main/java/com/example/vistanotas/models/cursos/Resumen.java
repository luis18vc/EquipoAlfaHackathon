package com.example.vistanotas.models.cursos;

public class Resumen {
    private String campus;
    private String cursosInscritos;
    private String cicloRelativo;
    private String totalCreditos;
    private String horasSemanales;

    public String getCampus() {
        return campus;
    }

    public void setCampus(String campus) {
        this.campus = campus;
    }

    public String getCursosInscritos() {
        return cursosInscritos;
    }

    public void setCursosInscritos(String cursosInscritos) {
        this.cursosInscritos = cursosInscritos;
    }

    public String getCicloRelativo() {
        return cicloRelativo;
    }

    public void setCicloRelativo(String cicloRelativo) {
        this.cicloRelativo = cicloRelativo;
    }

    public String getTotalCreditos() {
        return totalCreditos;
    }

    public void setTotalCreditos(String totalCreditos) {
        this.totalCreditos = totalCreditos;
    }

    public String getHorasSemanales() {
        return horasSemanales;
    }

    public void setHorasSemanales(String horasSemanales) {
        this.horasSemanales = horasSemanales;
    }
}
