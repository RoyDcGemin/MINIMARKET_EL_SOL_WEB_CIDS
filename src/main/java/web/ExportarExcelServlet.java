package web;

import controller.VentaController;
import dto.VentaDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Exporta el REPORTE DE VENTAS a Excel (.xlsx) usando Apache POI.
 *
 * Ruta: /reportes/excel?desde=aaaa-mm-dd&hasta=aaaa-mm-dd
 *
 * Es una de las librerias de apoyo del proyecto: Apache POI permite
 * generar el archivo Excel desde Java sin herramientas externas.
 * Solo accesible para el perfil ADMIN (lo controla AuthFilter).
 */
@WebServlet(name = "ExportarExcelServlet", urlPatterns = {"/reportes/excel"})
public class ExportarExcelServlet extends HttpServlet {

    private final VentaController ventaController = new VentaController();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String desde = valor(req.getParameter("desde"), LocalDate.now().minusDays(30).toString());
        String hasta = valor(req.getParameter("hasta"), LocalDate.now().toString());

        ArrayList<VentaDTO> ventas = ventaController.listarConFiltros(desde, hasta, 0, 0);

        // Apache POI: libro XLSX -> hoja -> filas y celdas.
        try (XSSFWorkbook libro = new XSSFWorkbook()) {

            Sheet hoja = libro.createSheet("Ventas");

            Font negrita = libro.createFont();
            negrita.setBold(true);
            CellStyle estiloCabecera = libro.createCellStyle();
            estiloCabecera.setFont(negrita);

            String[] columnas = {"ID", "Fecha", "Cajero", "Cliente", "DNI",
                                 "Estado", "Metodo de pago", "Total (S/)"};
            Row cabecera = hoja.createRow(0);
            for (int i = 0; i < columnas.length; i++) {
                Cell celda = cabecera.createCell(i);
                celda.setCellValue(columnas[i]);
                celda.setCellStyle(estiloCabecera);
            }

            int fila = 1;
            double total = 0;
            for (VentaDTO v : ventas) {
                Row r = hoja.createRow(fila++);
                r.createCell(0).setCellValue(v.getIdVenta());
                r.createCell(1).setCellValue(v.getFecha() == null ? "" : v.getFecha());
                r.createCell(2).setCellValue(v.getUsuario() == null ? "" : v.getUsuario().getNombre());
                r.createCell(3).setCellValue(v.getNombreCliente() == null ? "" : v.getNombreCliente());
                r.createCell(4).setCellValue(v.getDniCliente() == null ? "" : v.getDniCliente());
                r.createCell(5).setCellValue(v.getEstado() == null ? "" : v.getEstado());
                r.createCell(6).setCellValue(v.getMetodoPago() == null ? "" : v.getMetodoPago());
                r.createCell(7).setCellValue(v.getTotal());
                if ("VIGENTE".equalsIgnoreCase(v.getEstado())) {
                    total += v.getTotal();
                }
            }

            Row filaTotal = hoja.createRow(fila);
            filaTotal.createCell(6).setCellValue("TOTAL (ventas vigentes)");
            filaTotal.createCell(7).setCellValue(total);
            filaTotal.getCell(6).setCellStyle(estiloCabecera);

            for (int i = 0; i < columnas.length; i++) {
                hoja.autoSizeColumn(i);
            }

            resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            resp.setHeader("Content-Disposition",
                    "attachment; filename=\"reporte_ventas_" + desde + "_" + hasta + ".xlsx\"");
            try (OutputStream salida = resp.getOutputStream()) {
                libro.write(salida);
            }
        }
    }

    /** Devuelve el valor o un valor por defecto si viene vacio. */
    private String valor(String v, String porDefecto) {
        return (v == null || v.isBlank()) ? porDefecto : v.trim();
    }
}
