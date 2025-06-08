package com.example.vistanotas.models.calendario;

import java.util.List;

public class CalendarioResponse {
    private  String fecha;
    private List<Clase> clases;

    public List<Clase> getClases() {
        return clases;
    }

    public void setClases(List<Clase> clases) {
        this.clases = clases;
    }
}
