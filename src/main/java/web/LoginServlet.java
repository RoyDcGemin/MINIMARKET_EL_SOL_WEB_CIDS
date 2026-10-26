package web;

import controller.UsuarioController;
import controller.ValidacionException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import modelo.Usuario;

/**
 * Controlador web del LOGIN.
 *
 * Reemplaza a la JFrame Logueo: en vez de una ventana, recibe el formulario
 * HTML (POST) y delega la validacion en UsuarioController (que se reutiliza
 * sin cambios). Si las credenciales son correctas, guarda al Usuario en la
 * HttpSession (es la "sesion" que antes se pasaba entre ventanas).
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    private final UsuarioController usuarioController = new UsuarioController();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Si ya hay una sesion iniciada, no tiene sentido volver al login.
        if (WebUtil.obtenerUsuario(req) != null) {
            resp.sendRedirect(req.getContextPath() + "/menu");
            return;
        }
        req.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String nombre = req.getParameter("usuario");
        String clave = req.getParameter("clave");
        nombre = (nombre == null) ? "" : nombre.trim();
        clave = (clave == null) ? "" : clave;

        try {
            Usuario u = usuarioController.iniciarSesion(nombre, clave);
            HttpSession sesion = req.getSession(true);
            sesion.setAttribute(WebUtil.ATTR_USUARIO, u);
            resp.sendRedirect(req.getContextPath() + "/menu");
        } catch (ValidacionException ex) {
            // FLUJO identico al viejo JOptionPane: el controlador lanza el
            // mensaje de error y la vista solo lo muestra.
            req.setAttribute("error", ex.getMessage());
            req.setAttribute("usuarioIngresado", nombre);
            req.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(req, resp);
        }
    }
}