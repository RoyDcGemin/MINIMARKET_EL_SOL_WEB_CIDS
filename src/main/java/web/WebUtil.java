package web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Locale;
import modelo.Usuario;

/**
 * Utilidades compartidas por los servlets y las vistas.
 */
public final class WebUtil {

    /** Nombre del atributo de sesion donde se guarda el Usuario logueado. */
    public static final String ATTR_USUARIO = "usuario";

    /** Nombre del atributo de sesion para los mensajes "flash" (exito/error). */
    public static final String ATTR_FLASH = "flash";

    private WebUtil() {
    }

    /** Devuelve el Usuario guardado en la sesion, o null si no hay sesion. */
    public static Usuario obtenerUsuario(HttpServletRequest req) {
        HttpSession sesion = req.getSession(false);
        if (sesion == null) {
            return null;
        }
        Object u = sesion.getAttribute(ATTR_USUARIO);
        return (u instanceof Usuario) ? (Usuario) u : null;
    }

    /** Indica si el usuario tiene rol ADMIN. */
    public static boolean esAdmin(Usuario usuario) {
        return usuario != null && "ADMIN".equalsIgnoreCase(usuario.getRol());
    }

    /**
     * Guarda un mensaje que se mostrara UNA sola vez en la siguiente pagina.
     * Se usa el patron "redirect despues del POST": el servlet redirige y la
     * vista lee este mensaje y lo borra.
     *
     * @param tipo "exito", "error" o "info"
     */
    public static void mensajeFlash(HttpServletRequest req, String tipo, String texto) {
        HttpSession sesion = req.getSession();
        sesion.setAttribute(ATTR_FLASH, new String[]{tipo, texto});
    }

    /** Formatea un monto como "S/. 1,234.50". */
    public static String formatearMonto(double valor) {
        return String.format(new Locale("es", "PE"), "S/. %,.2f", valor);
    }

    /** Formatea un numero entero simple. */
    public static String formatearCantidad(int valor) {
        return String.format(new Locale("es", "PE"), "%,d", valor);
    }
}