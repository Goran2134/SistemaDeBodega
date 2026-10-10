package com.example.sistemadebodega.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.sistemadebodega.model.Usuario;
import java.util.List;

@Dao
public interface UsuarioDao {
    @Query("SELECT * FROM usuario WHERE username = :username LIMIT 1")
    Usuario obtenerPorUsername(String username);

    @Insert
    void insertarUsuario(Usuario usuario);

    @Query("SELECT COUNT(*) FROM usuario WHERE username = :username")
    int contarPorUsername(String username);

    // Consulta para desplegar listado en RecyclerView con nombre de rol
    @Query("SELECT u.*, r.nombre_rol FROM usuario u INNER JOIN rol r ON u.id_rol = r.id_rol")
    List<UsuarioConRol> obtenerUsuariosConRoles();

    // Clase auxiliar para mapear el resultado de la consulta con el nombre del rol
    class UsuarioConRol {
        public int id_usuario;
        public String username;
        public String password_hash;
        public String nombre_completo;
        public int id_rol;
        public int activo;
        public String nombre_rol;
    }
}