package com.example.sistemadebodega.model;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "categoria", indices = {@Index(value = {"nombre_categoria"}, unique = true)})
public class Categoria {
    @PrimaryKey(autoGenerate = true)
    public int id_categoria;

    public String nombre_categoria;
    public String descripcion;
    public int activo; // 1 = Activo, 0 = Inactivo
}