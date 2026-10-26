package web;

import controller.VentaController;
import dto.VentaDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Controlador web del CIERRE DE CAJA (/cierre) - RF06.
 *
 * Al final del turno muestra, para la fecha elegida (por defecto hoy):
 *  - total vendido, numero de transacciones y ticket promedio;
 *  - desglose por metodo de pago (EFECTIVO/YAPE/TRANSFERENCIA);
 *  - lista de las ventas del dia.
 *
 * Solo cuentan las ventas VIGENTES: las anuladas no entraron a caja.
 */
@WebServlet(name = "CierreServlet", urlPatterns = {"/cierre"})
public class CierreServlet extends HttpServlet {

    private final VentaController ventaController = new VentaController();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String fecha = req.getParameter("fecha");
        if (fecha == null || fecha.isBlank()) {
            fecha = LocalDate.now().toString();
        }

        ArrayList<VentaDTO> delDia = ventaController.listarConFiltros(fecha, fecha, 0, 0);

        double total = 0;
        int n = 0;
        // metodo -> [total, cantidad]. LinkedHashMap conserva el orden.
        Map<String, double[]> porMetodo = new LinkedHashMap<>();
        for (String m : VentaController.METODOS_PAGO) {
            porMetodo.put(m, new double[]{0, 0});
        }
        for (VentaDTO v : delDia) {
            if (!"VIGENTE".equalsIgnoreCase(v.getEstado())) {
                continue;
            }
            total += v.getTotal();
            n++;
            double[] fila = porMetodo.get(v.getMetodoPago());
            if (fila == null) {
                fila = new double[]{0, 0};
                porMetodo.put(v.getMetodoPago(), fila);
            }
            fila[0] += v.getTotal();
            fila[1] += 1;
        }

        req.setAttribute("fecha", fecha);
        req.setAttribute("ventas", delDia);
        req.setAttribute("total", total);
        req.setAttribute("n", n);
        req.setAttribute("ticket", (n == 0) ? 0 : total / n);
        req.setAttribute("porMetodo", porMetodo);
        req.getRequestDispatcher("/WEB-INF/jsp/cierre.jsp").forward(req, resp);
    }
}
