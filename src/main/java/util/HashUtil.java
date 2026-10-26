package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utilidad para "hashear" (resumir) texto, usado para las contrasenias.
 *
 * Por que se usa:
 * - Las contrasenias NO deben guardarse en texto plano en la base de datos.
 * - Se guarda el resumen SHA-256 de la contrasenia.
 * - Al iniciar sesion se hashea lo que el usuario escribe y se compara
 *   con el hash guardado.
 *
 * Nota Java moderno: existen algoritmos mas seguros (bcrypt/argon2)
 * pensados para contrasenias. SHA-256 es suficiente para un proyecto
 * de estudio y sirve para entender el concepto de "hash".
 */
public class HashUtil {

    // Constructor privado: clase de utileria, no se instancia.
    private HashUtil() {
    }

    /**
     * Devuelve el hash SHA-256 (en hexadecimal) de una cadena de texto.
     *
     * @param texto la cadena a hashear (por ejemplo, la contrasenia)
     * @return hash en formato hexadecimal (64 caracteres)
     */
    public static String sha256(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(texto.getBytes(StandardCharsets.UTF_8));

            // Convertimos los bytes a hexadecimal para guardarlo como texto.
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            // No deberia ocurrir: SHA-256 existe en todo JDK.
            throw new RuntimeException("Algoritmo SHA-256 no disponible", ex);
        }
    }
}