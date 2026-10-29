package util;

/**
 * POLITICA DE CONTRASENAS del Minimarket "El Sol".
 *
 * Reglas exigidas en la indicacion (validacion de datos del logueo):
 *   1) longitud minima de 8 posiciones;
 *   2) al menos una letra minuscula;
 *   3) al menos una letra mayuscula;
 *   4) al menos un numero;
 *   5) al menos un simbolo especial.
 *
 * Se aplica cuando una clave se CREA (registro de usuario) o se CAMBIA,
 * y tambien en iniciarSesion(), para que el mensaje se vea en la propia
 * ventana de logueo. La clase centraliza las reglas: si cambia la
 * politica, se cambia solo aqui.
 */
public final class PasswordPolicy {

    /** Largo minimo de la contrasena (indicacion del curso). */
    public static final int LARGO_MINIMO = 8;

    /** Largo maximo (coincide con el ancho razonable del campo en la BD). */
    public static final int LARGO_MAXIMO = 64;

    private PasswordPolicy() {
    }

    /**
     * Valida una contraseña contra la politica.
     *
     * @param clave la contraseña propuesta (sin hashear)
     * @return null si la clave CUMPLE la politica; en caso contrario el
     *         mensaje de error exacto para mostrar al usuario
     */
    public static String validar(String clave) {
        if (clave == null || clave.isEmpty()) {
            return "Ingresar contraseña";
        }
        if (clave.length() < LARGO_MINIMO) {
            return "La contraseña debe tener al menos " + LARGO_MINIMO + " caracteres";
        }
        if (clave.length() > LARGO_MAXIMO) {
            return "La contraseña no puede superar " + LARGO_MAXIMO + " caracteres";
        }
        if (!clave.matches(".*[a-z].*")) {
            return "La contraseña debe incluir al menos una letra minúscula";
        }
        if (!clave.matches(".*[A-Z].*")) {
            return "La contraseña debe incluir al menos una letra mayúscula";
        }
        if (!clave.matches(".*[0-9].*")) {
            return "La contraseña debe incluir al menos un numero";
        }
        if (!clave.matches(".*[^A-Za-z0-9].*")) {
            return "La contraseña debe incluir al menos un simbolo especial";
        }
        return null; // cumple todas las reglas
    }

    /**
     * Verificacion booleana de la politica (util para pruebas o para la
     * vista). Equivale a validar(clave) == null.
     */
    public static boolean cumple(String clave) {
        return validar(clave) == null;
    }
}
