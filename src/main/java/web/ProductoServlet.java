package web;

import controller.CategoriaController;
import controller.ProductoController;
import controller.ValidacionException;
import dto.ProductoDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Controlador web del CRUD de Productos (/productos).
 *
 * Reemplaza a la JFrame ProductoVista. La tabla se llena con ProductoDTO
 * (producto + categoria) igual que hacia la vista de escritorio, y el combo
 * de categorias se llena con CategoriaController.listar().
 *
 * AUTORIZACION (2 niveles):
 *  - RUTA: /categorias y /reportes son solo de ADMIN (ver AuthFilter).
 *  - ACCION: /productos abre para ambos perfiles, pero solo el ADMIN puede
 *    modificar el catalogo. El POST se bloquea si quien lo envia no es
 *    ADMIN (defensa incluso si alguien manipula el HTML).
 */
@WebServlet(name = "ProductoServlet", urlPatterns = {"/productos"})
public class ProductoServlet extends HttpServlet {

    private final ProductoController productoController = new ProductoController();
    private final CategoriaController categoriaController = new CategoriaController();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        ArrayList<ProductoDTO> productos = productoController.listarDTO();

        // Consulta con filtro (el boton "Buscar"): si hay texto, se deja en
        // la lista solo lo que coincida con codigo, nombre o descripcion.
        String q = req.getParameter("q");
        q = (q == null) ? "" : q.trim();
        if (!q.isEmpty()) {
            String patron = q.toLowerCase();
            ArrayList<ProductoDTO> filtrados = new ArrayList<>();
            for (ProductoDTO p : productos) {
                String cod = p.getCodigo() == null ? "" : p.getCodigo().toLowerCase();
                String nom = p.getNombre() == null ? "" : p.getNombre().toLowerCase();
                String des = p.getDescripcion() == null ? "" : p.getDescripcion().toLowerCase();
                if (cod.contains(patron) || nom.contains(patron) || des.contains(patron)) {
                    filtrados.add(p);
                }
            }
            productos = filtrados;
        }

        req.setAttribute("productos", productos);
        req.setAttribute("q", q);
        req.setAttribute("categorias", categoriaController.listar());
        req.getRequestDispatcher("/WEB-INF/jsp/productos.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // GUARDIA DE AUTORIZACION: aunque la vista le muestre la tabla al
        // CAJERO en modo lectura, el servidor vuelve a verificar el rol
        // antes de aceptar cualquier cambio del catalogo.
        if (!WebUtil.esAdmin(WebUtil.obtenerUsuario(req))) {
            WebUtil.mensajeFlash(req, "error",
                    "Su perfil de CAJERO no tiene permiso para modificar el catalogo");
            resp.sendRedirect(req.getContextPath() + "/productos");
            return;
        }

        String accion = req.getParameter("accion");
        try {
            switch (accion == null ? "" : accion) {
                case "agregar" -> {
                    productoController.agregar(
                            req.getParameter("codigo"),
                            req.getParameter("nombre"),
                            req.getParameter("descripcion"),
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
                            req.getParameter("descripcion"),
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

    /**
     * Convierte un parametro a int. VALIDACION DE TIPO: si el texto no es
     * un numero entero lanza una excepcion de validacion (antes devolvia
     * 0 y el mensaje de error final era enganoso).
     */
    private int entero(HttpServletRequest req, String nombre) throws ValidacionException {
        String valor = req.getParameter(nombre);
        if (valor == null || valor.trim().isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException ex) {
            throw new ValidacionException("El campo \"" + nombre + "\" debe ser un numero entero");
        }
    }

    /**
     * Convierte un parametro a double. VALIDACION DE TIPO: como arriba;
     * ademas rechaza valores no numericos aunque el navegador los envie.
     */
    private double decimal(HttpServletRequest req, String nombre) throws ValidacionException {
        String valor = req.getParameter(nombre);
        if (valor == null || valor.trim().isEmpty()) {
            return 0;
        }
        try {
            return Double.parseDouble(valor.trim());
        } catch (NumberFormatException ex) {
            throw new ValidacionException("El campo \"" + nombre + "\" debe ser un numero");
        }
    }
}
