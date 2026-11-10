package com.example.sistemadebodega;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sistemadebodega.data.AppDatabase;
import com.example.sistemadebodega.model.Producto;
import com.example.sistemadebodega.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InformeStockActivity extends AppCompatActivity {

    private EditText etBuscar;
    private Spinner spFiltroEstado;
    private TextView tvResumen;
    private RecyclerView recyclerView;

    private AppDatabase db;
    private SessionManager sessionManager;
    private InformeStockAdapter adapter;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final List<Producto> listaProductosCompleta = new ArrayList<>();
    private final List<Producto> listaProductosFiltrada = new ArrayList<>();

    private final String[] estadosFiltro = {
            "Todos los estados",
            "Disponible (Normal)",
            "Stock Crítico",
            "Sin Stock (Agotado)"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_informe_stock);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        db = AppDatabase.getDatabase(getApplicationContext());

        etBuscar = findViewById(R.id.etBuscarInforme);
        spFiltroEstado = findViewById(R.id.spFiltroEstadoInforme);
        tvResumen = findViewById(R.id.tvResumenInforme);
        recyclerView = findViewById(R.id.rvInformeStock);
        Button btnVolver = findViewById(R.id.btnVolverInforme);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InformeStockAdapter(listaProductosFiltrada);
        recyclerView.setAdapter(adapter);

        ArrayAdapter<String> filtroAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                estadosFiltro
        );
        filtroAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFiltroEstado.setAdapter(filtroAdapter);

        btnVolver.setOnClickListener(v -> finish());

        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                aplicarFiltros();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        spFiltroEstado.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                aplicarFiltros();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarDatos();
    }

    private void cargarDatos() {
        executor.execute(() -> {
            List<Producto> productos = db.productoDao().obtenerProductosActivos();
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;
                listaProductosCompleta.clear();
                listaProductosCompleta.addAll(productos);
                aplicarFiltros();
            });
        });
    }

    private void aplicarFiltros() {
        String textoBusqueda = etBuscar.getText().toString().trim().toLowerCase();
        int filtroEstado = spFiltroEstado.getSelectedItemPosition();

        listaProductosFiltrada.clear();

        for (Producto p : listaProductosCompleta) {
            // Filtro de búsqueda
            boolean coincideTexto = textoBusqueda.isEmpty() ||
                    (p.nombre != null && p.nombre.toLowerCase().contains(textoBusqueda)) ||
                    (p.codigo_sku != null && p.codigo_sku.toLowerCase().contains(textoBusqueda));

            if (!coincideTexto) continue;

            // Filtro de estado
            // 0: Todos, 1: Disponible, 2: Stock Crítico, 3: Sin Stock
            boolean coincideEstado = true;
            if (filtroEstado == 1) {
                coincideEstado = (p.stock_actual > p.stock_minimo);
            } else if (filtroEstado == 2) {
                coincideEstado = (p.stock_actual > 0 && p.stock_actual <= p.stock_minimo);
            } else if (filtroEstado == 3) {
                coincideEstado = (p.stock_actual == 0);
            }

            if (coincideEstado) {
                listaProductosFiltrada.add(p);
            }
        }

        adapter.notifyDataSetChanged();
        tvResumen.setText("Mostrando " + listaProductosFiltrada.size() + " de " + listaProductosCompleta.size() + " productos");
    }
}
