package com.example.sistemadebodega.model;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "rol", indices = {@Index(value = {"nombre_rol"}, unique = true)})
public class Rol {
    @PrimaryKey(autoGenerate = true)
    public int id_rol;

    public String nombre_rol;
}