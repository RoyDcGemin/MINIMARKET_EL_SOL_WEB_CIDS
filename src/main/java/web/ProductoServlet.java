package web;

import controller.CategoriaController;
import controller.ProductoController;
import controller.ValidacionException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Controlador web del CRUD de Productos (/productos).
 *
 * Reemplaza a la JFrame ProductoVista. La tabla se llena con ProductoDTO
 * (producto + categoria) igual que hacia la vista de escritorio, y el combo
 * de categorias se llena con CategoriaController.listar().
 */
@WebServlet(name = "ProductoServlet", urlPatterns = {"/productos"})
public class ProductoServlet extends HttpServlet {

    private final ProductoController productoController = new ProductoController();
    private final CategoriaController categoriaController = new CategoriaController();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("productos", productoController.listarDTO());
        req.setAttribute("categorias", categoriaController.listar());
        req.getRequestDispatcher("/WEB-INF/jsp/productos.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String accion = req.getParameter("accion");
        try {
            switch (accion == null ? "" : accion) {
                case "agregar" -> {
                    productoController.agregar(
                            req.getParameter("codigo"),
                            req.getParameter("nombre"),
                            req.getParameter("principioActivo"),
                            decimal(req, "precio"),
                            decimal(req, "costo"),
                            entero(req, "stock"),
                            entero(req, "stockMinimo"),
                            entero(req, "idcategoria"));
                    WebUtil.mensajeFlash(req, "exito", "Producto agregado correctamente");
                }
                case "modificar" -> {
                    productoController.modificar(
                            entero(req, "id"),
                            req.getParameter("codigo"),
                            req.getParameter("nombre"),
                            req.getParameter("principioActivo"),
                            decimal(req, "precio"),
                            decimal(req, "costo"),
                            entero(req, "stock"),
                            entero(req, "stockMinimo"),
                            entero(req, "idcategoria"));
                    WebUtil.mensajeFlash(req, "exito", "Producto modificado correctamente");
                }
                // ELIMINACION LOGICA: marca activo=0 (UPDATE). No borra la fila.
                case "desactivar" -> {
                    productoController.desactivar(entero(req, "id"));
                    WebUtil.mensajeFlash(req, "exito", "Producto desactivado (eliminación lógica)");
                }
                case "activar" -> {
                    productoController.activar(entero(req, "id"));
                    WebUtil.mensajeFlash(req, "exito", "Producto reactivado");
                }
                // ELIMINACION FISICA: DELETE, solo si nunca se vendio.
                case "eliminar" -> {
                    productoController.eliminar(entero(req, "id"));
                    WebUtil.mensajeFlash(req, "exito", "Producto eliminado definitivamente (DELETE)");
                }
                default -> WebUtil.mensajeFlash(req, "error", "Accion no valida");
            }
        } catch (ValidacionException ex) {
            WebUtil.mensajeFlash(req, "error", ex.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + "/productos");
    }

    /** Convierte un parametro a int (0 si falta o no es numero). */
    private int entero(HttpServletRequest req, String nombre) {
        try {
            return Integer.parseInt(req.getParameter(nombre));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    /** Convierte un parametro a double (0 si falta o no es numero). */
    private double decimal(HttpServletRequest req, String nombre) {
        try {
            return Double.parseDouble(req.getParameter(nombre));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}