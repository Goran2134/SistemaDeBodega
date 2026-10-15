package com.example.sistemadebodega;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sistemadebodega.R;
import com.example.sistemadebodega.data.AppDatabase;
import com.example.sistemadebodega.model.Categoria;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class CategoriasActivity extends AppCompatActivity {

    private EditText etNombreCategoria, etDescCategoria;
    private Button btnRegistrar;
    private RecyclerView rvCategorias;
    private AppDatabase db;
    private CategoriaAdapter categoriaAdapter;
    private List<Categoria> listaCategorias = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categorias);

        etNombreCategoria = findViewById(R.id.etNombreCategoriaReg);
        etDescCategoria = findViewById(R.id.etDescCategoriaReg);
        btnRegistrar = findViewById(R.id.btnRegistrarCategoria);
        rvCategorias = findViewById(R.id.rvCategorias);

        db = AppDatabase.getDatabase(getApplicationContext());

        rvCategorias.setLayoutManager(new LinearLayoutManager(this));

        // Configurar el adaptador con el listener de eliminación
        categoriaAdapter = new CategoriaAdapter(listaCategorias, categoria -> {
            new AlertDialog.Builder(CategoriasActivity.this)
                    .setTitle("Eliminar Categoría")
                    .setMessage("¿Está seguro de eliminar la categoría '" + categoria.nombre_categoria + "'?")
                    .setPositiveButton("Sí, eliminar", (dialog, which) -> {
                        eliminarCategoriaDeBaseDeDatos(categoria);
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        rvCategorias.setAdapter(categoriaAdapter);

        cargarCategoriasActivas();

        // Registrar nueva categoría (CU-09)
        btnRegistrar.setOnClickListener(v -> {
            String nombre = etNombreCategoria.getText().toString().trim();
            String descripcion = etDescCategoria.getText().toString().trim();

            if (nombre.isEmpty()) {
                Toast.makeText(this, getString(R.string.msg_fill_fields), Toast.LENGTH_SHORT).show();
                return;
            }

            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    Categoria nueva = new Categoria();
                    nueva.nombre_categoria = nombre;
                    nueva.descripcion = descripcion;
                    nueva.activo = 1; // Activo por defecto

                    db.categoriaDao().insertarCategoria(nueva);

                    runOnUiThread(() -> {
                        Toast.makeText(this, getString(R.string.msg_categoria_registrada), Toast.LENGTH_SHORT).show();
                        etNombreCategoria.setText("");
                        etDescCategoria.setText("");
                        cargarCategoriasActivas(); // Actualizar listado
                    });
                } catch (Exception e) {
                    // Interceptar nombre duplicado (Restricción UNIQUE / Flujo alternativo 5a)
                    runOnUiThread(() -> {
                        Toast.makeText(this, getString(R.string.error_categoria_duplicada), Toast.LENGTH_LONG).show();
                        etNombreCategoria.requestFocus();
                    });
                }
            });
        });
    }

    // Consultar categorías activas ordenadas alfabéticamente
    private void cargarCategoriasActivas() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Categoria> categoriasDao = db.categoriaDao().obtenerCategoriasActivas();
            runOnUiThread(() -> {
                categoriaAdapter.actualizarLista(categoriasDao);
            });
        });
    }

    private void eliminarCategoriaDeBaseDeDatos(Categoria categoria) {
        Executors.newSingleThreadExecutor().execute(() -> {
            db.categoriaDao().eliminarCategoria(categoria);
            runOnUiThread(() -> {
                Toast.makeText(this, getString(R.string.msg_categoria_eliminada), Toast.LENGTH_SHORT).show();
                cargarCategoriasActivas(); // Recargar el listado tras eliminar
            });
        });
    }
}