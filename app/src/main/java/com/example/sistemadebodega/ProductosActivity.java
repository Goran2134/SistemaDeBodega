package com.example.sistemadebodega;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sistemadebodega.data.AppDatabase;
import com.example.sistemadebodega.model.Producto;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanIntentResult;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductosActivity extends AppCompatActivity {

    private EditText etBuscarProducto;
    private TextView tvCantidadProductos;
    private RecyclerView rvProductos;
    private Spinner spFiltroEstado;

    private ProductoAdapter adapter;
    private AppDatabase db;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private int numeroConsulta = 0;

    private final String[] filtros = {
            "Productos activos",
            "Productos deshabilitados",
            "Todos los productos"
    };

    // RF-20: RESULTADO DEL ESCÁNER
    private final ActivityResultLauncher<ScanOptions> escanerLauncher =
            registerForActivityResult(
                    new ScanContract(),
                    this::procesarCodigoEscaneado
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_productos);

        etBuscarProducto = findViewById(R.id.etBuscarProducto);
        tvCantidadProductos = findViewById(R.id.tvCantidadProductos);
        rvProductos = findViewById(R.id.rvProductos);
        spFiltroEstado = findViewById(R.id.spFiltroEstado);

        Button btnRegistrarProducto =
                findViewById(R.id.btnRegistrarProducto);

        Button btnVolver =
                findViewById(R.id.btnVolverProductos);

        Button btnEscanearCodigo =
                findViewById(R.id.btnEscanearCodigo);

        btnVolver.setOnClickListener(v -> finish());

        db = AppDatabase.getDatabase(getApplicationContext());

        adapter = new ProductoAdapter(
                new ProductoAdapter.OnProductoAccionListener() {

                    @Override
                    public void onEditar(Producto producto) {
                        Intent intent = new Intent(
                                ProductosActivity.this,
                                FormularioProductoActivity.class
                        );

                        intent.putExtra(
                                "producto_id",
                                producto.id_producto
                        );

                        startActivity(intent);
                    }

                    @Override
                    public void onDeshabilitar(Producto producto) {
                        confirmarCambioEstado(producto, false);
                    }

                    @Override
                    public void onReactivar(Producto producto) {
                        confirmarCambioEstado(producto, true);
                    }
                }
        );

        rvProductos.setLayoutManager(
                new LinearLayoutManager(this)
        );

        rvProductos.setAdapter(adapter);

        ArrayAdapter<String> filtroAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                filtros
        );

        filtroAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spFiltroEstado.setAdapter(filtroAdapter);

        spFiltroEstado.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            android.view.View view,
                            int position,
                            long id
                    ) {
                        cargarProductos();
                    }

                    @Override
                    public void onNothingSelected(
                            android.widget.AdapterView<?> parent
                    ) {
                    }
                }
        );

        // REGISTRAR PRODUCTO
        btnRegistrarProducto.setOnClickListener(v -> {
            startActivity(new Intent(
                    this,
                    FormularioProductoActivity.class
            ));
        });

        // RF-20: ABRIR ESCÁNER
        btnEscanearCodigo.setOnClickListener(v -> abrirEscaner());

        // BÚSQUEDA POR NOMBRE O SKU
        etBuscarProducto.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after
            ) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count
            ) {
                cargarProductos();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    // RF-20: INICIAR ESCANEO
    private void abrirEscaner() {

        ScanOptions opciones = new ScanOptions();

        opciones.setDesiredBarcodeFormats(
                ScanOptions.ALL_CODE_TYPES
        );

        opciones.setPrompt(
                "Enfoque el código de barras del producto"
        );

        opciones.setBeepEnabled(true);
        opciones.setOrientationLocked(false);
        opciones.setBarcodeImageEnabled(false);

        escanerLauncher.launch(opciones);
    }

    // RF-20: PROCESAR CÓDIGO LEÍDO
    private void procesarCodigoEscaneado(ScanIntentResult resultado) {

        if (resultado.getContents() == null) {

            Toast.makeText(
                    this,
                    "Escaneo cancelado",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String codigo = resultado.getContents().trim();

        if (codigo.isEmpty()) {
            Toast.makeText(
                    this,
                    "No se pudo leer el código",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Mostrar todos los productos para que el filtro
        // de estado no oculte un producto escaneado.
        spFiltroEstado.setSelection(2);

        // Reutilizar la búsqueda existente.
        etBuscarProducto.setText(codigo);
        etBuscarProducto.setSelection(codigo.length());

        Toast.makeText(
                this,
                "Código escaneado: " + codigo,
                Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarProductos();
    }

    private void cargarProductos() {

        String texto = etBuscarProducto.getText()
                .toString()
                .trim();

        int filtro = spFiltroEstado.getSelectedItemPosition();

        final int consultaActual = ++numeroConsulta;

        executor.execute(() -> {
            try {

                List<Producto> todos =
                        db.productoDao().obtenerTodosLosProductos();

                List<Producto> resultado = new ArrayList<>();

                for (Producto producto : todos) {

                    if (filtro == 0 && producto.activo != 1) {
                        continue;
                    }

                    if (filtro == 1 && producto.activo != 0) {
                        continue;
                    }

                    String nombre = producto.nombre == null
                            ? "" : producto.nombre;

                    String sku = producto.codigo_sku == null
                            ? "" : producto.codigo_sku;

                    if (!texto.isEmpty()) {

                        String busqueda =
                                texto.toLowerCase(Locale.ROOT);

                        if (!nombre.toLowerCase(Locale.ROOT)
                                .contains(busqueda)
                                && !sku.toLowerCase(Locale.ROOT)
                                .contains(busqueda)) {
                            continue;
                        }
                    }

                    resultado.add(producto);
                }

                runOnUiThread(() -> {

                    if (isFinishing()
                            || isDestroyed()
                            || consultaActual != numeroConsulta) {
                        return;
                    }

                    adapter.actualizarLista(resultado);

                    tvCantidadProductos.setText(
                            resultado.isEmpty()
                                    ? "No se encontraron productos"
                                    : resultado.size()
                                      + (resultado.size() == 1
                                         ? " producto encontrado"
                                         : " productos encontrados")
                    );
                });

            } catch (Exception e) {

                runOnUiThread(() -> {

                    if (!isFinishing() && !isDestroyed()) {

                        Toast.makeText(
                                this,
                                "Error al cargar productos",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
            }
        });
    }

    private void confirmarCambioEstado(
            Producto producto,
            boolean reactivar
    ) {

        String accion = reactivar
                ? "Reactivar"
                : "Deshabilitar";

        String mensaje = reactivar
                ? "¿Desea reactivar el producto \""
                  + producto.nombre + "\"?"
                : "¿Desea deshabilitar el producto \""
                  + producto.nombre + "\"?\n\n"
                  + "El producto no será eliminado "
                  + "de la base de datos.";

        new AlertDialog.Builder(this)
                .setTitle(accion + " producto")
                .setMessage(mensaje)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton(accion, (dialog, which) -> {
                    cambiarEstadoProducto(
                            producto.id_producto,
                            reactivar
                    );
                })
                .show();
    }

    private void cambiarEstadoProducto(
            int productoId,
            boolean reactivar
    ) {

        executor.execute(() -> {
            try {

                int filas;

                if (reactivar) {
                    filas = db.productoDao()
                            .habilitarProducto(productoId);
                } else {
                    filas = db.productoDao()
                            .deshabilitarProducto(productoId);
                }

                runOnUiThread(() -> {

                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    if (filas > 0) {

                        Toast.makeText(
                                this,
                                reactivar
                                        ? "Producto reactivado correctamente"
                                        : "Producto deshabilitado correctamente",
                                Toast.LENGTH_SHORT
                        ).show();

                        cargarProductos();

                    } else {

                        Toast.makeText(
                                this,
                                "No se pudo actualizar el producto",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });

            } catch (Exception e) {

                runOnUiThread(() -> {

                    if (!isFinishing() && !isDestroyed()) {

                        Toast.makeText(
                                this,
                                "Error al actualizar el estado",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
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
