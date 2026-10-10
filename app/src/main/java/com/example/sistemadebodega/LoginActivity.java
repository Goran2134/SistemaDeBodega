package com.example.sistemadebodega;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sistemadebodega.data.AppDatabase;
import com.example.sistemadebodega.model.Usuario;
import com.example.sistemadebodega.utils.PasswordUtils;
import com.example.sistemadebodega.utils.SessionManager;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private EditText etUser, etPass;
    private Button btnLogin;
    private AppDatabase db;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        // Si ya hay una sesión activa, redirigir directamente al Dashboard
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        etUser = findViewById(R.id.etUser);
        etPass = findViewById(R.id.etPass);
        btnLogin = findViewById(R.id.btnLogin);

        db = AppDatabase.getDatabase(getApplicationContext());

        btnLogin.setOnClickListener(v -> {
            String username = etUser.getText().toString().trim();
            String password = etPass.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, getString(R.string.msg_fill_fields), Toast.LENGTH_SHORT).show();
                return;
            }

            // Consultar la base de datos fuera del hilo principal
            Executors.newSingleThreadExecutor().execute(() -> {
                Usuario usuario = db.usuarioDao().obtenerPorUsername(username);

                runOnUiThread(() -> {
                    if (usuario != null) {
                        // 1. Validar si el usuario está activo
                        if (usuario.activo == 0) {
                            Toast.makeText(LoginActivity.this, getString(R.string.error_usuario_inactivo), Toast.LENGTH_LONG).show();
                            return;
                        }

                        // 2. Verificar la contraseña usando PasswordUtils
                        if (PasswordUtils.verifyPassword(password, usuario.password_hash)) {
                            // 3. Persistir la sesión localmente y obtener el rol
                            sessionManager.crearSesion(usuario.username, usuario.nombre_completo, usuario.id_rol);

                            // 4. Redirigir al Dashboard
                            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // Credenciales incorrectas
                            mostrarErrorCredenciales();
                        }
                    } else {
                        // Usuario no existe
                        mostrarErrorCredenciales();
                    }
                });
            });
        });
    }

    private void mostrarErrorCredenciales() {
        Toast.makeText(LoginActivity.this, getString(R.string.error_credenciales), Toast.LENGTH_SHORT).show();
        etPass.setText("");
        etPass.requestFocus();
    }
}