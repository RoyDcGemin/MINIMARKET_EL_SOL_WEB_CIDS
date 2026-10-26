package modeloDAO;

import interfaces.UsuarioInterface;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import modelo.Usuario;
import util.Conexion;
import util.HashUtil;

/**
 * DAO (Data Access Object) de la entidad Usuario.
 *
 * CAPA DE DATOS: única clase que conoce SQL de la tabla "usuario".
 *
 * SEGURIDAD: la contrasenia se guarda hasheada (SHA-256) y NUNCA en texto
 * plano. Al agregar/modificar se hashea; al buscar se devuelve el hash
 * guardado para que el controlador pueda compararlo con el hash de lo
 * que el usuario escribió (ver controller.UsuarioController).
 */
public class UsuarioDAO implements UsuarioInterface {

    @Override
    public void agregar(Usuario u) {
        String sql = "INSERT INTO usuario (usuario, clave, nombre, rol) VALUES (?, ?, ?, ?)";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getUsuario());
            ps.setString(2, HashUtil.sha256(u.getClave())); // se guarda el HASH
            ps.setString(3, u.getNombre());
            ps.setString(4, u.getRol());
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(UsuarioDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void modificar(Usuario u) {
        String sql = "UPDATE usuario SET usuario=?, clave=?, nombre=?, rol=? WHERE id_usuario=?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getUsuario());
            ps.setString(2, HashUtil.sha256(u.getClave()));
            ps.setString(3, u.getNombre());
            ps.setString(4, u.getRol());
            ps.setInt(5, u.getIdusuario());
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(UsuarioDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void eliminar(int id) {
        String sql = "DELETE FROM usuario WHERE id_usuario=?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(UsuarioDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public ArrayList<Usuario> listar() {
        ArrayList<Usuario> vu = new ArrayList<>();
        String sql = "SELECT * FROM usuario";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Usuario u = new Usuario();
                u.setIdusuario(rs.getInt("id_usuario"));
                u.setUsuario(rs.getString("usuario"));
                u.setClave(rs.getString("clave"));
                u.setNombre(rs.getString("nombre"));
                u.setRol(rs.getString("rol"));
                vu.add(u);
            }
        } catch (SQLException ex) {
            Logger.getLogger(UsuarioDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return vu;
    }

    @Override
    public Usuario buscar(int id) {
        Usuario u = null;
        String sql = "SELECT * FROM usuario WHERE id_usuario=?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    u = new Usuario();
                    u.setIdusuario(rs.getInt("id_usuario"));
                    u.setUsuario(rs.getString("usuario"));
                    u.setClave(rs.getString("clave"));
                    u.setNombre(rs.getString("nombre"));
                    u.setRol(rs.getString("rol"));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UsuarioDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return u; // null si no existe.
    }

    @Override
    public Usuario buscarPorNombre(String nombreUsuario) {
        Usuario u = null;
        String sql = "SELECT * FROM usuario WHERE usuario = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombreUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                // Si existe un usuario con ese nombre, se convierte la fila a objeto.
                if (rs.next()) {
                    u = new Usuario();
                    u.setIdusuario(rs.getInt("id_usuario"));
                    u.setUsuario(rs.getString("usuario"));
                    u.setClave(rs.getString("clave")); // aquí viene el HASH guardado
                    u.setNombre(rs.getString("nombre"));
                    u.setRol(rs.getString("rol"));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UsuarioDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return u; // null si el usuario no existe en la BD.
    }
}