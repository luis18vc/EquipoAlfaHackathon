package com.example.vistanotas;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsuario, etContrasena;
    private Button btnIngresar;

    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_LOGGED_IN = "isLoggedIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Si ya está logueado, ir directo a MainActivity
        if (preferences.getBoolean(KEY_LOGGED_IN, false)) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);  // Enlazamos el XML

        etUsuario = findViewById(R.id.etUsuario);
        etContrasena = findViewById(R.id.etContrasena);
        btnIngresar = findViewById(R.id.btnIngresar);

        btnIngresar.setOnClickListener(v -> {
            String usuario = etUsuario.getText().toString();
            String contrasena = etContrasena.getText().toString();

            //Modificar para colocar los datos correctos del Login
            if (usuario.equals("alumno") && contrasena.equals("1234")) {
                Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();

                // Guardar sesión
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(KEY_LOGGED_IN, true);
                editor.apply();

                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Credenciales inválidas", Toast.LENGTH_SHORT).show();
            }
        });
    }
}