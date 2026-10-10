package com.example.sistemadebodega.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.sistemadebodega.model.Rol;
import java.util.List;

@Dao
public interface RolDao {
    @Insert
    void insertarRol(Rol rol);

    @Query("SELECT * FROM rol")
    List<Rol> obtenerTodosRoles();
}