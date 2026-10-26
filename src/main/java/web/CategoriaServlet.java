package web;

import controller.CategoriaController;
import controller.ValidacionException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Controlador web del CRUD de Categorias (/categorias).
 *
 * Vista NUEVA: en el proyecto de escritorio las categorias no tenian pantalla,
 * solo se elegian desde el formulario de productos. Junto con el
 * CategoriaController (que ahora tambien tiene agregar/modificar/eliminar),
 * esta pagina completa la gestion.
 */
@WebServlet(name = "CategoriaServlet", urlPatterns = {"/categorias"})
public class CategoriaServlet extends HttpServlet {

    private final CategoriaController categoriaController = new CategoriaController();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("categorias", categoriaController.listar());
        req.getRequestDispatcher("/WEB-INF/jsp/categorias.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String accion = req.getParameter("accion");
        try {
            switch (accion == null ? "" : accion) {
                case "agregar" -> {
                    categoriaController.agregar(req.getParameter("descripcion"));
                    WebUtil.mensajeFlash(req, "exito", "Categoria agregada correctamente");
                }
                case "modificar" -> {
                    categoriaController.modificar(
                            entero(req, "id"), req.getParameter("descripcion"));
                    WebUtil.mensajeFlash(req, "exito", "Categoria modificada correctamente");
                }
                case "eliminar" -> {
                    categoriaController.eliminar(entero(req, "id"));
                    WebUtil.mensajeFlash(req, "exito", "Categoria eliminada");
                }
                default -> WebUtil.mensajeFlash(req, "error", "Accion no valida");
            }
        } catch (ValidacionException ex) {
            WebUtil.mensajeFlash(req, "error", ex.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + "/categorias");
    }

    /** Convierte un parametro a int (0 si falta o no es numero). */
    private int entero(HttpServletRequest req, String nombre) {
        try {
            return Integer.parseInt(req.getParameter(nombre));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}