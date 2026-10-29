package web;

import controller.ProductoController;
import controller.ValidacionException;
import controller.VentaController;
import dto.DetalleVentaDTO;
import dto.VentaDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import modelo.Producto;
import modelo.Usuario;

/**
 * Controlador web de la VENTA (/venta).
 *
 * Es la pantalla principal del punto de venta:
 *  - El cajero digita el CODIGO del producto + OK (emula el scanner
 *    de codigo de barras, sin hardware) e indica la CANTIDAD.
 *  - El "carrito" (los detalles que aun no se guardan) vive en la
 *    HttpSession junto con la hora de inicio (base del TPRV).
 *  - Al confirmar se pide DNI + nombre (snapshot, sin tabla cliente),
 *    descuento (RF04) y metodo de pago (RF06).
 *
 * FLUJO:
 *  1. GET  /venta              -> muestra el formulario + el carrito.
 *  2. POST accion=agregar      -> busca por codigo, valida stock y agrega.
 *  3. GET  {@code ?accion=quitar&i=n}  -> quita un detalle del carrito.
 *  4. GET  ?accion=limpiar     -> vacia el carrito (y reinicia la hora).
 *  5. POST accion=vender       -> guarda TODO en la BD (transaccion).
 *  6. GET  {@code ?accion=confirmada&id=N} -> muestra el comprobante HTML.
 *
 * El total SIEMPRE se recalcula del lado del servidor (nunca se confia en
 * el texto de un campo de la pagina).
 */
@WebServlet(name = "VentaServlet", urlPatterns = {"/venta"})
public class VentaServlet extends HttpServlet {

    private final VentaController ventaController = new VentaController();
    private final ProductoController productoController = new ProductoController();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String accion = req.getParameter("accion");

        // Carrito guardado en la sesion (se crea con su hora de inicio).
        ArrayList<DetalleVentaDTO> carrito = obtenerCarrito(req);

        if ("quitar".equals(accion)) {
            int indice = entero(req, "i");
            if (indice >= 0 && indice < carrito.size()) {
                carrito.remove(indice);
            }
            resp.sendRedirect(req.getContextPath() + "/venta");
            return;
        }

        if ("limpiar".equals(accion)) {
            carrito.clear();
            req.getSession().removeAttribute("horaInicio");
            resp.sendRedirect(req.getContextPath() + "/venta");
            return;
        }

        if ("confirmada".equals(accion)) {
            mostrarComprobante(req, resp, entero(req, "id"));
            return;
        }

        // Formulario normal. Pool filtrable por codigo, nombre comercial
        // o descripcion (el cliente suele pedir por presentacion).
        String q = req.getParameter("q");
        q = (q == null) ? "" : q.trim();
        ArrayList<dto.ProductoDTO> productos = productoController.listarDTO();
        if (!q.isEmpty()) {
            String ql = q.toLowerCase();
            ArrayList<dto.ProductoDTO> filtrados = new ArrayList<>();
            for (dto.ProductoDTO p : productos) {
                String cod = p.getCodigo() == null ? "" : p.getCodigo().toLowerCase();
                String nom = p.getNombre() == null ? "" : p.getNombre().toLowerCase();
                String pa = p.getDescripcion() == null ? "" : p.getDescripcion().toLowerCase();
                if (cod.contains(ql) || nom.contains(ql) || pa.contains(ql)) {
                    filtrados.add(p);
                }
            }
            productos = filtrados;
        }
        req.setAttribute("productos", productos);
        req.setAttribute("q", q);
        req.setAttribute("carrito", carrito);
        req.setAttribute("subtotalCarrito", calcularSubtotal(carrito));
        req.setAttribute("siguienteId", ventaController.obtenerSiguienteId());
        req.setAttribute("dniSugeridos", ventaController.listarDniRegistrados());
        req.setAttribute("metodos", VentaController.METODOS_PAGO);
        req.getRequestDispatcher("/WEB-INF/jsp/venta.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String accion = req.getParameter("accion");

        if ("agregar".equals(accion)) {
            agregarDetalle(req, resp);
            return;
        }

        if ("vender".equals(accion)) {
            vender(req, resp);
            return;
        }

        WebUtil.mensajeFlash(req, "error", "Accion no valida");
        resp.sendRedirect(req.getContextPath() + "/venta");
    }

