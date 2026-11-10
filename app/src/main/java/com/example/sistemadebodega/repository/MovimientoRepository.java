package com.example.sistemadebodega.repository;

import android.content.Context;

import com.example.sistemadebodega.data.AppDatabase;
import com.example.sistemadebodega.model.MovimientoInventario;
import com.example.sistemadebodega.model.Producto;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MovimientoRepository {

    private final AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public interface MovimientoCallback {
        void onSuccess();
        void onError(String mensaje);
    }

    public MovimientoRepository(Context context) {
        db = AppDatabase.getDatabase(context);
    }

    public void registrarEntrada(
            int idProducto,
            int idUsuario,
            int cantidad,
            String motivo,
            String observaciones,
            MovimientoCallback callback
    ) {
        executor.execute(() -> {
            try {
                if (cantidad <= 0) {
                    callback.onError("La cantidad debe ser mayor a cero.");
                    return;
                }

                Producto producto = db.productoDao().obtenerPorId(idProducto);
                if (producto == null || producto.activo == 0) {
                    callback.onError("El producto no existe o está inactivo.");
                    return;
                }

                // Transacción Room
                db.runInTransaction(() -> {
                    int filasActualizadas = db.productoDao().incrementarStock(idProducto, cantidad);
                    if (filasActualizadas == 0) {
                        throw new RuntimeException("No se pudo actualizar el stock del producto.");
                    }

                    MovimientoInventario movimiento = new MovimientoInventario();
                    movimiento.id_producto = idProducto;
                    movimiento.id_usuario = idUsuario;
                    movimiento.tipo_movimiento = "ENTRADA";
                    movimiento.cantidad = cantidad;
                    movimiento.motivo = motivo != null && !motivo.trim().isEmpty() ? motivo : "Entrada a bodega";
                    movimiento.observaciones = observaciones;
                    movimiento.fecha_hora = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

                    db.movimientoDao().insertarMovimiento(movimiento);
                });

                callback.onSuccess();

            } catch (Exception e) {
                callback.onError("Error al registrar entrada: " + e.getMessage());
            }
        });
    }

    public void registrarSalida(
            int idProducto,
            int idUsuario,
            int cantidad,
            String motivo,
            String observaciones,
            MovimientoCallback callback
    ) {
        executor.execute(() -> {
            try {
                if (cantidad <= 0) {
                    callback.onError("La cantidad debe ser mayor a cero.");
                    return;
                }

                Producto producto = db.productoDao().obtenerPorId(idProducto);
                if (producto == null || producto.activo == 0) {
                    callback.onError("El producto no existe o está inactivo.");
                    return;
                }

                if (producto.stock_actual < cantidad) {
                    callback.onError("Stock insuficiente. Stock actual: " + producto.stock_actual);
                    return;
                }

                // Transacción Room
                db.runInTransaction(() -> {
                    int filasActualizadas = db.productoDao().decrementarStock(idProducto, cantidad);
                    if (filasActualizadas == 0) {
                        throw new RuntimeException("Stock insuficiente o error al actualizar.");
                    }

                    MovimientoInventario movimiento = new MovimientoInventario();
                    movimiento.id_producto = idProducto;
                    movimiento.id_usuario = idUsuario;
                    movimiento.tipo_movimiento = "SALIDA";
                    movimiento.cantidad = cantidad;
                    movimiento.motivo = motivo != null && !motivo.trim().isEmpty() ? motivo : "Salida de bodega";
                    movimiento.observaciones = observaciones;
                    movimiento.fecha_hora = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

                    db.movimientoDao().insertarMovimiento(movimiento);
                });

                callback.onSuccess();

            } catch (Exception e) {
                callback.onError("Error al registrar salida: " + e.getMessage());
            }
        });
    }
}
