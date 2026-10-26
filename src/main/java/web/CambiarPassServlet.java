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
 * Controlador web del cambio de contrasena (/cambiarpass).
 * Disponible para cualquier usuario con sesion iniciada.
 */
@WebServlet(name = "CambiarPassServlet", urlPatterns = {"/cambiarpass"})
public class CambiarPassServlet extends HttpServlet {

    private final UsuarioController usuarioController = new UsuarioController();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/jsp/cambiarpass.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        try {
            int idUsuario = WebUtil.obtenerUsuario(req).getIdusuario();
            usuarioController.cambiarContrasena(
                    idUsuario,
                    req.getParameter("claveActual"),
                    req.getParameter("claveNueva"),
                    req.getParameter("claveNueva2"));
            WebUtil.mensajeFlash(req, "exito", "Contrasena actualizada correctamente");
        } catch (ValidacionException ex) {
            WebUtil.mensajeFlash(req, "error", ex.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + "/cambiarpass");
    }
}