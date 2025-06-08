package com.example.vistanotas.models.sesion;
public class LoginRequest {
    private String cod;
    private String contraseña;

    public LoginRequest(String cod, String contraseña) {
        this.cod = cod;
        this.contraseña = contraseña;
    }

    // Getters y Setters (opcional si usas Gson)
    public String getCod() {
        return cod;
    }

    public void setCod(String cod) {
        this.cod = cod;
    }

    public String getPassword() {
        return contraseña;
    }

    public void setPassword(String password) {
        this.contraseña = password;
    }
}
