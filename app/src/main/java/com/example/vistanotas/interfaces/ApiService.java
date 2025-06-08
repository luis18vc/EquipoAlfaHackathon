package com.example.vistanotas.interfaces;

import com.example.vistanotas.models.calendario.CalendarioRequest;
import com.example.vistanotas.models.calendario.CalendarioResponse;
import com.example.vistanotas.models.cursos.Curso;
import com.example.vistanotas.models.cursos.CursosResponse;
import com.example.vistanotas.models.notas.NotasResponse;
import com.example.vistanotas.models.pagos.Pago;
import com.example.vistanotas.models.pagos.PagosResponse;
import com.example.vistanotas.models.sesion.LoginRequest;
import com.example.vistanotas.models.sesion.LoginResponse;


import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {

    @GET("/api/v1/cursos/obtener-cursos")
    Call<CursosResponse> obtenerCursos(@Header("Authorization") String token);

    @GET("/api/v1/pagos/obtener-pagos")
    Call<List<Pago>> obtenerPagos(@Header("Authorization") String token);


    @GET("/api/v1/notas/{cursoId}/obtener-notas")
    Call<NotasResponse> obtenerNotas(
            @Path("cursoId") String cursoId,
            @Header("Authorization") String token
    );

    @POST("/api/v1/calendario/portal-calendario")
    Call<CalendarioResponse> obtenerCalendario(
            @Header("Authorization") String token,
            @Body CalendarioRequest request
    );
    @POST("/api/v1/users/iniciar-sesion")
    Call<LoginResponse> iniciarSesion(@Body LoginRequest request);
}