    /**
     * Agrega un detalle al carrito buscando el producto por su CODIGO
     * digitado (emulacion del scanner) y validando stock suficiente.
     */
    private void agregarDetalle(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String codigo = req.getParameter("codigo");
        codigo = (codigo == null) ? "" : codigo.trim();
        int cantidad = entero(req, "cantidad");
        String contexto = req.getContextPath();

        Producto producto = productoController.buscarPorCodigo(codigo);
        if (producto == null && !codigo.isEmpty()) {
            // Fallback del minimarket: permite digitar una descripcion o el
            // nombre comercial (ej. "arroz") en vez del codigo exacto.
            java.util.ArrayList<Producto> cands = productoController.buscarPorTexto(codigo);
            if (cands.size() == 1) {
                producto = cands.get(0);
            } else if (cands.size() > 1) {
                WebUtil.mensajeFlash(req, "error",
                        "Hay " + cands.size() + " productos que coinciden con \"" + codigo
                        + "\". Use el codigo exacto del pool.");
                resp.sendRedirect(contexto + "/venta");
                return;
            }
        }
        if (producto == null) {
            WebUtil.mensajeFlash(req, "error",
                    "No existe producto con codigo \"" + codigo + "\"");
            resp.sendRedirect(contexto + "/venta");
            return;
        }
        if (cantidad <= 0) {
            WebUtil.mensajeFlash(req, "error", "La cantidad debe ser mayor a 0");
            resp.sendRedirect(contexto + "/venta");
            return;
        }

        // Stock: sumamos lo pedido MAS lo que ya esta en el carrito de ese
        // producto, para no superar el disponible.
        ArrayList<DetalleVentaDTO> carrito = obtenerCarrito(req);
        int enCarrito = 0;
        for (DetalleVentaDTO d : carrito) {
            if (d.getProducto().getIdProducto() == producto.getIdProducto()) {
                enCarrito += d.getCantidad();
            }
        }
        if (enCarrito + cantidad > producto.getStock()) {
            WebUtil.mensajeFlash(req, "error",
                    "Stock insuficiente de \"" + producto.getNombre()
                    + "\". Disponible: " + producto.getStock());
            resp.sendRedirect(contexto + "/venta");
            return;
        }

        DetalleVentaDTO detalle = new DetalleVentaDTO();
        detalle.setProducto(producto);
        detalle.setCantidad(cantidad);
        detalle.setSubtotal(producto.getPrecio() * cantidad);

        carrito.add(detalle);
        resp.sendRedirect(contexto + "/venta");
    }

    /** Guarda la venta en la BD (transaccion) y muestra el comprobante. */
    private void vender(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Usuario usuario = WebUtil.obtenerUsuario(req);
        String dni = req.getParameter("dni");
        String nombre = req.getParameter("nombre");
        String metodo = req.getParameter("metodo");
        double descuento = decimal(req, "descuento");
        String contexto = req.getContextPath();

        HttpSession sesion = req.getSession();
        String horaInicio = (String) sesion.getAttribute("horaInicio");
        if (horaInicio == null) {
            horaInicio = ahora();
        }

        ArrayList<DetalleVentaDTO> carrito = obtenerCarrito(req);
        try {
            int idVenta = ventaController.registrarVentaEnBD(
                    dni, nombre, usuario.getIdusuario(), horaInicio,
                    carrito, descuento, metodo);
            carrito.clear();
            sesion.removeAttribute("horaInicio");
            resp.sendRedirect(contexto + "/venta?accion=confirmada&id=" + idVenta);
        } catch (ValidacionException ex) {
            WebUtil.mensajeFlash(req, "error", ex.getMessage());
            resp.sendRedirect(contexto + "/venta");
        }
    }

    /** Busca la venta recien creada y la envia a la pagina del comprobante. */
    private void mostrarComprobante(HttpServletRequest req, HttpServletResponse resp,
                                    int idVenta) throws ServletException, IOException {
        VentaDTO venta = null;
        for (VentaDTO v : ventaController.listarVentasConDetalles()) {
            if (v.getIdVenta() == idVenta) {
                venta = v;
                break;
            }
        }
        if (venta == null) {
            WebUtil.mensajeFlash(req, "error", "No se encontro la venta");
            resp.sendRedirect(req.getContextPath() + "/venta");
            return;
        }
        req.setAttribute("venta", venta);
        req.getRequestDispatcher("/WEB-INF/jsp/comprobante.jsp").forward(req, resp);
    }

    /** Suma los subtotales del carrito (servidor, nunca cliente). */
    private double calcularSubtotal(List<DetalleVentaDTO> carrito) {
        double total = 0;
        for (DetalleVentaDTO d : carrito) {
            total += d.getSubtotal();
        }
        return total;
    }

    /**
     * Devuelve el carrito de la sesion (lo crea si no existe) junto con
     * su hora de inicio (base del indicador TPRV).
     */
    private ArrayList<DetalleVentaDTO> obtenerCarrito(HttpServletRequest req) {
        HttpSession sesion = req.getSession();
        @SuppressWarnings("unchecked")
        ArrayList<DetalleVentaDTO> carrito =
                (ArrayList<DetalleVentaDTO>) sesion.getAttribute("carrito");
        if (carrito == null) {
            carrito = new ArrayList<>();
            sesion.setAttribute("carrito", carrito);
            sesion.setAttribute("horaInicio", ahora());
        }
        if (sesion.getAttribute("horaInicio") == null) {
            sesion.setAttribute("horaInicio", ahora());
        }
        return carrito;
    }

    /** Fecha-hora actual en formato MySQL DATETIME. */
    private String ahora() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
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
