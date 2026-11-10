package com.example.sistemadebodega.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "movimiento_inventario",
        indices = {
                @Index(value = {"id_producto"}),
                @Index(value = {"id_usuario"})
        },
        foreignKeys = {
                @ForeignKey(
                        entity = Producto.class,
                        parentColumns = "id_producto",
                        childColumns = "id_producto",
                        onDelete = ForeignKey.RESTRICT
                ),
                @ForeignKey(
                        entity = Usuario.class,
                        parentColumns = "id_usuario",
                        childColumns = "id_usuario",
                        onDelete = ForeignKey.RESTRICT
                )
        }
)
public class MovimientoInventario {

    @PrimaryKey(autoGenerate = true)
    public int id_movimiento;

    // Producto afectado
    public int id_producto;

    // Usuario que realizó el movimiento
    public int id_usuario;

    // "ENTRADA" o "SALIDA"
    public String tipo_movimiento;

    // Cantidad movida
    public int cantidad;

    // Motivo del movimiento (ej: Compra, Venta, Ajuste, etc.)
    public String motivo;

    // Información adicional (proveedor, documento, área, destinatario, etc.)
    public String observaciones;

    // Fecha y hora del movimiento (formato ISO o texto legible)
    public String fecha_hora;

    public MovimientoInventario() {
    }
}
