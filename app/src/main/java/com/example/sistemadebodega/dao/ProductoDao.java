
package com.example.sistemadebodega.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.sistemadebodega.model.Producto;

import java.util.List;

@Dao
public interface ProductoDao {

    // RF-03: Registrar productos
    @Insert
    long insertarProducto(Producto producto);

    // RF-04: Consultar productos activos
    @Query("SELECT * FROM producto " +
            "WHERE activo = 1 " +
            "ORDER BY nombre ASC")
    List<Producto> obtenerProductosActivos();

    // Consultar todos, incluidos inactivos
    @Query("SELECT * FROM producto " +
            "ORDER BY nombre ASC")
    List<Producto> obtenerTodosLosProductos();

    // RF-05: Buscar por nombre o SKU
    @Query("SELECT * FROM producto " +
            "WHERE activo = 1 AND " +
            "(nombre LIKE '%' || :texto || '%' " +
            "OR codigo_sku LIKE '%' || :texto || '%') " +
            "ORDER BY nombre ASC")
    List<Producto> buscarProductos(String texto);

    // Consultar producto por ID
    @Query("SELECT * FROM producto " +
            "WHERE id_producto = :id LIMIT 1")
    Producto obtenerPorId(int id);

    // Consultar producto por SKU
    @Query("SELECT * FROM producto " +
            "WHERE codigo_sku = :sku LIMIT 1")
    Producto obtenerPorSku(String sku);

    // Incrementar stock (Entrada)
    @Query("UPDATE producto SET stock_actual = stock_actual + :cantidad " +
            "WHERE id_producto = :id")
    int incrementarStock(int id, int cantidad);

    // Decrementar stock (Salida)
    @Query("UPDATE producto SET stock_actual = stock_actual - :cantidad " +
            "WHERE id_producto = :id AND stock_actual >= :cantidad")
    int decrementarStock(int id, int cantidad);

    // RF-07: Modificar producto
    @Update
    int actualizarProducto(Producto producto);

    // RF-08: Deshabilitar producto
    @Query("UPDATE producto SET activo = 0 " +
            "WHERE id_producto = :id")
    int deshabilitarProducto(int id);

    // Reactivar producto
    @Query("UPDATE producto SET activo = 1 " +
            "WHERE id_producto = :id")
    int habilitarProducto(int id);

    // Verificar SKU duplicado
    @Query("SELECT COUNT(*) FROM producto " +
            "WHERE codigo_sku = :sku")
    int contarPorSku(String sku);

    // Verificar SKU al editar, excluyendo el actual
    @Query("SELECT COUNT(*) FROM producto " +
            "WHERE codigo_sku = :sku " +
            "AND id_producto != :id")
    int contarSkuDuplicado(String sku, int id);

    // Contar productos activos
    @Query("SELECT COUNT(*) FROM producto " +
            "WHERE activo = 1")
    int contarProductosActivos();
}
