package com.example.sistemadebodega.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "usuario",
        indices = {@Index(value = {"username"}, unique = true)},
        foreignKeys = @ForeignKey(
                entity = Rol.class,
                parentColumns = "id_rol",
                childColumns = "id_rol",
                onDelete = ForeignKey.CASCADE
        )
)
public class Usuario {
    @PrimaryKey(autoGenerate = true)
    public int id_usuario;

    public String username;
    public String password_hash;
    public String nombre_completo;
    public int id_rol;
    public int activo; // 1 = Activo, 0 = Inactivo
}