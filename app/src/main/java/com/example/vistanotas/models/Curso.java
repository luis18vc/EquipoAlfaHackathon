package com.example.vistanotas.models;

public class Curso {
    private String nombre;
    private double nota;
    private int porcentaje;

    // Getters y setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public double getNota() { return nota; }
    public void setNota(double nota) { this.nota = nota; }

    public int getPorcentaje() { return porcentaje; }
    public void setPorcentaje(int porcentaje) { this.porcentaje = porcentaje; }
}
