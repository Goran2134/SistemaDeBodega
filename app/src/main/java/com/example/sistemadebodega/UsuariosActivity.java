package com.example.sistemadebodega;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sistemadebodega.data.AppDatabase;
import com.example.sistemadebodega.dao.UsuarioDao;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class UsuariosActivity extends AppCompatActivity {

    private RecyclerView rvUsuarios;
    private FloatingActionButton fabNuevoUsuario;
    private AppDatabase db;
    private UsuarioAdapter usuarioAdapter;
    private List<UsuarioDao.UsuarioConRol> listaUsuarios = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuarios);

        rvUsuarios = findViewById(R.id.rvUsuarios);
        fabNuevoUsuario = findViewById(R.id.fabNuevoUsuario);
        db = AppDatabase.getDatabase(getApplicationContext());

        rvUsuarios.setLayoutManager(new LinearLayoutManager(this));
        usuarioAdapter = new UsuarioAdapter(listaUsuarios);
        rvUsuarios.setAdapter(usuarioAdapter);

        fabNuevoUsuario.setOnClickListener(v -> {
            Intent intent = new Intent(UsuariosActivity.this, RegistrarUsuarioActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarUsuarios(); // Actualizar listado al volver de registrar un usuario
    }

    private void cargarUsuarios() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<UsuarioDao.UsuarioConRol> usuariosDao = db.usuarioDao().obtenerUsuariosConRoles();
            runOnUiThread(() -> {
                usuarioAdapter.actualizarLista(usuariosDao);
            });
        });
    }
}