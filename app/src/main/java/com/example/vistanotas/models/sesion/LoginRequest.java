package com.example.vistanotas.models.sesion;
public class LoginRequest {
    private String cod;
    private String password;

    public LoginRequest(String cod, String password) {
        this.cod = cod;
        this.password = password;
    }

    // Getters y Setters (opcional si usas Gson)
    public String getCod() {
        return cod;
    }

    public void setCod(String cod) {
        this.cod = cod;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
