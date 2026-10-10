package com.example.sistemadebodega.data;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.example.sistemadebodega.dao.CategoriaDao;
import com.example.sistemadebodega.dao.RolDao;
import com.example.sistemadebodega.dao.UsuarioDao;
import com.example.sistemadebodega.model.Categoria;
import com.example.sistemadebodega.model.Rol;
import com.example.sistemadebodega.model.Usuario;
import com.example.sistemadebodega.utils.PasswordUtils;
import java.util.concurrent.Executors;

@Database(entities = {Rol.class, Usuario.class, Categoria.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract RolDao rolDao();
    public abstract UsuarioDao usuarioDao();
    public abstract CategoriaDao categoriaDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "inventario_db"
                            )
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            Executors.newSingleThreadExecutor().execute(() -> {
                RolDao rolDao = INSTANCE.rolDao();
                UsuarioDao usuarioDao = INSTANCE.usuarioDao();

                // 1. Insertar roles iniciales
                Rol rolAdmin = new Rol();
                rolAdmin.nombre_rol = "Administrador";
                rolDao.insertarRol(rolAdmin);

                Rol rolOperador = new Rol();
                rolOperador.nombre_rol = "Operador";
                rolDao.insertarRol(rolOperador);

                // 2. Insertar Administrador por defecto
                // Usando hash seguro PBKDF2
                Usuario admin = new Usuario();
                admin.username = "admin";
                admin.password_hash = PasswordUtils.hashPassword("Admin123*");
                admin.nombre_completo = "Administrador General";
                admin.id_rol = 1;
                admin.activo = 1;

                usuarioDao.insertarUsuario(admin);
            });
        }
    };
}