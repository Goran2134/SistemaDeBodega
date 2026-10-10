package com.example.sistemadebodega;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sistemadebodega.data.AppDatabase;
import com.example.sistemadebodega.dao.UsuarioDao;
import com.example.sistemadebodega.model.Usuario;
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

        // Configurar el adaptador pasando el listener de eliminación
        usuarioAdapter = new UsuarioAdapter(listaUsuarios, usuarioConRol -> {
            // Mostrar diálogo de confirmación antes de eliminar
            new AlertDialog.Builder(UsuariosActivity.this)
                    .setTitle("Eliminar Usuario")
                    .setMessage("¿Está seguro de eliminar al usuario '" + usuarioConRol.username + "'?")
                    .setPositiveButton("Sí, eliminar", (dialog, which) -> {
                        eliminarUsuarioDeBaseDeDatos(usuarioConRol);
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        rvUsuarios.setAdapter(usuarioAdapter);

        fabNuevoUsuario.setOnClickListener(v -> {
            Intent intent = new Intent(UsuariosActivity.this, RegistrarUsuarioActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarUsuarios();
    }

    private void cargarUsuarios() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<UsuarioDao.UsuarioConRol> usuariosDao = db.usuarioDao().obtenerUsuariosConRoles();
            runOnUiThread(() -> {
                usuarioAdapter.actualizarLista(usuariosDao);
            });
        });
    }

    private void eliminarUsuarioDeBaseDeDatos(UsuarioDao.UsuarioConRol usuarioConRol) {
        Executors.newSingleThreadExecutor().execute(() -> {
            // Mapear de UsuarioConRol a la entidad Usuario para que Room pueda eliminarlo por su ID primario
            Usuario usuarioAEliminar = new Usuario();
            usuarioAEliminar.id_usuario = usuarioConRol.id_usuario;
            usuarioAEliminar.username = usuarioConRol.username;
            usuarioAEliminar.id_rol = usuarioConRol.id_rol;

            db.usuarioDao().eliminarUsuario(usuarioAEliminar);

            runOnUiThread(() -> {
                Toast.makeText(this, "Usuario eliminado exitosamente", Toast.LENGTH_SHORT).show();
                cargarUsuarios(); // Recargar el listado para reflejar los cambios
            });
        });
    }
}