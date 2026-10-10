package com.example.sistemadebodega;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sistemadebodega.utils.SessionManager;

public class DashboardActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        sessionManager = new SessionManager(this);

        // Validar si existe sesión activa
        if (!sessionManager.isLoggedIn()) {
            redirigirLogin();
            return;
        }

        TextView tvBienvenida = findViewById(R.id.tvBienvenidaDashboard);
        Button btnMenuUsuarios = findViewById(R.id.btnMenuUsuarios);
        Button btnMenuCategorias = findViewById(R.id.btnMenuCategorias);
        Button btnCerrarSesion = findViewById(R.id.btnCerrarSesion);

        String nombre = sessionManager.obtenerNombreCompleto();
        tvBienvenida.setText("Bienvenido, " + nombre);

        // Control de acceso al módulo de usuarios con id_rol == 1)
        btnMenuUsuarios.setOnClickListener(v -> {
            int rolId = sessionManager.obtenerIdRol();
            if (rolId == 1) {
                Intent intent = new Intent(DashboardActivity.this, UsuariosActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(DashboardActivity.this, "Acceso restringido a Administradores", Toast.LENGTH_SHORT).show();
            }
        });

        // Acceso al módulo de categorías
        btnMenuCategorias.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, CategoriasActivity.class);
            startActivity(intent);
        });

        // Lógica de Cierre de Sesión
        btnCerrarSesion.setOnClickListener(v -> {
            new AlertDialog.Builder(DashboardActivity.this)
                    .setTitle(getString(R.string.dialog_logout_titulo))
                    .setMessage(getString(R.string.dialog_logout_mensaje))
                    .setPositiveButton(getString(R.string.dialog_si), (dialog, which) -> {
                        // Limpiar datos de sesión local
                        sessionManager.cerrarSesion();
                        // Redirigir al Login limpiando la pila de actividades
                        redirigirLogin();
                    })
                    .setNegativeButton(getString(R.string.dialog_cancelar), null)
                    .show();
        });
    }

    private void redirigirLogin() {
        Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}