package com.example.sistemadebodega.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.sistemadebodega.model.Categoria;
import java.util.List;

@Dao
public interface CategoriaDao {
    @Query("SELECT * FROM categoria WHERE activo = 1 ORDER BY nombre_categoria ASC")
    List<Categoria> obtenerCategoriasActivas();

    @Insert
    long insertarCategoria(Categoria categoria);

    @Query("SELECT COUNT(*) FROM categoria WHERE nombre_categoria = :nombre")
    int contarPorNombre(String nombre);
}