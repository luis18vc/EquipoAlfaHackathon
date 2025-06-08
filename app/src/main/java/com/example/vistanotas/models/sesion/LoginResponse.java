package com.example.vistanotas.models.sesion;

public class LoginResponse {
    private Usuario user;
    private String access;

    public Usuario getUser() {
        return user;
    }

    public void setUser(Usuario user) {
        this.user = user;
    }

    public String getAccess() {
        return access;
    }

    public void setAccess(String access) {
        this.access = access;
    }
}
