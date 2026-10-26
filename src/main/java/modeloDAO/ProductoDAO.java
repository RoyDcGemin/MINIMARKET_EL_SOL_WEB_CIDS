package modeloDAO;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dto.ProductoDTO;
import interfaces.ProductoInterface;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import modelo.Categoria;
import modelo.Producto;
import util.Conexion;

/**
 * DAO (Data Access Object) de la entidad Producto.
 *
 * CAPA DE DATOS: unica clase que conoce SQL de la tabla "producto".
 *
 * Ademas del CRUD basico, expone:
 * - listarDTO(), que devuelve productos con su CATEGORIA completa
 *   usando un JOIN (patron DTO + SQL relacional).
 * - buscarPorCodigo(codigo), que localiza un producto por su codigo
 *   digitado en caja (emulacion del scanner, sin hardware).
 * - buscarPorPrincipioActivo(texto), busqueda por nombre generico
 *   (el cliente del minimarket suele pedir por principio activo).
 * - listarStockBajo(), productos con stock <= stock_minimo.
 */
public class ProductoDAO implements ProductoInterface {

    @Override
    public void agregar(Producto p) {
        String sql = "INSERT INTO producto (codigo, nombre, principio_activo, precio, costo, stock, stock_minimo, id_categoria, activo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1)";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getCodigo());
            ps.setString(2, p.getNombre());
            ps.setString(3, p.getPrincipioActivo());
            ps.setDouble(4, p.getPrecio());
            ps.setDouble(5, p.getCosto());
            ps.setInt(6, p.getStock());
            ps.setInt(7, p.getStockMinimo());
            ps.setInt(8, p.getIdCategoria());
            ps.executeUpdate();
        } catch (SQLException ex) {
            LoggerFactory.getLogger(ProductoDAO.class).error("Error de base de datos", ex);
        }
    }

    @Override
    public void modificar(Producto p) {
        String sql = "UPDATE producto SET codigo=?, nombre=?, principio_activo=?, precio=?, costo=?, stock=?, stock_minimo=?, id_categoria=? WHERE id_producto=?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getCodigo());
            ps.setString(2, p.getNombre());
            ps.setString(3, p.getPrincipioActivo());
            ps.setDouble(4, p.getPrecio());
            ps.setDouble(5, p.getCosto());
            ps.setInt(6, p.getStock());
            ps.setInt(7, p.getStockMinimo());
            ps.setInt(8, p.getIdCategoria());
            ps.setInt(9, p.getIdProducto());
            ps.executeUpdate();
        } catch (SQLException ex) {
            LoggerFactory.getLogger(ProductoDAO.class).error("Error de base de datos", ex);
        }
    }

    /**
     * ELIMINACION FISICA: borra la fila con DELETE.
     * Solo se permite si el producto nunca se vendio (lo valida el controlador).
     */
    @Override
    public void eliminar(int id) {
        String sql = "DELETE FROM producto WHERE id_producto=?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            LoggerFactory.getLogger(ProductoDAO.class).error("Error de base de datos", ex);
        }
    }

    /**
     * ELIMINACION LOGICA: marca el registro como inactivo con un UPDATE
     * (la fila NO se borra). Es la eliminacion recomendada para el catalogo.
     */
    @Override
    public void desactivar(int id) {
        String sql = "UPDATE producto SET activo=0 WHERE id_producto=?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            LoggerFactory.getLogger(ProductoDAO.class).error("Error de base de datos", ex);
        }
    }

    /** Revierte la eliminacion logica (activo = 1). */
    @Override
    public void activar(int id) {
        String sql = "UPDATE producto SET activo=1 WHERE id_producto=?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            LoggerFactory.getLogger(ProductoDAO.class).error("Error de base de datos", ex);
        }
    }

    /** Registros con eliminacion logica (activo = 0) para el reporte de auditoria. */
    @Override
    public ArrayList<Producto> listarInactivos() {
        ArrayList<Producto> lista = new ArrayList<>();
        String sql = "SELECT * FROM producto WHERE activo=0 ORDER BY id_producto";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException ex) {
            LoggerFactory.getLogger(ProductoDAO.class).error("Error de base de datos", ex);
        }
        return lista;
    }

    @Override
    public ArrayList<Producto> listar() {
        ArrayList<Producto> lista = new ArrayList<>();
        // Consulta de UNA tabla: solo los productos vigentes.
        String sql = "SELECT * FROM producto WHERE activo=1 ORDER BY id_producto";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException ex) {
            LoggerFactory.getLogger(ProductoDAO.class).error("Error de base de datos", ex);
        }
        return lista;
    }

    @Override
    public Producto buscar(int id) {
        Producto p = null;
        String sql = "SELECT * FROM producto WHERE id_producto=?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    p = mapear(rs);
                }
            }
        } catch (SQLException ex) {
            LoggerFactory.getLogger(ProductoDAO.class).error("Error de base de datos", ex);
        }
        return p; // null si no existe.
    }

    /** Convierte la fila actual del ResultSet en un objeto Producto. */
    private Producto mapear(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setIdProducto(rs.getInt("id_producto"));
        p.setCodigo(rs.getString("codigo"));
        p.setNombre(rs.getString("nombre"));
        try {
            p.setPrincipioActivo(rs.getString("principio_activo"));
        } catch (SQLException ex) {
            p.setPrincipioActivo("");
        }
        p.setPrecio(rs.getDouble("precio"));
        try {
            p.setCosto(rs.getDouble("costo"));
        } catch (SQLException ex) {
            p.setCosto(0);
        }
        p.setStock(rs.getInt("stock"));
        try {
            p.setStockMinimo(rs.getInt("stock_minimo"));
        } catch (SQLException ex) {
            p.setStockMinimo(5);
        }
        try {
            p.setActivo(rs.getInt("activo"));
        } catch (SQLException ex) {
            p.setActivo(1);
        }
        p.setIdCategoria(rs.getInt("id_categoria"));
        return p;
    }

    /**
     * Busca un producto por su codigo digitado (el que el cajero escribe
     * en la venta para emular el scanner).
     *
     * @param codigo codigo tal como se digito (se compara exacto)
     * @return el Producto, o null si no existe ese codigo
     */
    public Producto buscarPorCodigo(String codigo) {
        Producto p = null;
        String sql = "SELECT * FROM producto WHERE codigo=?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    p = mapear(rs);
                }
            }
        } catch (SQLException ex) {
            LoggerFactory.getLogger(ProductoDAO.class).error("Error de base de datos", ex);
        }
        return p; // null si no existe.
    }

    /**
     * Busca productos por principio activo o nombre comercial (LIKE).
     * El cliente del minimarket suele pedir por principio activo y no por marca.
     */
    public ArrayList<Producto> buscarPorPrincipioActivo(String texto) {
        ArrayList<Producto> lista = new ArrayList<>();
        String sql = "SELECT * FROM producto WHERE principio_activo LIKE ? OR nombre LIKE ? LIMIT 20";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String patron = "%" + (texto == null ? "" : texto.trim()) + "%";
            ps.setString(1, patron);
            ps.setString(2, patron);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException ex) {
            LoggerFactory.getLogger(ProductoDAO.class).error("Error de base de datos", ex);
        }
        return lista;
    }

    /**
     * Productos con stock en nivel critico (stock <= stock_minimo).
     * Sostiene el panel "Stock bajo" del Minimarket.
     */
    public ArrayList<ProductoDTO> listarStockBajo() {
        ArrayList<ProductoDTO> todos = listarDTO();
        ArrayList<ProductoDTO> bajos = new ArrayList<>();
        for (ProductoDTO dto : todos) {
            if (dto.getStock() <= dto.getStockMinimo()) {
                bajos.add(dto);
            }
        }
        return bajos;
    }

    /**
     * Cuenta cuantas ventas incluyen un producto.
     * Se usa para impedir borrar un producto que ya se ha vendido.
     */
    public int usadoEnVentas(int idProducto) {
        String sql = "SELECT COUNT(*) AS total FROM detalle_venta WHERE id_producto=?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        } catch (SQLException ex) {
            LoggerFactory.getLogger(ProductoDAO.class).error("Error de base de datos", ex);
        }
        return 0;
    }

    @Override
    public ArrayList<ProductoDTO> listarDTO() {
        ArrayList<ProductoDTO> lista = new ArrayList<>();

        // CONSULTA DE DOS TABLAS (JOIN): producto + categoria en una sola consulta.
        String sql = "SELECT p.id_producto, p.codigo, p.nombre, p.principio_activo, p.precio, p.costo, p.stock, p.stock_minimo, p.activo, "
                + "c.id_categoria, c.descripcion AS nombreCategoria "
                + "FROM producto p "
                + "INNER JOIN categoria c ON p.id_categoria = c.id_categoria "
                + "WHERE p.activo = 1 "
                + "ORDER BY p.id_producto";

        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                // Construimos un ProductoDTO que trae los datos del producto
                // + un objeto Categoria completo (patron DTO = dato compuesto).
                ProductoDTO dto = new ProductoDTO();
                dto.setIdProducto(rs.getInt("id_producto"));
                dto.setCodigo(rs.getString("codigo"));
                dto.setNombre(rs.getString("nombre"));
                try {
                    dto.setPrincipioActivo(rs.getString("principio_activo"));
                } catch (SQLException ex) {
                    dto.setPrincipioActivo("");
                }
                dto.setPrecio(rs.getDouble("precio"));
                try {
                    dto.setCosto(rs.getDouble("costo"));
                } catch (SQLException ex) {
                    dto.setCosto(0);
                }
                dto.setStock(rs.getInt("stock"));
                try {
                    dto.setStockMinimo(rs.getInt("stock_minimo"));
                } catch (SQLException ex) {
                    dto.setStockMinimo(5);
                }
                try {
                    dto.setActivo(rs.getInt("activo"));
                } catch (SQLException ex) {
                    dto.setActivo(1);
                }

                Categoria categoria = new Categoria();
                categoria.setIdCategoria(rs.getInt("id_categoria"));
                categoria.setDescripcion(rs.getString("nombreCategoria"));

                dto.setCategoria(categoria);
                lista.add(dto);
            }
        } catch (SQLException ex) {
            LoggerFactory.getLogger(ProductoDAO.class).error("Error de base de datos", ex);
        }
        return lista;
    }
}
