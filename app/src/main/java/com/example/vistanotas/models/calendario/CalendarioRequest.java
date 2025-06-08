package com.example.vistanotas.models.calendario;

public class CalendarioRequest {
    private String date;

    public CalendarioRequest(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}