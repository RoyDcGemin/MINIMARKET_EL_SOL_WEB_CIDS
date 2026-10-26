package modeloDAO;

import interfaces.CategoriaInterface;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import modelo.Categoria;
import util.Conexion;

/**
 * DAO (Data Access Object) de la entidad Categoria.
 *
 * CAPA DE DATOS: es la unica clase que conoce SQL de la tabla "categoria".
 * - Recibe un objeto Categoria (o un id) y lo convierte en sentencias SQL.
 * - Devuelve objetos Categoria a partir de los registros devueltos por la BD.
 *
 * Uso de try-with-resources: garantiza que la conexion, la sentencia y el
 * result set se cierren solos al terminar el try (aunque ocurra una excepcion).
 */
public class CategoriaDAO implements CategoriaInterface {

    @Override
    public void agregar(Categoria c) {
        String sql = "INSERT INTO categoria (descripcion) VALUES (?)";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getDescripcion());
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(CategoriaDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void modificar(Categoria c) {
        String sql = "UPDATE categoria SET descripcion=? WHERE id_categoria=?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getDescripcion());
            ps.setInt(2, c.getIdCategoria());
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(CategoriaDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void eliminar(int id) {
        String sql = "DELETE FROM categoria WHERE id_categoria=?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(CategoriaDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public ArrayList<Categoria> listar() {
        ArrayList<Categoria> vc = new ArrayList<>();
        String sql = "SELECT * FROM categoria";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            // FLUJO (lectura): recorremos fila por fila el ResultSet y
            // convertimos cada fila en un objeto Categoria.
            while (rs.next()) {
                Categoria c = new Categoria();
                c.setIdCategoria(rs.getInt("id_categoria"));
                c.setDescripcion(rs.getString("descripcion"));
                vc.add(c);
            }
        } catch (SQLException ex) {
            Logger.getLogger(CategoriaDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        // Devolvemos la lista de objetos (datos listos para la capa superior).
        return vc;
    }

    @Override
    public Categoria buscar(int id) {
        Categoria c = null;
        String sql = "SELECT * FROM categoria WHERE id_categoria=?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    c = new Categoria();
                    c.setIdCategoria(rs.getInt("id_categoria"));
                    c.setDescripcion(rs.getString("descripcion"));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(CategoriaDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return c; // null si no existe.
    }

    /**
     * Cuenta cuantos productos estan asignados a una categoria.
     * Se usa para impedir el borrado de categorias en uso.
     */
    public int contarProductos(int idCategoria) {
        String sql = "SELECT COUNT(*) AS total FROM producto WHERE id_categoria=?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCategoria);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(CategoriaDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }
}