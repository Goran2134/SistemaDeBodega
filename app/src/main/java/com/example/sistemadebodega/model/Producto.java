
package com.example.sistemadebodega.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "producto",
        indices = {
                @Index(value = {"codigo_sku"}, unique = true),
                @Index(value = {"id_categoria"})
        },
        foreignKeys = @ForeignKey(
                entity = Categoria.class,
                parentColumns = "id_categoria",
                childColumns = "id_categoria",
                onDelete = ForeignKey.RESTRICT
        )
)
public class Producto {

    @PrimaryKey(autoGenerate = true)
    public int id_producto;

    // Código único del producto
    public String codigo_sku;

    // Nombre del producto
    public String nombre;

    // Descripción
    public String descripcion;

    // Categoría asociada
    public int id_categoria;

    // Unidad: unidad, kg, litro, etc.
    public String unidad_medida;

    // Stock disponible
    public int stock_actual;

    // Stock mínimo de alerta
    public int stock_minimo;

    // 1 = Activo, 0 = Inactivo
    public int activo;

    public Producto() {
        this.activo = 1;
        this.stock_actual = 0;
        this.stock_minimo = 5;
    }
}
