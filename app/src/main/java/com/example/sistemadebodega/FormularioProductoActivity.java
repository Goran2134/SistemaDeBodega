package com.example.sistemadebodega;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sistemadebodega.data.AppDatabase;
import com.example.sistemadebodega.model.Categoria;
import com.example.sistemadebodega.model.Producto;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FormularioProductoActivity extends AppCompatActivity {

    private EditText etSku, etNombre, etDescripcion, etStock;
    private Spinner spCategoria, spUnidad;
    private Button btnGuardarProducto;

    private AppDatabase db;
    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private final List<Categoria> categorias = new ArrayList<>();

    private final String[] unidades = {
            "Unidad", "Kilogramo", "Gramo",
            "Litro", "Mililitro", "Caja", "Paquete"
    };

    private int productoId = -1;
    private Producto productoActual;
    private boolean guardando = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario_producto);

        etSku = findViewById(R.id.etSkuProducto);
        etNombre = findViewById(R.id.etNombreProducto);
        etDescripcion = findViewById(R.id.etDescripcionProducto);
        etStock = findViewById(R.id.etStockInicial);
        spCategoria = findViewById(R.id.spCategoriaProducto);
        spUnidad = findViewById(R.id.spUnidadProducto);
        btnGuardarProducto = findViewById(R.id.btnGuardarProducto);

        db = AppDatabase.getDatabase(getApplicationContext());

        productoId = getIntent().getIntExtra("producto_id", -1);

        ArrayAdapter<String> unidadAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                unidades
        );
        unidadAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );
        spUnidad.setAdapter(unidadAdapter);

        if (productoId != -1) {
            btnGuardarProducto.setText("Guardar cambios");

            // El stock debe cambiarse mediante movimientos
            // de inventario, no editando directamente el producto.
            etStock.setEnabled(false);
            etStock.setFocusable(false);
        }

        cargarDatos();

        btnGuardarProducto.setOnClickListener(v -> guardarProducto());

        findViewById(R.id.btnCancelarProducto)
                .setOnClickListener(v -> finish());
    }

    private void cargarDatos() {
        btnGuardarProducto.setEnabled(false);

        executor.execute(() -> {
            try {
                List<Categoria> resultado =
                        db.categoriaDao().obtenerCategoriasActivas();

                Producto encontrado = null;

                if (productoId != -1) {
                    encontrado = db.productoDao()
                            .obtenerPorId(productoId);
                }

                Producto finalEncontrado = encontrado;

                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;

                    categorias.clear();
                    categorias.addAll(resultado);

                    List<String> nombres = new ArrayList<>();

                    for (Categoria categoria : categorias) {
                        nombres.add(categoria.nombre_categoria);
                    }

                    ArrayAdapter<String> adapter =
                            new ArrayAdapter<>(
                                    this,
                                    android.R.layout.simple_spinner_item,
                                    nombres
                            );

                    adapter.setDropDownViewResource(
                            android.R.layout.simple_spinner_dropdown_item
                    );

                    spCategoria.setAdapter(adapter);

                    if (productoId != -1) {
                        if (finalEncontrado == null) {
                            Toast.makeText(
                                    this,
                                    "Producto no encontrado",
                                    Toast.LENGTH_SHORT
                            ).show();
                            finish();
                            return;
                        }

                        productoActual = finalEncontrado;

                        etSku.setText(productoActual.codigo_sku);
                        etNombre.setText(productoActual.nombre);
                        etDescripcion.setText(productoActual.descripcion);
                        etStock.setText(
                                String.valueOf(productoActual.stock_actual)
                        );

                        for (int i = 0; i < unidades.length; i++) {
                            if (unidades[i].equals(
                                    productoActual.unidad_medida)) {
                                spUnidad.setSelection(i);
                                break;
                            }
                        }

                        boolean categoriaEncontrada = false;

                        for (int i = 0; i < categorias.size(); i++) {
                            if (categorias.get(i).id_categoria
                                    == productoActual.id_categoria) {
                                spCategoria.setSelection(i);
                                categoriaEncontrada = true;
                                break;
                            }
                        }

                        if (!categoriaEncontrada) {
                            Toast.makeText(
                                    this,
                                    "La categoría del producto no está activa",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    btnGuardarProducto.setEnabled(
                            !categorias.isEmpty()
                    );

                    if (categorias.isEmpty()) {
                        Toast.makeText(
                                this,
                                "No hay categorías activas",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    if (!isFinishing() && !isDestroyed()) {
                        Toast.makeText(
                                this,
                                "Error al cargar el formulario",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
            }
        });
    }

    private void guardarProducto() {
        if (guardando) return;

        String sku = etSku.getText().toString().trim();
        String nombre = etNombre.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String stockTexto = etStock.getText().toString().trim();

        if (sku.isEmpty()) {
            etSku.setError("Ingrese el código SKU");
            etSku.requestFocus();
            return;
        }

        if (nombre.isEmpty()) {
            etNombre.setError("Ingrese el nombre");
            etNombre.requestFocus();
            return;
        }

        int posicion = spCategoria.getSelectedItemPosition();

        if (posicion < 0 || posicion >= categorias.size()) {
            Toast.makeText(
                    this,
                    "Seleccione una categoría válida",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        int stock = 0;

        if (productoId == -1) {
            try {
                stock = Integer.parseInt(stockTexto);

                if (stock < 0) {
                    etStock.setError("El stock no puede ser negativo");
                    return;
                }

            } catch (NumberFormatException e) {
                etStock.setError("Ingrese un stock válido");
                return;
            }
        }

        int idCategoria = categorias.get(posicion).id_categoria;
        String unidad = unidades[spUnidad.getSelectedItemPosition()];

        Producto producto;

        if (productoId != -1) {
            if (productoActual == null) return;
            producto = productoActual;
        } else {
            producto = new Producto();
            producto.stock_actual = stock;
            producto.activo = 1;
        }

        producto.codigo_sku = sku;
        producto.nombre = nombre;
        producto.descripcion = descripcion;
        producto.id_categoria = idCategoria;
        producto.unidad_medida = unidad;

        guardando = true;
        btnGuardarProducto.setEnabled(false);

        executor.execute(() -> {
            try {
                int duplicados;

                if (productoId == -1) {
                    duplicados = db.productoDao().contarPorSku(sku);
                } else {
                    duplicados = db.productoDao()
                            .contarSkuDuplicado(sku, productoId);
                }

                if (duplicados > 0) {
                    runOnUiThread(() -> {
                        if (isFinishing() || isDestroyed()) return;

                        guardando = false;
                        btnGuardarProducto.setEnabled(true);
                        etSku.setError("Este SKU ya existe");
                        etSku.requestFocus();
                    });
                    return;
                }

                if (productoId == -1) {
                    db.productoDao().insertarProducto(producto);
                } else {
                    db.productoDao().actualizarProducto(producto);
                }

                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;

                    Toast.makeText(
                            this,
                            productoId == -1
                                    ? "Producto registrado"
                                    : "Producto actualizado",
                            Toast.LENGTH_SHORT
                    ).show();

                    setResult(RESULT_OK);
                    finish();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;

                    guardando = false;
                    btnGuardarProducto.setEnabled(true);

                    Toast.makeText(
                            this,
                            "Error al guardar el producto",
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        executor.shutdown();
        super.onDestroy();
    }
}
