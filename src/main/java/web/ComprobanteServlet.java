package web;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import controller.VentaController;
import dto.DetalleVentaDTO;
import dto.VentaDTO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Descarga del COMPROBANTE como archivo PDF (/comprobante?id=N).
 *
 * Reemplaza al TXT del proyecto base: el PDF se genera al vuelo con
 * OpenPDF a partir de la base de datos y se envia como descarga, con
 * formato (cabecera de la minimarket, detalle, totales y pie).
 */
@WebServlet(name = "ComprobanteServlet", urlPatterns = {"/comprobante"})
public class ComprobanteServlet extends HttpServlet {

    private final VentaController ventaController = new VentaController();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        int idVenta;
        try {
            idVenta = Integer.parseInt(req.getParameter("id"));
        } catch (NumberFormatException ex) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Id de venta no valido");
            return;
        }

        VentaDTO venta = null;
        for (VentaDTO v : ventaController.listarVentasConDetalles()) {
            if (v.getIdVenta() == idVenta) {
                venta = v;
                break;
            }
        }

        if (venta == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Venta no encontrada");
            return;
        }

        // Cabecera de descarga.
        resp.setContentType("application/pdf");
        resp.setHeader("Content-Disposition",
                "attachment; filename=\"comprobante_venta_" + venta.getIdVenta() + ".pdf\"");

        try {
            Document doc = new Document();
            PdfWriter.getInstance(doc, resp.getOutputStream());
            doc.open();

            Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font subtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10);

            Paragraph cabecera = new Paragraph("MINIMARKET EL SOL\n"
                    + "Av. Los Alamos 123 - Surco, Lima\n"
                    + "COMPROBANTE DE VENTA N° " + venta.getIdVenta(), titulo);
            cabecera.setAlignment(Element.ALIGN_CENTER);
            doc.add(cabecera);
            doc.add(new Paragraph(" "));

            String cajero = (venta.getUsuario() != null) ? venta.getUsuario().getNombre() : "-";
            doc.add(new Paragraph("Fecha: " + venta.getFecha()
                    + "    Cajero: " + cajero, normal));
            doc.add(new Paragraph("Cliente DNI: " + venta.getDniCliente()
                    + "    Nombre: " + venta.getNombreCliente(), normal));
            doc.add(new Paragraph("Metodo de pago: " + venta.getMetodoPago()
                    + "    Estado: " + venta.getEstado(), normal));
            doc.add(new Paragraph(" "));

            PdfPTable tabla = new PdfPTable(4);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[]{45, 15, 20, 20});
            agregarCeldaCabecera(tabla, "Producto");
            agregarCeldaCabecera(tabla, "Cant.");
            agregarCeldaCabecera(tabla, "P. Unit.");
            agregarCeldaCabecera(tabla, "Subtotal");
            for (DetalleVentaDTO d : venta.getDetalles()) {
                tabla.addCell(new Phrase(d.getProducto().getNombre(), normal));
                tabla.addCell(new Phrase(String.valueOf(d.getCantidad()), normal));
                tabla.addCell(new Phrase(WebUtil.formatearMonto(d.getProducto().getPrecio()), normal));
                tabla.addCell(new Phrase(WebUtil.formatearMonto(d.getSubtotal()), normal));
            }
            doc.add(tabla);
            doc.add(new Paragraph(" "));

            Paragraph totales = new Paragraph(
                    "Descuento: " + WebUtil.formatearMonto(venta.getDescuento()) + "\n"
                    + "TOTAL: " + WebUtil.formatearMonto(venta.getTotal()), subtitulo);
            totales.setAlignment(Element.ALIGN_RIGHT);
            doc.add(totales);
            doc.add(new Paragraph(" "));

            Paragraph pie = new Paragraph("Gracias por su compra", normal);
            pie.setAlignment(Element.ALIGN_CENTER);
            doc.add(pie);

            doc.close();
        } catch (Exception ex) {
            throw new IOException("Error al generar el PDF: " + ex.getMessage(), ex);
        }
    }

    /** Celda de cabecera de la tabla del detalle (fondo gris, centrada). */
    private void agregarCeldaCabecera(PdfPTable tabla, String texto) {
        Font f = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        PdfPCell celda = new PdfPCell(new Phrase(texto, f));
        celda.setHorizontalAlignment(Element.ALIGN_CENTER);
        celda.setGrayFill(0.85f);
        tabla.addCell(celda);
    }
}
