package com.example.sistemadebodega;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sistemadebodega.R;
import com.example.sistemadebodega.data.AppDatabase;
import com.example.sistemadebodega.model.Rol;
import com.example.sistemadebodega.model.Usuario;
import com.example.sistemadebodega.utils.PasswordUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class RegistrarUsuarioActivity extends AppCompatActivity {

    private EditText etNombreCompleto, etUsername, etPassword;
    private Spinner spinnerRoles;
    private Button btnGuardar;
    private AppDatabase db;
    private List<Rol> listaRoles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_usuario);

        etNombreCompleto = findViewById(R.id.etNombreCompletoReg);
        etUsername = findViewById(R.id.etUsernameReg);
        etPassword = findViewById(R.id.etPasswordReg);
        spinnerRoles = findViewById(R.id.spinnerRoles);
        btnGuardar = findViewById(R.id.btnGuardarUsuario);

        db = AppDatabase.getDatabase(getApplicationContext());

        cargarRolesSpinner();

        btnGuardar.setOnClickListener(v -> {
            String nombreCompleto = etNombreCompleto.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (nombreCompleto.isEmpty() || username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, getString(R.string.msg_fill_fields), Toast.LENGTH_SHORT).show();
                return;
            }

            if (spinnerRoles.getSelectedItem() == null) {
                Toast.makeText(this, "Debe seleccionar un rol", Toast.LENGTH_SHORT).show();
                return;
            }

            int indexSeleccionado = spinnerRoles.getSelectedItemPosition();
            int idRolSeleccionado = listaRoles.get(indexSeleccionado).id_rol;

            // Registrar en segundo plano
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    Usuario nuevoUsuario = new Usuario();
                    nuevoUsuario.nombre_completo = nombreCompleto;
                    nuevoUsuario.username = username;
                    nuevoUsuario.password_hash = PasswordUtils.hashPassword(password);
                    nuevoUsuario.id_rol = idRolSeleccionado;
                    nuevoUsuario.activo = 1; // Asignar activo = 1 por defecto

                    db.usuarioDao().insertarUsuario(nuevoUsuario);

                    runOnUiThread(() -> {
                        Toast.makeText(this, getString(R.string.msg_usuario_registrado), Toast.LENGTH_SHORT).show();
                        finish(); // Regresar a la lista de usuarios
                    });
                } catch (Exception e) {
                    // Interceptar duplicidad de username (Restricción UNIQUE / SQLiteConstraintException)
                    runOnUiThread(() -> {
                        Toast.makeText(this, getString(R.string.error_username_duplicado), Toast.LENGTH_LONG).show();
                        etUsername.requestFocus();
                    });
                }
            });
        });
    }

    private void cargarRolesSpinner() {
        Executors.newSingleThreadExecutor().execute(() -> {
            listaRoles = db.rolDao().obtenerTodosRoles();
            List<String> nombresRoles = new ArrayList<>();
            for (Rol rol : listaRoles) {
                nombresRoles.add(rol.nombre_rol);
            }

            runOnUiThread(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, nombresRoles);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerRoles.setAdapter(adapter);
            });
        });
    }
}