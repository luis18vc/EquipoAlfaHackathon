package com.example.vistanotas.models.cursos;

public class Curso {
    private String idCurso;
    private String titulo;
    private String modoCurso;
    private String creditos;
    private String horasSemanales;
    private String formula;

    public String getIdCurso() {
        return idCurso;
    }

    public void setIdCurso(String idCurso) {
        this.idCurso = idCurso;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getModoCurso() {
        return modoCurso;
    }

    public void setModoCurso(String modoCurso) {
        this.modoCurso = modoCurso;
    }

    public String getCreditos() {
        return creditos;
    }

    public void setCreditos(String creditos) {
        this.creditos = creditos;
    }

    public String getHorasSemanales() {
        return horasSemanales;
    }

    public void setHorasSemanales(String horasSemanales) {
        this.horasSemanales = horasSemanales;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }
}
