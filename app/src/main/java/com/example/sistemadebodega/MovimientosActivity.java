package com.example.sistemadebodega;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sistemadebodega.dao.MovimientoDao;
import com.example.sistemadebodega.data.AppDatabase;
import com.example.sistemadebodega.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MovimientosActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MovimientoAdapter adapter;
    private AppDatabase db;
    private SessionManager sessionManager;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final List<MovimientoDao.MovimientoConProducto> listaMovimientos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movimientos);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        db = AppDatabase.getDatabase(getApplicationContext());

        recyclerView = findViewById(R.id.recyclerMovimientos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MovimientoAdapter(listaMovimientos);
        recyclerView.setAdapter(adapter);

        Button btnEntrada = findViewById(R.id.btnRegistrarEntradaMenu);
        Button btnSalida = findViewById(R.id.btnRegistrarSalidaMenu);
        Button btnVolver = findViewById(R.id.btnVolverDashboard);

        btnEntrada.setOnClickListener(v -> {
            Intent intent = new Intent(this, FormularioMovimientoActivity.class);
            intent.putExtra("tipo_movimiento", "ENTRADA");
            startActivity(intent);
        });

        btnSalida.setOnClickListener(v -> {
            Intent intent = new Intent(this, FormularioMovimientoActivity.class);
            intent.putExtra("tipo_movimiento", "SALIDA");
            startActivity(intent);
        });

        btnVolver.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarMovimientos();
    }

    private void cargarMovimientos() {
        executor.execute(() -> {
            List<MovimientoDao.MovimientoConProducto> resultado =
                    db.movimientoDao().obtenerMovimientosConDetalle();

            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;
                listaMovimientos.clear();
                listaMovimientos.addAll(resultado);
                adapter.notifyDataSetChanged();
            });
        });
    }
}
