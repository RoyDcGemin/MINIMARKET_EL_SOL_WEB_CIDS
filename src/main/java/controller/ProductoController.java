package controller;

import dto.ProductoDTO;
import java.util.ArrayList;
import modelo.Producto;
import modeloDAO.ProductoDAO;

/**
 * Controlador de la entidad Producto.
 *
 * CAPA INTERMEDIA (logica): recibe los datos de la vista (nombre, precio,
 * stock, categoria), los VALIDA y delega en el DAO. Expone listarDTO()
 * para que la tabla de la vista muestre el producto con su categoria.
 *
 * FLUJO: Vista -> ProductoController (valida) -> ProductoDAO -> Base de datos
 *                                     y de vuelta con los objetos.
 */
public class ProductoController {

    private final ProductoDAO productoDAO = new ProductoDAO();

    public ArrayList<Producto> listar() {
        return productoDAO.listar();
    }

    public Producto buscar(int id) {
        return productoDAO.buscar(id);
    }

    public ArrayList<ProductoDTO> listarDTO() {
        return productoDAO.listarDTO();
    }

    public Producto buscarPorCodigo(String codigo) {
        if (codigo == null) {
            return null;
        }
        return productoDAO.buscarPorCodigo(codigo.trim());
    }

    public ArrayList<Producto> buscarPorPrincipioActivo(String texto) {
        return productoDAO.buscarPorPrincipioActivo(texto);
    }

    public ArrayList<ProductoDTO> listarStockBajo() {
        return productoDAO.listarStockBajo();
    }

    /**
     * Agrega un producto farmaceutico nuevo.
     *
     * @param codigo codigo digitado en caja (unico, emula el scanner)
     * @param nombre nombre comercial del producto
     * @param principioActivo nombre generico (ej. "Paracetamol")
     * @param precio precio unitario
     * @param stock cantidad en inventario
     * @param stockMinimo umbral de alerta de reposicion
     * @param idCategoria id de la categoria elegida
     * @throws ValidacionException si algun dato no es valido
     */
    public void agregar(String codigo, String nombre, String principioActivo, double precio, double costo, int stock, int stockMinimo, int idCategoria) throws ValidacionException {
        validar(codigo, nombre, precio, costo, stock, stockMinimo, idCategoria, 0);

        Producto p = new Producto();
        p.setCodigo(codigo.trim());
        p.setNombre(nombre.trim());
        p.setPrincipioActivo(principioActivo == null ? "" : principioActivo.trim());
        p.setPrecio(precio);
        p.setCosto(costo);
        p.setStock(stock);
        p.setStockMinimo(stockMinimo);
        p.setIdCategoria(idCategoria);
        productoDAO.agregar(p);
    }

    /**
     * Modifica un producto existente.
     *
     * @param idProducto id del producto que se edita
     */
    public void modificar(int idProducto, String codigo, String nombre, String principioActivo, double precio, double costo, int stock, int stockMinimo, int idCategoria) throws ValidacionException {
        validar(codigo, nombre, precio, costo, stock, stockMinimo, idCategoria, idProducto);

        Producto p = new Producto();
        p.setIdProducto(idProducto);
        p.setCodigo(codigo.trim());
        p.setNombre(nombre.trim());
        p.setPrincipioActivo(principioActivo == null ? "" : principioActivo.trim());
        p.setPrecio(precio);
        p.setCosto(costo);
        p.setStock(stock);
        p.setStockMinimo(stockMinimo);
        p.setIdCategoria(idCategoria);
        productoDAO.modificar(p);
    }

    /**
     * Elimina un producto, pero solo si no se ha vendido antes.
     * Es la ELIMINACION FISICA (DELETE). Para el catalogo se recomienda
     * desactivar() (eliminacion logica).
     *
     * @param idProducto id del producto a eliminar
     * @throws ValidacionException si el producto aparece en alguna venta
     */
    public void eliminar(int idProducto) throws ValidacionException {
        if (productoDAO.usadoEnVentas(idProducto) > 0) {
            throw new ValidacionException(
                    "No se puede eliminar: el producto ya aparece en ventas. Use Desactivar.");
        }
        productoDAO.eliminar(idProducto);
    }

    /** ELIMINACION LOGICA: marca el producto como inactivo (UPDATE). */
    public void desactivar(int idProducto) throws ValidacionException {
        if (idProducto <= 0) {
            throw new ValidacionException("Seleccione un producto válido");
        }
        productoDAO.desactivar(idProducto);
    }

    /** Reactiva un producto previamente desactivado. */
    public void activar(int idProducto) throws ValidacionException {
        if (idProducto <= 0) {
            throw new ValidacionException("Seleccione un producto válido");
        }
        productoDAO.activar(idProducto);
    }

    /** Productos con eliminacion logica (para el reporte de auditoria). */
    public ArrayList<Producto> listarInactivos() {
        return productoDAO.listarInactivos();
    }

    /**
     * Validacion centralizada de los campos del producto.
     * Se aplica en el servidor antes de llegar a la base de datos.
     */
    private void validar(String codigo, String nombre, double precio, double costo, int stock, int stockMinimo, int idCategoria, int idProducto) throws ValidacionException {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new ValidacionException("Ingrese el codigo del producto");
        }
        Producto otro = productoDAO.buscarPorCodigo(codigo.trim());
        if (otro != null && otro.getIdProducto() != idProducto) {
            throw new ValidacionException("El codigo ya existe en otro producto");
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ValidacionException("Ingrese el nombre del producto");
        }
        if (precio <= 0) {
            throw new ValidacionException("El precio debe ser mayor a 0");
        }
        if (costo < 0) {
            throw new ValidacionException("El costo no puede ser negativo");
        }
        if (stock < 0) {
            throw new ValidacionException("El stock no puede ser negativo");
        }
        if (stockMinimo < 0) {
            throw new ValidacionException("El stock minimo no puede ser negativo");
        }
        if (idCategoria <= 0) {
            throw new ValidacionException("Seleccione una categoría");
        }
    }
}