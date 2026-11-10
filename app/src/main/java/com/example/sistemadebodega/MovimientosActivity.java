package com.example.sistemadebodega;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sistemadebodega.dao.MovimientoDao;
import com.example.sistemadebodega.data.AppDatabase;
import com.example.sistemadebodega.utils.SessionManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MovimientosActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MovimientoAdapter adapter;
    private AppDatabase db;
    private SessionManager sessionManager;

    private Spinner spFiltroProducto;
    private Spinner spFiltroUsuario;
    private TextView tvCantidadMovimientos;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private final List<MovimientoDao.MovimientoConProducto> listaMovimientos =
            new ArrayList<>();

    private List<MovimientoDao.MovimientoConProducto> todosLosMovimientos =
            new ArrayList<>();

    private final List<Integer> idsProductos = new ArrayList<>();
    private final List<Integer> idsUsuarios = new ArrayList<>();

    private int productoSeleccionado = -1;
    private int usuarioSeleccionado = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movimientos);

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );
            startActivity(intent);
            finish();
            return;
        }

        db = AppDatabase.getDatabase(getApplicationContext());

        recyclerView = findViewById(R.id.recyclerMovimientos);
        spFiltroProducto = findViewById(R.id.spFiltroProductoMovimiento);
        spFiltroUsuario = findViewById(R.id.spFiltroUsuarioMovimiento);
        tvCantidadMovimientos = findViewById(R.id.tvCantidadMovimientos);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        adapter = new MovimientoAdapter(listaMovimientos);
        recyclerView.setAdapter(adapter);

        Button btnEntrada = findViewById(R.id.btnRegistrarEntradaMenu);
        Button btnSalida = findViewById(R.id.btnRegistrarSalidaMenu);
        Button btnVolver = findViewById(R.id.btnVolverDashboard);

        btnEntrada.setOnClickListener(v -> {
            Intent intent = new Intent(
                    this,
                    FormularioMovimientoActivity.class
            );
            intent.putExtra("tipo_movimiento", "ENTRADA");
            startActivity(intent);
        });

        btnSalida.setOnClickListener(v -> {
            Intent intent = new Intent(
                    this,
                    FormularioMovimientoActivity.class
            );
            intent.putExtra("tipo_movimiento", "SALIDA");
            startActivity(intent);
        });

        btnVolver.setOnClickListener(v -> finish());

        configurarFiltros();
    }

    private void configurarFiltros() {

        android.widget.AdapterView.OnItemSelectedListener listener =
                new android.widget.AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            android.view.View view,
                            int position,
                            long id
                    ) {

                        if (parent.getId() == R.id.spFiltroProductoMovimiento) {
                            if (position < idsProductos.size()) {
                                productoSeleccionado = idsProductos.get(position);
                            }
                        }

                        if (parent.getId() == R.id.spFiltroUsuarioMovimiento) {
                            if (position < idsUsuarios.size()) {
                                usuarioSeleccionado = idsUsuarios.get(position);
                            }
                        }

                        aplicarFiltros();
                    }

                    @Override
                    public void onNothingSelected(
                            android.widget.AdapterView<?> parent
                    ) {
                    }
                };

        spFiltroProducto.setOnItemSelectedListener(listener);
        spFiltroUsuario.setOnItemSelectedListener(listener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarMovimientos();
    }

    private void cargarMovimientos() {

        executor.execute(() -> {

            try {

                List<MovimientoDao.MovimientoConProducto> resultado =
                        db.movimientoDao().obtenerMovimientosConDetalle();

                runOnUiThread(() -> {

                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    todosLosMovimientos = resultado;

                    cargarOpcionesFiltros();
                    aplicarFiltros();
                });

            } catch (Exception e) {

                runOnUiThread(() -> {
                    if (!isFinishing() && !isDestroyed()) {
                        Toast.makeText(
                                this,
                                "Error al cargar historial de movimientos",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
            }
        });
    }

    private void cargarOpcionesFiltros() {

        Map<Integer, String> productos = new LinkedHashMap<>();
        Map<Integer, String> usuarios = new LinkedHashMap<>();

        for (MovimientoDao.MovimientoConProducto movimiento
                : todosLosMovimientos) {

            String nombreProducto = movimiento.nombre_producto == null
                    ? "Producto #" + movimiento.id_producto
                    : movimiento.nombre_producto;

            String nombreUsuario = movimiento.nombre_usuario == null
                    ? "Usuario #" + movimiento.id_usuario
                    : movimiento.nombre_usuario;

            productos.put(movimiento.id_producto, nombreProducto);
            usuarios.put(movimiento.id_usuario, nombreUsuario);
        }

        List<String> opcionesProductos = new ArrayList<>();
        List<String> opcionesUsuarios = new ArrayList<>();

        idsProductos.clear();
        idsUsuarios.clear();

        opcionesProductos.add("Todos los productos");
        idsProductos.add(-1);

        opcionesUsuarios.add("Todos los usuarios");
        idsUsuarios.add(-1);

        for (Map.Entry<Integer, String> entry : productos.entrySet()) {
            idsProductos.add(entry.getKey());
            opcionesProductos.add(entry.getValue());
        }

        for (Map.Entry<Integer, String> entry : usuarios.entrySet()) {
            idsUsuarios.add(entry.getKey());
            opcionesUsuarios.add(entry.getValue());
        }

        ArrayAdapter<String> adapterProductos = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                opcionesProductos
        );

        adapterProductos.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        ArrayAdapter<String> adapterUsuarios = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                opcionesUsuarios
        );

        adapterUsuarios.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spFiltroProducto.setAdapter(adapterProductos);
        spFiltroUsuario.setAdapter(adapterUsuarios);

        int posicionProducto = idsProductos.indexOf(productoSeleccionado);
        int posicionUsuario = idsUsuarios.indexOf(usuarioSeleccionado);

        spFiltroProducto.setSelection(
                Math.max(0, posicionProducto)
        );

        spFiltroUsuario.setSelection(
                Math.max(0, posicionUsuario)
        );

        if (posicionProducto < 0) {
            productoSeleccionado = -1;
        }

        if (posicionUsuario < 0) {
            usuarioSeleccionado = -1;
        }
    }

    private void aplicarFiltros() {

        listaMovimientos.clear();

        for (MovimientoDao.MovimientoConProducto movimiento
                : todosLosMovimientos) {

            if (productoSeleccionado != -1
                    && movimiento.id_producto != productoSeleccionado) {
                continue;
            }

            if (usuarioSeleccionado != -1
                    && movimiento.id_usuario != usuarioSeleccionado) {
                continue;
            }

            listaMovimientos.add(movimiento);
        }

        adapter.notifyDataSetChanged();

        tvCantidadMovimientos.setText(
                listaMovimientos.isEmpty()
                        ? "No se encontraron movimientos"
                        : listaMovimientos.size()
                          + (listaMovimientos.size() == 1
                             ? " movimiento encontrado"
                             : " movimientos encontrados")
        );
    }

    @Override
    protected void onDestroy() {
        executor.shutdown();
        super.onDestroy();
    }
}
