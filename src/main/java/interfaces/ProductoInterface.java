package interfaces;

import dto.ProductoDTO;
import java.util.ArrayList;
import modelo.Producto;

/**
 * Interfaz de Producto.
 * Ademas del CRUD<Producto> normal, agrega un metodo propio:
 * listarProductoDTO() devuelve productos "enriquecidos" con el nombre
 * de su categoria (ProductoDTO = Producto + Categoria).
 * ProductoDAO la implementa.
 */
public interface ProductoInterface extends CRUD<Producto> {
    public ArrayList<ProductoDTO> listarDTO();

    /** ELIMINACION LOGICA: UPDATE producto SET activo = 0 (no borra la fila). */
    public void desactivar(int id);

    /** Revierte la eliminacion logica (activo = 1). */
    public void activar(int id);

    /** Registros con eliminacion logica, para el reporte de auditoria. */
    public ArrayList<Producto> listarInactivos();
}
