package controller;

import dto.ProductoDTO;
import java.util.ArrayList;
import modelo.Producto;
import modeloDAO.CategoriaDAO;
import modeloDAO.ProductoDAO;
import org.apache.commons.lang3.StringUtils;

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

    /** Rango de valores aceptables (ver informe: validacion tipo/longitud/rango). */
    private static final int CODIGO_MIN = 3, CODIGO_MAX = 30;
    private static final int NOMBRE_MIN = 3, NOMBRE_MAX = 100;
    private static final int DESCRIPCION_MAX = 100;
    /** DECIMAL(10,2): maximo 99,999,999.99 */
    private static final double MONTO_MAXIMO = 9999999.99;
    private static final int STOCK_MAXIMO = 1000000;

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

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

    /** Busqueda por texto (descripcion o nombre). Consulta de UNA tabla. */
    public ArrayList<Producto> buscarPorTexto(String texto) {
        return productoDAO.buscarPorTexto(texto);
    }

    public ArrayList<ProductoDTO> listarStockBajo() {
        return productoDAO.listarStockBajo();
    }

    /**
     * Agrega un producto nuevo del minimarket.
     *
     * @param codigo codigo digitado en caja (unico, emula el scanner)
     * @param nombre nombre comercial del producto
     * @param descripcion presentacion del producto (ej. "Bolsa de 1 kg")
     * @param precio precio unitario
     * @param costo precio de compra
     * @param stock cantidad en inventario
     * @param stockMinimo umbral de alerta de reposicion
     * @param idCategoria id de la categoria elegida
     * @throws ValidacionException si algun dato no es valido
     */
    public void agregar(String codigo, String nombre, String descripcion, double precio, double costo, int stock, int stockMinimo, int idCategoria) throws ValidacionException {
        validar(codigo, nombre, descripcion, precio, costo, stock, stockMinimo, idCategoria, 0);

        Producto p = new Producto();
        p.setCodigo(codigo.trim());
        p.setNombre(nombre.trim());
        p.setDescripcion(descripcion == null ? "" : descripcion.trim());
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
    public void modificar(int idProducto, String codigo, String nombre, String descripcion, double precio, double costo, int stock, int stockMinimo, int idCategoria) throws ValidacionException {
        validar(codigo, nombre, descripcion, precio, costo, stock, stockMinimo, idCategoria, idProducto);

        Producto p = new Producto();
        p.setIdProducto(idProducto);
        p.setCodigo(codigo.trim());
        p.setNombre(nombre.trim());
        p.setDescripcion(descripcion == null ? "" : descripcion.trim());
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
        if (idProducto <= 0) {
            throw new ValidacionException("Seleccione un producto válido");
        }
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
     * Se aplica en el servidor antes de llegar a la base de datos y cubre:
     *   - TIPO: se exige texto/numero (el servlet no acepta letras en numericos).
     *   - LONGITUD: limites coherentes con las columnas de la tabla.
     *   - RANGO: valores minimos y maximos aceptables.
     */
    private void validar(String codigo, String nombre, String descripcion,
                         double precio, double costo, int stock, int stockMinimo,
                         int idCategoria, int idProducto) throws ValidacionException {

        // Apache Commons Lang: StringUtils.isBlank simplifica la validacion de texto.
        if (StringUtils.isBlank(codigo)) {
            throw new ValidacionException("Ingrese el codigo del producto");
        }
        codigo = codigo.trim();
        if (codigo.length() < CODIGO_MIN || codigo.length() > CODIGO_MAX) {
            throw new ValidacionException(
                    "El codigo debe tener entre " + CODIGO_MIN + " y " + CODIGO_MAX + " caracteres");
        }
        if (!codigo.matches("[A-Za-z0-9._-]+")) {
            throw new ValidacionException("El codigo solo puede tener letras, numeros, punto, guion o guion bajo");
        }
        Producto otro = productoDAO.buscarPorCodigo(codigo);
        if (otro != null && otro.getIdProducto() != idProducto) {
            throw new ValidacionException("El codigo ya existe en otro producto");
        }

        if (StringUtils.isBlank(nombre)) {
            throw new ValidacionException("Ingrese el nombre del producto");
        }
        if (nombre.trim().length() < NOMBRE_MIN || nombre.trim().length() > NOMBRE_MAX) {
            throw new ValidacionException(
                    "El nombre debe tener entre " + NOMBRE_MIN + " y " + NOMBRE_MAX + " caracteres");
        }

        if (descripcion != null && descripcion.trim().length() > DESCRIPCION_MAX) {
            throw new ValidacionException(
                    "La descripcion no puede superar " + DESCRIPCION_MAX + " caracteres");
        }

        if (precio <= 0) {
            throw new ValidacionException("El precio debe ser mayor a 0");
        }
        if (precio > MONTO_MAXIMO) {
            throw new ValidacionException("El precio no puede superar S/. " + MONTO_MAXIMO);
        }
        if (costo < 0) {
            throw new ValidacionException("El costo no puede ser negativo");
        }
        if (costo > MONTO_MAXIMO) {
            throw new ValidacionException("El costo no puede superar S/. " + MONTO_MAXIMO);
        }

        if (stock < 0) {
            throw new ValidacionException("El stock no puede ser negativo");
        }
        if (stock > STOCK_MAXIMO) {
            throw new ValidacionException("El stock no puede superar " + STOCK_MAXIMO);
        }
        if (stockMinimo < 0) {
            throw new ValidacionException("El stock minimo no puede ser negativo");
        }
        if (stockMinimo > STOCK_MAXIMO) {
            throw new ValidacionException("El stock minimo no puede superar " + STOCK_MAXIMO);
        }
        // Stock minimo 0 no tiene sentido operativo: se pide al menos 1.
        if (stockMinimo <= 0) {
            throw new ValidacionException("El stock minimo debe ser al menos 1 (umbral de reposicion)");
        }

        if (idCategoria <= 0) {
            throw new ValidacionException("Seleccione una categoría");
        }
        if (categoriaDAO.buscar(idCategoria) == null) {
            throw new ValidacionException("La categoría seleccionada no existe");
        }
    }
}
