package interfaces;

import modelo.DetalleVenta;
import dto.DetalleVentaDTO;
import java.util.ArrayList;

/**
 * Interfaz de DetalleVenta.
 * Ademas del CRUD<DetalleVenta> normal, agrega:
 * listarPorVenta(idVenta): devuelve los detalles de una
 * venta con el nombre del producto (DetalleVentaDTO).
 * DetalleVentaDAO la implementa.
 */
public interface DetalleVentaInterface extends CRUD<DetalleVenta> {
    public ArrayList<DetalleVentaDTO> listarPorVenta(int idVenta);
}
