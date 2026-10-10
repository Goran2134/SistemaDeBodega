package com.example.sistemadebodega.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "BodegaSessionPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_NOMBRE_COMPLETO = "nombreCompleto";
    private static final String KEY_ID_ROL = "idRol";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void crearSesion(String username, String nombreCompleto, int idRol) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_NOMBRE_COMPLETO, nombreCompleto);
        editor.putInt(KEY_ID_ROL, idRol);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public int obtenerIdRol() {
        return prefs.getInt(KEY_ID_ROL, -1);
    }

    public String obtenerNombreCompleto() {
        return prefs.getString(KEY_NOMBRE_COMPLETO, "");
    }

    public void cerrarSesion() {
        editor.clear();
        editor.apply();
    }
}