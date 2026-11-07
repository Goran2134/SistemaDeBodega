package com.example.sistemadebodega;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sistemadebodega.utils.SessionManager;

public class Bienvenida extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bienvenida);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {

                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        // Eliminar cualquier sesión guardada anteriormente
        SessionManager sessionManager =
                new SessionManager(Bienvenida.this);

        sessionManager.cerrarSesion();

        // Mostrar bienvenida durante 2 segundos
        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            // Abrir siempre el inicio de sesión
            Intent intent = new Intent(
                    Bienvenida.this,
                    LoginActivity.class
            );

            // Limpiar las pantallas anteriores
            intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);
            finish();

        }, 2000);
    }
}
