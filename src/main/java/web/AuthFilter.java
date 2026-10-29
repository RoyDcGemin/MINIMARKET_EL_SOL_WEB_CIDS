package web;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filtro de AUTENTICACION Y AUTORIZACION (los dos perfiles del sistema).
 *
 * 1) AUTENTICACION: protege TODAS las rutas; si no hay un Usuario en la
 *    sesion, redirige al login. Solo quedan publicos el login y los
 *    recursos estaticos (css, js, imagenes).
 *
 * 2) AUTORIZACION POR ROL (2 perfiles):
 *    - ADMIN  : acceso total (venta, cierre, reportes, productos, categorias).
 *    - CAJERO : venta, comprobante, cierre de caja y CONSULTA del catalogo.
 *    Si un CAJERO intenta entrar a una ruta de administracion (/categorias,
 *    /reportes) se le redirige al menu con un mensaje.
 *    (La modificacion del catalogo de productos se bloquea por ACCION en
 *    ProductoServlet: la vista /productos es de lectura para el CAJERO.)
 */
@WebFilter(urlPatterns = {"/*"})
public class AuthFilter implements Filter {

    /** Rutas que solo puede usar el perfil ADMIN. */
    private static final String[] RUTAS_ADMIN = {"/categorias", "/reportes"};

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String contexto = req.getContextPath();
        String ruta = req.getRequestURI();

        // Rutas publicas: el login y los recursos estaticos.
        if (esRutaPublica(contexto, ruta)) {
            chain.doFilter(request, response);
            return;
        }

        // Si no hay sesion (o no hay usuario), fuera.
        modelo.Usuario usuario = WebUtil.obtenerUsuario(req);
        if (usuario == null) {
            resp.sendRedirect(contexto + "/login");
            return;
        }

        // AUTORIZACION: el cajero no puede entrar a las rutas de administracion.
        if (!WebUtil.esAdmin(usuario) && esRutaAdmin(contexto, ruta)) {
            WebUtil.mensajeFlash(req, "error", "Su perfil de CAJERO no tiene acceso a esa opcion");
            resp.sendRedirect(contexto + "/menu");
            return;
        }

        chain.doFilter(request, response);
    }

    /** Indica si la ruta es de administracion (solo ADMIN). */
    private boolean esRutaAdmin(String contexto, String ruta) {
        for (String r : RUTAS_ADMIN) {
            // startsWith para cubrir tambien subrutas como /reportes/excel
            if (ruta.startsWith(contexto + r)) {
                return true;
            }
        }
        return false;
    }

    /** Indica si la ruta puede verse sin iniciar sesion. */
    private boolean esRutaPublica(String contexto, String ruta) {
        if (ruta.equals(contexto) || ruta.equals(contexto + "/")) {
            return true; // raiz: redirige via index.jsp
        }
        if (ruta.equals(contexto + "/login") || ruta.equals(contexto + "/index.jsp")
                || ruta.equals(contexto + "/registro")) {
            return true;
        }
        if (ruta.startsWith(contexto + "/css/")
                || ruta.startsWith(contexto + "/js/")
                || ruta.startsWith(contexto + "/img/")) {
            return true;
        }
        return false;
    }
}