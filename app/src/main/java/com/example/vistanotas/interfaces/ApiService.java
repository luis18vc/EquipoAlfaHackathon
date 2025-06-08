package com.example.vistanotas.interfaces;

import com.example.vistanotas.models.Curso;

import retrofit2.Call;
import retrofit2.http.GET;
import java.util.List;
public interface ApiService {
    @GET("/api/v1/notas")
    Call<List<Curso>> obtenerCursos();
}