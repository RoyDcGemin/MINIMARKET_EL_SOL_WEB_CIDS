package web;

import controller.ProductoController;
import controller.UsuarioController;
import controller.ValidacionException;
import controller.VentaController;
import dto.VentaDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

/**
 * Controlador web de REPORTES de ventas (/reportes) - RF07.
 *
 * Lista las ventas con filtros por fecha, cajero y producto, muestra
 * el detalle de una venta (?ver=N) y permite anularla (sostiene %ERR).
 * Ademas calcula las 4 tarjetas de indicadores del informe:
 * TPRV (tiempo promedio), %ERR (errores), TPV (ticket promedio)
 * y %OK (ventas correctas).
 */
@WebServlet(name = "ReportesServlet", urlPatterns = {"/reportes"})
public class ReportesServlet extends HttpServlet {

    private final VentaController ventaController = new VentaController();
    private final UsuarioController usuarioController = new UsuarioController();
    private final ProductoController productoController = new ProductoController();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Filtros (por defecto: ultimos 30 dias, todos los cajeros/productos).
        String hasta = parametro(req, "hasta", LocalDate.now().toString());
        String desde = parametro(req, "desde", LocalDate.now().minusDays(30).toString());
        int idCajero = entero(req, "cajero");
        int idProducto = entero(req, "producto");

        ArrayList<VentaDTO> ventas = ventaController.listarConFiltros(desde, hasta, idCajero, idProducto);

        req.setAttribute("ventas", ventas);
        req.setAttribute("desde", desde);
        req.setAttribute("hasta", hasta);
        req.setAttribute("idCajero", idCajero);
        req.setAttribute("idProducto", idProducto);
        req.setAttribute("cajeros", usuarioController.listar());
        req.setAttribute("productos", productoController.listar());

        // Tarjetas de indicadores sobre el mismo conjunto filtrado.
        req.setAttribute("tprv", tiempoPromedio(ventas));
        req.setAttribute("porcErr", porcentajeErrores(ventas));
        req.setAttribute("tpv", ticketPromedio(ventas));
        req.setAttribute("porcOk", porcentajeCorrectas(ventas));

        // 1) Reporte por fechas: ya esta en "ventas" (filtrado por rango).
        // 2) Reporte maximo / minimo / promedio (funciones SQL MAX, MIN, AVG).
        double[] est = ventaController.maxMinPromedio(desde, hasta);
        req.setAttribute("maxVenta", est[0]);
        req.setAttribute("minVenta", est[1]);
        req.setAttribute("promVenta", est[2]);
        // 3) Reporte de ingresos, utilidad y unidades (JOIN de 3 tablas).
        double[] iu = ventaController.ingresosUtilidad(desde, hasta);
        req.setAttribute("ingresos", iu[0]);
        req.setAttribute("utilidad", iu[1]);
        req.setAttribute("unidades", iu[2]);
        // 4) Reporte de registros eliminados (eliminacion logica: activo = 0).
        req.setAttribute("inactivos", productoController.listarInactivos());

        // Detalle opcional: /reportes?ver=ID (conserva los filtros).
        int idMostrar = entero(req, "ver");
        if (idMostrar > 0) {
            for (VentaDTO v : ventas) {
                if (v.getIdVenta() == idMostrar) {
                    req.setAttribute("detalle", v);
                    break;
                }
            }
        }

        req.getRequestDispatcher("/WEB-INF/jsp/reportes.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        if ("anular".equals(req.getParameter("accion"))) {
            try {
                ventaController.anularVenta(entero(req, "id"));
                WebUtil.mensajeFlash(req, "exito", "Venta anulada correctamente");
            } catch (ValidacionException ex) {
                WebUtil.mensajeFlash(req, "error", ex.getMessage());
            }
        } else {
            WebUtil.mensajeFlash(req, "error", "Accion no valida");
        }
        resp.sendRedirect(req.getContextPath() + "/reportes");
    }

    /**
     * TPRV (seg) = promedio de (hora_fin - hora_inicio) sobre las ventas
     * que tienen ambas marcas. Es el indicador de agilidad en caja.
     */
    private double tiempoPromedio(ArrayList<VentaDTO> ventas) {
        long suma = 0;
        int n = 0;
        for (VentaDTO v : ventas) {
            Long segundos = segundosEntre(v.getHoraInicio(), v.getHoraFin());
            if (segundos != null && segundos >= 0) {
                suma += segundos;
                n++;
            }
        }
        return (n == 0) ? 0 : ((double) suma) / n;
    }

    /** %ERR = (anuladas + corregidas) / total * 100. */
    private double porcentajeErrores(ArrayList<VentaDTO> ventas) {
        if (ventas.isEmpty()) {
            return 0;
        }
        int malas = 0;
        for (VentaDTO v : ventas) {
            if (!"VIGENTE".equalsIgnoreCase(v.getEstado())) {
                malas++;
            }
        }
        return (malas * 100.0) / ventas.size();
    }

    /** TPV (S/) = suma de totales / numero de ventas. */
    private double ticketPromedio(ArrayList<VentaDTO> ventas) {
        if (ventas.isEmpty()) {
            return 0;
        }
        double suma = 0;
        for (VentaDTO v : ventas) {
            suma += v.getTotal();
        }
        return suma / ventas.size();
    }

    /** %OK = ventas completas (con detalle) / total * 100. */
    private double porcentajeCorrectas(ArrayList<VentaDTO> ventas) {
        if (ventas.isEmpty()) {
            return 0;
        }
        int buenas = 0;
        for (VentaDTO v : ventas) {
            if (v.getDetalles() != null && !v.getDetalles().isEmpty()
                    && "VIGENTE".equalsIgnoreCase(v.getEstado())) {
                buenas++;
            }
        }
        return (buenas * 100.0) / ventas.size();
    }

    /** Segundos entre dos marcas "yyyy-MM-dd HH:mm:ss" (null si no parsean). */
    private Long segundosEntre(String inicio, String fin) {
        try {
            DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime a = LocalDateTime.parse(inicio, f);
            LocalDateTime b = LocalDateTime.parse(fin, f);
            return ChronoUnit.SECONDS.between(a, b);
        } catch (DateTimeParseException | NullPointerException ex) {
            return null;
        }
    }

    /** Parametro de texto con valor por defecto. */
    private String parametro(HttpServletRequest req, String nombre, String defecto) {
        String v = req.getParameter(nombre);
        return (v == null || v.isBlank()) ? defecto : v.trim();
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
