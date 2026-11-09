package com.example.sistemadebodega;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sistemadebodega.data.AppDatabase;
import com.example.sistemadebodega.model.Producto;
import com.example.sistemadebodega.repository.MovimientoRepository;
import com.example.sistemadebodega.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FormularioMovimientoActivity extends AppCompatActivity {

    private TextView tvTitulo, tvStockActual;
    private Spinner spProductos;
    private EditText etCantidad, etMotivo, etObservaciones;
    private Button btnConfirmar, btnCancelar;

    private AppDatabase db;
    private MovimientoRepository movimientoRepository;
    private SessionManager sessionManager;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private String tipoMovimiento = "ENTRADA";
    private final List<Producto> listaProductos = new ArrayList<>();
    private Producto productoSeleccionado = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario_movimiento);

        tipoMovimiento = getIntent().getStringExtra("tipo_movimiento");
        if (tipoMovimiento == null) tipoMovimiento = "ENTRADA";

        tvTitulo = findViewById(R.id.tvTituloFormMovimiento);
        tvStockActual = findViewById(R.id.tvStockActualForm);
        spProductos = findViewById(R.id.spProductosMovimiento);
        etCantidad = findViewById(R.id.etCantidadMovimiento);
        etMotivo = findViewById(R.id.etMotivoMovimiento);
        etObservaciones = findViewById(R.id.etObservacionesMovimiento);
        btnConfirmar = findViewById(R.id.btnConfirmarMovimiento);
        btnCancelar = findViewById(R.id.btnCancelarMovimientoForm);

        db = AppDatabase.getDatabase(getApplicationContext());
        movimientoRepository = new MovimientoRepository(getApplicationContext());
        sessionManager = new SessionManager(this);

        if ("ENTRADA".equals(tipoMovimiento)) {
            tvTitulo.setText("Registrar Entrada de Producto");
            btnConfirmar.setBackgroundColor(android.graphics.Color.parseColor("#0284C7"));
        } else {
            tvTitulo.setText("Registrar Salida de Producto");
            btnConfirmar.setBackgroundColor(android.graphics.Color.parseColor("#B42318"));
        }

        cargarProductos();

        spProductos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < listaProductos.size()) {
                    productoSeleccionado = listaProductos.get(position);
                    tvStockActual.setText("Stock actual disponible: " + productoSeleccionado.stock_actual + " " + (productoSeleccionado.unidad_medida != null ? productoSeleccionado.unidad_medida : "unidades"));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                productoSeleccionado = null;
                tvStockActual.setText("Stock actual disponible: 0");
            }
        });

        btnConfirmar.setOnClickListener(v -> confirmarOperacion());
        btnCancelar.setOnClickListener(v -> finish());
    }

    private void cargarProductos() {
        executor.execute(() -> {
            List<Producto> productosActivos = db.productoDao().obtenerProductosActivos();
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;
                listaProductos.clear();
                listaProductos.addAll(productosActivos);

                List<String> nombresNombresSku = new ArrayList<>();
                for (Producto p : listaProductos) {
                    nombresNombresSku.add(p.nombre + " (SKU: " + p.codigo_sku + ")");
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        nombresNombresSku
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spProductos.setAdapter(adapter);

                if (!listaProductos.isEmpty()) {
                    productoSeleccionado = listaProductos.get(0);
                    tvStockActual.setText("Stock actual disponible: " + productoSeleccionado.stock_actual + " " + (productoSeleccionado.unidad_medida != null ? productoSeleccionado.unidad_medida : "unidades"));
                }
            });
        });
    }

    private void confirmarOperacion() {
        if (productoSeleccionado == null) {
            Toast.makeText(this, "Seleccione un producto válido", Toast.LENGTH_SHORT).show();
            return;
        }

        String cantidadStr = etCantidad.getText().toString().trim();
        if (cantidadStr.isEmpty()) {
            etCantidad.setError("Ingrese una cantidad");
            return;
        }

        int cantidad;
        try {
            cantidad = Integer.parseInt(cantidadStr);
        } catch (NumberFormatException e) {
            etCantidad.setError("Cantidad inválida");
            return;
        }

        if (cantidad <= 0) {
            etCantidad.setError("La cantidad debe ser mayor a cero");
            return;
        }

        int idUsuario = sessionManager.obtenerIdUsuario();
        String motivo = etMotivo.getText().toString().trim();
        String observaciones = etObservaciones.getText().toString().trim();

        btnConfirmar.setEnabled(false);

        MovimientoRepository.MovimientoCallback callback = new MovimientoRepository.MovimientoCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(FormularioMovimientoActivity.this, "Operación registrada con éxito", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onError(String mensaje) {
                runOnUiThread(() -> {
                    btnConfirmar.setEnabled(true);
                    Toast.makeText(FormularioMovimientoActivity.this, mensaje, Toast.LENGTH_LONG).show();
                });
            }
        };

        if ("ENTRADA".equals(tipoMovimiento)) {
            movimientoRepository.registrarEntrada(
                    productoSeleccionado.id_producto,
                    idUsuario,
                    cantidad,
                    motivo,
                    observaciones,
                    callback
            );
        } else {
            movimientoRepository.registrarSalida(
                    productoSeleccionado.id_producto,
                    idUsuario,
                    cantidad,
                    motivo,
                    observaciones,
                    callback
            );
        }
    }
}
