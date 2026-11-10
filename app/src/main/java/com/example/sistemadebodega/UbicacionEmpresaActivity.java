package com.example.sistemadebodega;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Locale;

public class UbicacionEmpresaActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private Marker marcadorEmpresa;
    private Marker marcadorUsuario;

    private LatLng ubicacionSeleccionada;
    private LatLng ubicacionGuardada;
    private LatLng ubicacionUsuario;

    private TextView tvCoordenadas;
    private TextView tvMiUbicacion;

    private Button btnGuardarUbicacion;
    private Button btnMiUbicacion;
    private Button btnComoLlegar;

    private SharedPreferences preferencias;
    private FusedLocationProviderClient clienteUbicacion;

    private static final String PREF_NAME = "UbicacionEmpresa";
    private static final String KEY_LATITUD = "latitud";
    private static final String KEY_LONGITUD = "longitud";
    private static final String KEY_CONFIGURADA = "configurada";

    private ActivityResultLauncher<String[]> permisoUbicacionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ubicacion_empresa);

        tvCoordenadas = findViewById(R.id.tvCoordenadas);
        tvMiUbicacion = findViewById(R.id.tvMiUbicacion);

        btnGuardarUbicacion = findViewById(R.id.btnGuardarUbicacion);
        btnMiUbicacion = findViewById(R.id.btnMiUbicacion);
        btnComoLlegar = findViewById(R.id.btnComoLlegar);

        preferencias = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        clienteUbicacion = LocationServices
                .getFusedLocationProviderClient(this);

        btnGuardarUbicacion.setEnabled(false);
        btnComoLlegar.setEnabled(
                preferencias.getBoolean(KEY_CONFIGURADA, false)
        );

        permisoUbicacionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                permisos -> {
                    boolean precisa = Boolean.TRUE.equals(
                            permisos.get(Manifest.permission.ACCESS_FINE_LOCATION)
                    );

                    boolean aproximada = Boolean.TRUE.equals(
                            permisos.get(Manifest.permission.ACCESS_COARSE_LOCATION)
                    );

                    if (precisa || aproximada) {
                        obtenerUbicacionActual();
                    } else {
                        Toast.makeText(
                                this,
                                "Se necesita permiso para consultar tu ubicación",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.mapEmpresa);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnGuardarUbicacion.setOnClickListener(v -> guardarUbicacion());

        btnMiUbicacion.setOnClickListener(v -> verificarPermisosUbicacion());

        btnComoLlegar.setOnClickListener(v -> mostrarRuta());
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;

        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);

        // Punto inicial de referencia, no ubicación empresarial.
        LatLng ubicacionInicial = new LatLng(-30.6016, -71.2000);

        if (preferencias.getBoolean(KEY_CONFIGURADA, false)) {
            double latitud = Double.longBitsToDouble(
                    preferencias.getLong(KEY_LATITUD, 0)
            );

            double longitud = Double.longBitsToDouble(
                    preferencias.getLong(KEY_LONGITUD, 0)
            );

            ubicacionGuardada = new LatLng(latitud, longitud);
            ubicacionSeleccionada = ubicacionGuardada;
            ubicacionInicial = ubicacionGuardada;

            actualizarMarcadorEmpresa(ubicacionGuardada);

            tvCoordenadas.setText(
                    String.format(
                            Locale.US,
                            "Ubicación empresarial guardada\nLatitud: %.6f\nLongitud: %.6f",
                            latitud,
                            longitud
                    )
            );

        } else {
            tvCoordenadas.setText(
                    "Toque un punto del mapa para seleccionar la ubicación de la empresa."
            );
        }

        googleMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(ubicacionInicial, 14f)
        );

        googleMap.setOnMapClickListener(ubicacion -> {
            ubicacionSeleccionada = ubicacion;

            actualizarMarcadorEmpresa(ubicacion);

            tvCoordenadas.setText(
                    String.format(
                            Locale.US,
                            "Ubicación seleccionada (sin guardar)\nLatitud: %.6f\nLongitud: %.6f",
                            ubicacion.latitude,
                            ubicacion.longitude
                    )
            );

            btnGuardarUbicacion.setEnabled(true);
        });
    }

    private void actualizarMarcadorEmpresa(LatLng ubicacion) {
        if (marcadorEmpresa != null) {
            marcadorEmpresa.remove();
        }

        marcadorEmpresa = googleMap.addMarker(
                new MarkerOptions()
                        .position(ubicacion)
                        .title("Ubicación de la empresa")
                        .icon(BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory.HUE_RED
                        ))
        );
    }

    private void guardarUbicacion() {
        if (ubicacionSeleccionada == null) {
            Toast.makeText(
                    this,
                    "Seleccione una ubicación en el mapa",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        preferencias.edit()
                .putLong(
                        KEY_LATITUD,
                        Double.doubleToRawLongBits(
                                ubicacionSeleccionada.latitude
                        )
                )
                .putLong(
                        KEY_LONGITUD,
                        Double.doubleToRawLongBits(
                                ubicacionSeleccionada.longitude
                        )
                )
                .putBoolean(KEY_CONFIGURADA, true)
                .apply();

        ubicacionGuardada = ubicacionSeleccionada;

        btnGuardarUbicacion.setEnabled(false);
        btnComoLlegar.setEnabled(true);

        tvCoordenadas.setText(
                String.format(
                        Locale.US,
                        "Ubicación empresarial guardada\nLatitud: %.6f\nLongitud: %.6f",
                        ubicacionGuardada.latitude,
                        ubicacionGuardada.longitude
                )
        );

        Toast.makeText(
                this,
                "Ubicación guardada correctamente",
                Toast.LENGTH_SHORT
        ).show();
    }

    private boolean tienePermisoUbicacion() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                ||
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED;
    }

    private void verificarPermisosUbicacion() {
        if (tienePermisoUbicacion()) {
            obtenerUbicacionActual();
        } else {
            permisoUbicacionLauncher.launch(
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    }
            );
        }
    }

    private void obtenerUbicacionActual() {
        if (!tienePermisoUbicacion()) {
            verificarPermisosUbicacion();
            return;
        }

        tvMiUbicacion.setText("Obteniendo ubicación actual...");

        try {
            clienteUbicacion.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    null
            ).addOnSuccessListener(location -> {
                if (location == null) {
                    tvMiUbicacion.setText(
                            "No se pudo obtener la ubicación. Verifique que el GPS esté activado."
                    );
                    return;
                }

                ubicacionUsuario = new LatLng(
                        location.getLatitude(),
                        location.getLongitude()
                );

                if (marcadorUsuario != null) {
                    marcadorUsuario.remove();
                }

                if (googleMap != null) {
                    marcadorUsuario = googleMap.addMarker(
                            new MarkerOptions()
                                    .position(ubicacionUsuario)
                                    .title("Mi ubicación actual")
                                    .icon(BitmapDescriptorFactory.defaultMarker(
                                            BitmapDescriptorFactory.HUE_AZURE
                                    ))
                    );

                    googleMap.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                    ubicacionUsuario,
                                    16f
                            )
                    );
                }

                tvMiUbicacion.setText(
                        String.format(
                                Locale.US,
                                "Mi ubicación actual\nLatitud: %.6f\nLongitud: %.6f",
                                ubicacionUsuario.latitude,
                                ubicacionUsuario.longitude
                        )
                );

                Toast.makeText(
                        this,
                        "Ubicación obtenida correctamente",
                        Toast.LENGTH_SHORT
                ).show();

            }).addOnFailureListener(e -> {
                tvMiUbicacion.setText(
                        "Error al obtener la ubicación: " + e.getMessage()
                );
            });

        } catch (SecurityException e) {
            tvMiUbicacion.setText("Permiso de ubicación no disponible.");
        }
    }

    private void mostrarRuta() {
        if (ubicacionGuardada == null) {
            Toast.makeText(
                    this,
                    "Primero debe guardar la ubicación de la empresa",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        // Google Maps obtiene el punto de partida del dispositivo.
        String destino = String.format(
                Locale.US,
                "%f,%f",
                ubicacionGuardada.latitude,
                ubicacionGuardada.longitude
        );

        Uri uri = Uri.parse(
                "https://www.google.com/maps/dir/?api=1"
                        + "&destination=" + destino
                        + "&travelmode=driving"
        );

        Intent intent = new Intent(Intent.ACTION_VIEW, uri);

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(
                    this,
                    "No hay una aplicación disponible para mostrar la ruta",
                    Toast.LENGTH_LONG
            ).show();
        }
    }
}
