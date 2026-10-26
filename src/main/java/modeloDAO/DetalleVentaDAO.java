package modeloDAO;

import dto.DetalleVentaDTO;
import interfaces.DetalleVentaInterface;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import modelo.DetalleVenta;
import modelo.Producto;
import util.Conexion;

/**
 * DAO (Data Access Object) de la entidad DetalleVenta (tabla "detalle_venta").
 *
 * CAPA DE DATOS: cada linea de una venta (un producto vendido, con su
 * cantidad y subtotal) es un "detalle de venta".
 *
 * listarPorVenta(idVenta) es el metodo clave para reportes:
 * devuelve los detalles de UNA venta, ya convertidos a DetalleVentaDTO,
 * con el Producto completo de cada linea.
 */
public class DetalleVentaDAO implements DetalleVentaInterface {

    @Override
    public void agregar(DetalleVenta detalle) {
        String sql = "INSERT INTO detalle_venta(id_venta, id_producto, cantidad, precio_unitario, subtotal) VALUES(?,?,?,?,?)";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, detalle.getIdVenta());
            ps.setInt(2, detalle.getIdProducto());
            ps.setInt(3, detalle.getCantidad());
            ps.setDouble(4, detalle.getPrecioUnitario());
            ps.setDouble(5, detalle.getSubtotal());
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(DetalleVentaDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void modificar(DetalleVenta detalle) {
        String sql = "UPDATE detalle_venta SET id_producto=?, cantidad=?, precio_unitario=?, subtotal=? WHERE id_detalle=?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, detalle.getIdProducto());
            ps.setInt(2, detalle.getCantidad());
            ps.setDouble(3, detalle.getPrecioUnitario());
            ps.setDouble(4, detalle.getSubtotal());
            ps.setInt(5, detalle.getIdDetalle());
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(DetalleVentaDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void eliminar(int id) {
        String sql = "DELETE FROM detalle_venta WHERE id_detalle=?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(DetalleVentaDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public ArrayList<DetalleVenta> listar() {
        ArrayList<DetalleVenta> lista = new ArrayList<>();
        String sql = "SELECT * FROM detalle_venta";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                DetalleVenta d = new DetalleVenta();
                d.setIdDetalle(rs.getInt("id_detalle"));
                d.setIdVenta(rs.getInt("id_venta"));
                d.setIdProducto(rs.getInt("id_producto"));
                d.setCantidad(rs.getInt("cantidad"));
                d.setPrecioUnitario(rs.getDouble("precio_unitario"));
                d.setSubtotal(rs.getDouble("subtotal"));
                lista.add(d);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DetalleVentaDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return lista;
    }

    @Override
    public DetalleVenta buscar(int id) {
        DetalleVenta d = null;
        String sql = "SELECT * FROM detalle_venta WHERE id_detalle=?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    d = new DetalleVenta();
                    d.setIdDetalle(rs.getInt("id_detalle"));
                    d.setIdVenta(rs.getInt("id_venta"));
                    d.setIdProducto(rs.getInt("id_producto"));
                    d.setCantidad(rs.getInt("cantidad"));
                    d.setPrecioUnitario(rs.getDouble("precio_unitario"));
                    d.setSubtotal(rs.getDouble("subtotal"));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DetalleVentaDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return d; // null si no existe.
    }

    @Override
    public ArrayList<DetalleVentaDTO> listarPorVenta(int idVenta) {
        ArrayList<DetalleVentaDTO> lista = new ArrayList<>();
        String sql = "SELECT id_detalle, id_producto, cantidad, precio_unitario, subtotal "
                + "FROM detalle_venta WHERE id_venta=?";

        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idVenta);

            // Necesitamos el Producto de cada detalle, lo buscamos con su DAO.
            ProductoDAO productoDAO = new ProductoDAO();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idProducto = rs.getInt("id_producto");
                    Producto p = productoDAO.buscar(idProducto);

                    // FLUJO: fila de detalle + producto completo -> DetalleVentaDTO
                    DetalleVentaDTO dto = new DetalleVentaDTO(
                            rs.getInt("id_detalle"),
                            p,                                  // Producto completo
                            rs.getInt("cantidad"),
                            rs.getDouble("subtotal")
                    );
                    lista.add(dto);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DetalleVentaDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return lista;
    }
}
