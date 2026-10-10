package com.example.sistemadebodega.utils;

import android.util.Base64;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordUtils {

    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA1";

    // Generar hash seguro a partir de una contraseña en texto plano
    public static String hashPassword(String password) {
        try {
            SecureRandom sr = new SecureRandom();
            byte[] salt = new byte[16];
            sr.nextBytes(salt);

            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = factory.generateSecret(spec).getEncoded();

            // Combinar salt y hash en un solo string separado por dos puntos
            String saltStr = Base64.encodeToString(salt, Base64.NO_WRAP);
            String hashStr = Base64.encodeToString(hash, Base64.NO_WRAP);

            return saltStr + ":" + hashStr;
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el hash de la contraseña", e);
        }
    }

    // Verificar si una contraseña ingresada coincide con el hash almacenado
    public static boolean verifyPassword(String password, String storedHash) {
        try {
            String[] parts = storedHash.split(":");
            if (parts.length != 2) return false;

            byte[] salt = Base64.decode(parts[0], Base64.NO_WRAP);
            byte[] hash = Base64.decode(parts[1], Base64.NO_WRAP);

            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] testHash = factory.generateSecret(spec).getEncoded();

            int diff = hash.length ^ testHash.length;
            for (int i = 0; i < hash.length && i < testHash.length; i++) {
                diff |= hash[i] ^ testHash[i];
            }
            return diff == 0;
        } catch (Exception e) {
            return false;
        }
    }
}