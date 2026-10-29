package web;

import controller.UsuarioController;
import controller.ValidacionException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Controlador web del REGISTRO de usuarios (/registro).
 *
 * Es la "opcion de registrar nuevos usuarios" de la ventana de logueo:
 * desde login.jsp hay un enlace <a href="/registro">Crear cuenta</a>.
 *
 * SEGURIDAD (autorizacion):
 *  - la pagina es publica (AuthFilter), pero TODA cuenta creada aqui nace
 *    como CAJERO; el rol NO se lee del formulario (UsuarioController lo
 *    fuerza), de modo que nadie puede autoasignarse el perfil ADMIN.
 *  - las cuentas ADMIN solo se crean en elsol_db.sql (fuera del sistema).
 */
@WebServlet(name = "RegistroServlet", urlPatterns = {"/registro"})
public class RegistroServlet extends HttpServlet {

    private final UsuarioController usuarioController = new UsuarioController();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Si ya hay sesion, no tiene sentido registrar otra cuenta.
        if (WebUtil.obtenerUsuario(req) != null) {
            resp.sendRedirect(req.getContextPath() + "/menu");
            return;
        }
        req.getRequestDispatcher("/WEB-INF/jsp/registro.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        String usuario = req.getParameter("usuario");
        String nombre = req.getParameter("nombre");
        String clave = req.getParameter("clave");
        String clave2 = req.getParameter("clave2");
        usuario = (usuario == null) ? "" : usuario.trim();

        try {
            usuarioController.registrar(usuario, nombre, clave, clave2);
            // FLUJO identico al patron "redirect despues del POST":
            // se envia al login con un mensaje de exito para iniciar sesion.
            WebUtil.mensajeFlash(req, "exito",
                    "Usuario \"" + usuario + "\" creado con perfil CAJERO. Ya puede iniciar sesion");
            resp.sendRedirect(req.getContextPath() + "/login");
        } catch (ValidacionException ex) {
            req.setAttribute("error", ex.getMessage());
            req.setAttribute("usuarioIngresado", usuario);
            req.setAttribute("nombreIngresado", nombre);
            req.getRequestDispatcher("/WEB-INF/jsp/registro.jsp").forward(req, resp);
        }
    }
}
