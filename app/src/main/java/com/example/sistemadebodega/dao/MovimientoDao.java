package com.example.sistemadebodega.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.sistemadebodega.model.MovimientoInventario;

import java.util.List;

@Dao
public interface MovimientoDao {

    @Insert
    long insertarMovimiento(MovimientoInventario movimiento);

    @Query("SELECT * FROM movimiento_inventario ORDER BY id_movimiento DESC")
    List<MovimientoInventario> obtenerTodosLosMovimientos();

    @Query("SELECT * FROM movimiento_inventario WHERE id_producto = :idProducto ORDER BY id_movimiento DESC")
    List<MovimientoInventario> obtenerMovimientosPorProducto(int idProducto);

    @Query("SELECT m.*, p.nombre AS nombre_producto, p.codigo_sku AS codigo_sku " +
            "FROM movimiento_inventario m " +
            "INNER JOIN producto p ON m.id_producto = p.id_producto " +
            "ORDER BY m.id_movimiento DESC")
    List<MovimientoConProducto> obtenerMovimientosConDetalle();

    class MovimientoConProducto {
        public int id_movimiento;
        public int id_producto;
        public int id_usuario;
        public String tipo_movimiento;
        public int cantidad;
        public String motivo;
        public String observaciones;
        public String fecha_hora;
        public String nombre_producto;
        public String codigo_sku;
    }
}
