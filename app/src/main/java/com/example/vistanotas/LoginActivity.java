package com.example.vistanotas;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vistanotas.interfaces.ApiService;
import com.example.vistanotas.models.sesion.LoginRequest;
import com.example.vistanotas.models.sesion.LoginResponse;
import com.example.vistanotas.models.sesion.Usuario;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsuario, etContrasena;
    private Button btnIngresar;

    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_LOGGED_IN = "isLoggedIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Si ya está logueado, ir directo a MainActivity
        if (preferences.getBoolean(KEY_LOGGED_IN, false)) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_login);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        etUsuario = findViewById(R.id.etUsuario);
        etContrasena = findViewById(R.id.etContrasena);
        btnIngresar = findViewById(R.id.btnIngresar);

        btnIngresar.setOnClickListener(v -> {
            String usuario = etUsuario.getText().toString().trim().toLowerCase();  // Siempre minúscula
            String contrasena = etContrasena.getText().toString().trim();

            if(usuario.isEmpty() || contrasena.isEmpty()) {
                Toast.makeText(this, "Por favor ingrese usuario y contraseña", Toast.LENGTH_SHORT).show();
                return;
            }

            LoginRequest request = new LoginRequest(usuario, contrasena);

            ApiService apiService = ApiClient.getClient().create(ApiService.class);
            Call<LoginResponse> call = apiService.iniciarSesion(request);
            call.enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    Log.d("API_RESPONSE", "Código: " + response.code() + " | Body: " + response.body()); // ✅ Log de la respuesta

                    if(response.isSuccessful() && response.body() != null) {
                        LoginResponse loginResponse = response.body();

                        String token = loginResponse.getAccess();
                        Usuario user = loginResponse.getUser();

                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean(KEY_LOGGED_IN, true);
                        editor.putString("token", token);

                        if (user != null) {
                            editor.putInt("user_id", user.getId());
                            editor.putString("user_usuario", user.getUsuario());
                            editor.putString("user_cod", user.getCod());
                        }

                        editor.apply();

                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Credenciales inválidas", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    Toast.makeText(LoginActivity.this, "Error en conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("API_ERROR", "Error en conexión: ", t);
                }
            });
        });
    }
}
