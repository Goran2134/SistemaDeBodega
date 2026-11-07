
package com.example.sistemadebodega.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.sistemadebodega.dao.CategoriaDao;
import com.example.sistemadebodega.dao.ProductoDao;
import com.example.sistemadebodega.dao.RolDao;
import com.example.sistemadebodega.dao.UsuarioDao;

import com.example.sistemadebodega.model.Categoria;
import com.example.sistemadebodega.model.Producto;
import com.example.sistemadebodega.model.Rol;
import com.example.sistemadebodega.model.Usuario;

import com.example.sistemadebodega.utils.PasswordUtils;

import java.util.concurrent.Executors;

@Database(
        entities = {
                Rol.class,
                Usuario.class,
                Categoria.class,
                Producto.class
        },
        version = 2,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract RolDao rolDao();
    public abstract UsuarioDao usuarioDao();
    public abstract CategoriaDao categoriaDao();
    public abstract ProductoDao productoDao();

    private static volatile AppDatabase INSTANCE;

    // Migración de versión 1 a versión 2
    private static final Migration MIGRATION_1_2 =
            new Migration(1, 2) {

                @Override
                public void migrate(
                        @NonNull SupportSQLiteDatabase db
                ) {

                    // Crear tabla de productos
                    db.execSQL(
                            "CREATE TABLE IF NOT EXISTS `producto` (" +
                                    "`id_producto` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                                    "`codigo_sku` TEXT, " +
                                    "`nombre` TEXT, " +
                                    "`descripcion` TEXT, " +
                                    "`id_categoria` INTEGER NOT NULL, " +
                                    "`unidad_medida` TEXT, " +
                                    "`stock_actual` INTEGER NOT NULL, " +
                                    "`activo` INTEGER NOT NULL, " +
                                    "FOREIGN KEY(`id_categoria`) " +
                                    "REFERENCES `categoria`(`id_categoria`) " +
                                    "ON UPDATE NO ACTION ON DELETE RESTRICT)"
                    );

                    // SKU único
                    db.execSQL(
                            "CREATE UNIQUE INDEX IF NOT EXISTS " +
                                    "`index_producto_codigo_sku` " +
                                    "ON `producto` (`codigo_sku`)"
                    );

                    // Índice para categorías
                    db.execSQL(
                            "CREATE INDEX IF NOT EXISTS " +
                                    "`index_producto_id_categoria` " +
                                    "ON `producto` (`id_categoria`)"
                    );
                }
            };

    public static AppDatabase getDatabase(final Context context) {

        if (INSTANCE == null) {

            synchronized (AppDatabase.class) {

                if (INSTANCE == null) {

                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "inventario_db"
                            )
                            .addMigrations(MIGRATION_1_2)
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }

        return INSTANCE;
    }

    private static final RoomDatabase.Callback sRoomDatabaseCallback =
            new RoomDatabase.Callback() {

                @Override
                public void onCreate(
                        @NonNull SupportSQLiteDatabase db
                ) {

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

                        // 2. Insertar administrador por defecto
                        Usuario admin = new Usuario();

                        admin.username = "admin";
                        admin.password_hash =
                                PasswordUtils.hashPassword("Admin123*");
                        admin.nombre_completo = "Administrador General";
                        admin.id_rol = 1;
                        admin.activo = 1;

                        usuarioDao.insertarUsuario(admin);
                    });
                }
            };
}
