package modeloDAO;

import dto.DetalleVentaDTO;
import dto.VentaDTO;
import interfaces.VentaInterface;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import modelo.Venta;
import util.Conexion;

/**
 * DAO (Data Access Object) de la entidad Venta (tabla "venta").
 *
 * CAPA DE DATOS: ademas del CRUD basico, contiene las operaciones
 * importantes del sistema:
 *
 * 1) guardarVentaCompleta(VentaDTO): guarda la venta + sus detalles
 *    + descuenta stock en UNA SOLA TRANSACCION (todo o nada).
 *
 * 2) listarVentasConDetalles(): devuelve cada venta ya convertida en
 *    VentaDTO, con su Usuario (cajero) y su lista de DetalleVentaDTO.
 *
 * 3) listarConFiltros(...): lo mismo pero filtrado por rango de fechas,
 *    cajero y/o producto (reportes RF07 y cierre RF06).
 */
public class VentaDAO implements VentaInterface {

    @Override
    public void agregar(Venta v) {
        String sql = "INSERT INTO venta (fecha, hora_inicio, hora_fin, total, descuento, "
                + "estado, dni_cliente, nombre_cliente, metodo_pago, id_usuario) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            parametrosVenta(ps, v);
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(VentaDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void modificar(Venta v) {
        String sql = "UPDATE venta SET fecha=?, hora_inicio=?, hora_fin=?, total=?, "
                + "descuento=?, estado=?, dni_cliente=?, nombre_cliente=?, "
                + "metodo_pago=?, id_usuario=? WHERE id_venta=?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            parametrosVenta(ps, v);
            ps.setInt(11, v.getIdVenta());
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(VentaDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /** Copia los 10 campos de la venta a la sentencia (agregar/modificar). */
    private void parametrosVenta(PreparedStatement ps, Venta v) throws SQLException {
        ps.setString(1, v.getFecha());
        ps.setString(2, v.getHoraInicio());
        ps.setString(3, v.getHoraFin());
        ps.setDouble(4, v.getTotal());
        ps.setDouble(5, v.getDescuento());
        ps.setString(6, v.getEstado());
        ps.setString(7, v.getDniCliente());
        ps.setString(8, v.getNombreCliente());
        ps.setString(9, v.getMetodoPago());
        ps.setInt(10, v.getIdUsuario());
    }

    @Override
    public void eliminar(int idVenta) {
        String sql = "DELETE FROM venta WHERE id_venta=?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(VentaDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public ArrayList<Venta> listar() {
        ArrayList<Venta> lista = new ArrayList<>();
        String sql = "SELECT * FROM venta ORDER BY id_venta DESC";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException ex) {
            Logger.getLogger(VentaDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return lista;
    }

    @Override
    public Venta buscar(int idVenta) {
        Venta v = null;
        String sql = "SELECT * FROM venta WHERE id_venta=?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    v = mapear(rs);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(VentaDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return v; // null si no existe.
    }

    /** Convierte la fila actual del ResultSet en un objeto Venta. */
    private Venta mapear(ResultSet rs) throws SQLException {
        Venta v = new Venta();
        v.setIdVenta(rs.getInt("id_venta"));
        v.setFecha(rs.getString("fecha"));
        v.setHoraInicio(rs.getString("hora_inicio"));
        v.setHoraFin(rs.getString("hora_fin"));
        v.setTotal(rs.getDouble("total"));
        v.setDescuento(rs.getDouble("descuento"));
        v.setEstado(rs.getString("estado"));
        v.setDniCliente(rs.getString("dni_cliente"));
        v.setNombreCliente(rs.getString("nombre_cliente"));
        v.setMetodoPago(rs.getString("metodo_pago"));
        v.setIdUsuario(rs.getInt("id_usuario"));
        return v;
    }

    /**
     * Marca una venta como ANULADA (solo si estaba VIGENTE).
     * Sostiene el indicador %ERR del informe.
     */
    @Override
    public void anular(int idVenta) {
        String sql = "UPDATE venta SET estado='ANULADA' WHERE id_venta=? AND estado='VIGENTE'";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(VentaDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * DNIs ya usados en ventas anteriores (distintos), para autocompletar
     * el campo DNI en la venta sin necesidad de tabla de clientes.
     * Cada elemento es {dni, nombre}.
     */
    @Override
    public ArrayList<String[]> listarDniRegistrados() {
        ArrayList<String[]> lista = new ArrayList<>();
        String sql = "SELECT DISTINCT dni_cliente, nombre_cliente FROM venta "
                + "WHERE dni_cliente IS NOT NULL AND dni_cliente <> '' "
                + "ORDER BY id_venta DESC LIMIT 60";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new String[]{rs.getString("dni_cliente"), rs.getString("nombre_cliente")});
            }
        } catch (SQLException ex) {
            Logger.getLogger(VentaDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return lista;
    }

    /**
     * Guarda una venta completa (cabecera + detalles) y descuenta el stock.
     *
     * TRANSACCION: "todo o nada". Si alguna operacion falla, se llama a
     * rollback() y la BD queda como estaba (no queda una venta "a medias"
     * ni stock descontado sin venta).
     *
     * @param ventaDTO venta con usuario y lista de detalles
     * @return el id generado de la venta, o -1 si hubo un error
     */
    @Override
    public int guardarVentaCompleta(VentaDTO ventaDTO) {
        int idGenerado = -1;
        String sqlVenta = "INSERT INTO venta (fecha, hora_inicio, hora_fin, total, descuento, "
                + "estado, dni_cliente, nombre_cliente, metodo_pago, id_usuario) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?)";
        String sqlDetalle = "INSERT INTO detalle_venta (id_venta, id_producto, cantidad, "
                + "precio_unitario, subtotal) VALUES (?,?,?,?,?)";
        // El WHERE stock >= ? evita dejar stock negativo si otra caja ya
        // vendio el mismo producto (condicion de carrera entre cajas).
        String sqlStock = "UPDATE producto SET stock = stock - ? WHERE id_producto = ? AND stock >= ?";

        try (Connection conn = Conexion.getConexion()) {

            // 1) Desactivamos el autocommit: nosotros decidimos cuando confirmar.
            conn.setAutoCommit(false);

            try {
                // 2) Insertar la cabecera de la venta y recuperar su id generado.
                try (PreparedStatement ps = conn.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, ventaDTO.getFecha());
                    ps.setString(2, ventaDTO.getHoraInicio());
                    ps.setString(3, ventaDTO.getHoraFin());
                    ps.setDouble(4, ventaDTO.getTotal());
                    ps.setDouble(5, ventaDTO.getDescuento());
                    ps.setString(6, ventaDTO.getEstado());
                    ps.setString(7, ventaDTO.getDniCliente());
                    ps.setString(8, ventaDTO.getNombreCliente());
                    ps.setString(9, ventaDTO.getMetodoPago());
                    ps.setInt(10, ventaDTO.getUsuario().getIdusuario());
                    ps.executeUpdate();

                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            idGenerado = rs.getInt(1);
                        }
                    }
                }

                // 3) Insertar cada detalle y, en la misma transaccion, descontar stock.
                try (PreparedStatement psDet = conn.prepareStatement(sqlDetalle);
                     PreparedStatement psStock = conn.prepareStatement(sqlStock)) {

                    for (DetalleVentaDTO detalle : ventaDTO.getDetalles()) {
                        psDet.setInt(1, idGenerado);
                        psDet.setInt(2, detalle.getProducto().getIdProducto());
                        psDet.setInt(3, detalle.getCantidad());
                        psDet.setDouble(4, detalle.getProducto().getPrecio());
                        psDet.setDouble(5, detalle.getSubtotal());
                        psDet.addBatch();

                        psStock.setInt(1, detalle.getCantidad());
                        psStock.setInt(2, detalle.getProducto().getIdProducto());
                        psStock.setInt(3, detalle.getCantidad());
                        psStock.addBatch();
                    }
                    // Ejecutamos todo lo acumulado con addBatch de una sola vez.
                    psDet.executeBatch();
                    int[] filas = psStock.executeBatch();
                    // Si alguna fila no se actualizo, no habia stock suficiente:
                    // se lanza la excepcion para que se ejecute el ROLLBACK.
                    for (int n : filas) {
                        if (n == 0) {
                            throw new SQLException("Stock insuficiente al confirmar la venta");
                        }
                    }
                }

                // 4) Todo OK -> confirmamos (commit).
                conn.commit();

            } catch (SQLException ex) {
                // 5) Algo fallo -> revertimos (rollback) y avisamos.
                conn.rollback();
                Logger.getLogger(VentaDAO.class.getName()).log(Level.SEVERE, "Venta NO guardada (rollback)", ex);
                idGenerado = -1;
            }

        } catch (SQLException ex) {
            Logger.getLogger(VentaDAO.class.getName()).log(Level.SEVERE, null, ex);
            idGenerado = -1;
        }
        return idGenerado;
    }

    /**
     * Devuelve todas las ventas convertidas en VentaDTO, incluyendo
     * el Usuario (cajero) y la lista de detalles de cada una.
     * Es lo que usa reportes para no depender de archivos.
     */
    @Override
    public ArrayList<VentaDTO> listarVentasConDetalles() {
        ArrayList<VentaDTO> lista = new ArrayList<>();
        String sql = "SELECT * FROM venta ORDER BY id_venta DESC";

        UsuarioDAO usuarioDAO = new UsuarioDAO();
        DetalleVentaDAO detalleDAO = new DetalleVentaDAO();

        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(hidratar(rs, usuarioDAO, detalleDAO));
            }
        } catch (SQLException ex) {
            Logger.getLogger(VentaDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return lista;
    }

    /**
     * Lo mismo que listarVentasConDetalles pero filtrado:
     * - desde/hasta: fechas "yyyy-MM-dd" (comparadas por dia).
     * - idUsuario: 0 = todos los cajeros.
     * - idProducto: 0 = todos los productos.
     * Sostiene RF07 (reportes) y RF06 (cierre de caja).
     */
    @Override
    public ArrayList<VentaDTO> listarConFiltros(String desde, String hasta, int idUsuario, int idProducto) {
        ArrayList<VentaDTO> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT v.* FROM venta v WHERE DATE(v.fecha) BETWEEN ? AND ?");
        if (idUsuario > 0) {
            sql.append(" AND v.id_usuario=?");
        }
        if (idProducto > 0) {
            sql.append(" AND EXISTS (SELECT 1 FROM detalle_venta d "
                    + "WHERE d.id_venta=v.id_venta AND d.id_producto=?)");
        }
        sql.append(" ORDER BY v.id_venta DESC");

        UsuarioDAO usuarioDAO = new UsuarioDAO();
        DetalleVentaDAO detalleDAO = new DetalleVentaDAO();

        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int i = 1;
            ps.setString(i++, desde);
            ps.setString(i++, hasta);
            if (idUsuario > 0) {
                ps.setInt(i++, idUsuario);
            }
            if (idProducto > 0) {
                ps.setInt(i++, idProducto);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(hidratar(rs, usuarioDAO, detalleDAO));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(VentaDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return lista;
    }

    /** Convierte la fila actual en VentaDTO completo (usuario + detalles). */
    private VentaDTO hidratar(ResultSet rs, UsuarioDAO usuarioDAO, DetalleVentaDAO detalleDAO)
            throws SQLException {
        VentaDTO dto = new VentaDTO();
        int idVenta = rs.getInt("id_venta");
        dto.setIdVenta(idVenta);
        dto.setFecha(rs.getString("fecha"));
        dto.setHoraInicio(rs.getString("hora_inicio"));
        dto.setHoraFin(rs.getString("hora_fin"));
        dto.setTotal(rs.getDouble("total"));
        dto.setDescuento(rs.getDouble("descuento"));
        dto.setEstado(rs.getString("estado"));
        dto.setDniCliente(rs.getString("dni_cliente"));
        dto.setNombreCliente(rs.getString("nombre_cliente"));
        dto.setMetodoPago(rs.getString("metodo_pago"));
        dto.setUsuario(usuarioDAO.buscar(rs.getInt("id_usuario")));
        dto.setDetalles(detalleDAO.listarPorVenta(idVenta));
        return dto;
    }

    /**
     * REPORTE de maximo, minimo y promedio de los totales de venta.
     * Usa funciones de agregacion SQL (MAX, MIN, AVG) sobre la tabla venta.
     *
     * @return {maximo, minimo, promedio}
     */
    @Override
    public double[] maxMinPromedio(String desde, String hasta) {
        double[] r = new double[]{0, 0, 0};
        String sql = "SELECT MAX(total) AS maximo, MIN(total) AS minimo, AVG(total) AS promedio "
                + "FROM venta WHERE DATE(fecha) BETWEEN ? AND ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, desde);
            ps.setString(2, hasta);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    r[0] = rs.getDouble("maximo");
                    r[1] = rs.getDouble("minimo");
                    r[2] = rs.getDouble("promedio");
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(VentaDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return r;
    }

    /**
     * REPORTE de ingresos, utilidad y unidades vendidas.
     * Consulta de TRES tablas con JOIN: detalle_venta + venta + producto.
     * Utilidad = cantidad * (precio_unitario - costo del producto).
     *
     * @return {ingresos, utilidad, unidades}
     */
    @Override
    public double[] ingresosUtilidad(String desde, String hasta) {
        double[] r = new double[]{0, 0, 0};
        String sql = "SELECT COALESCE(SUM(d.cantidad * d.precio_unitario),0) AS ingresos, "
                + "COALESCE(SUM(d.cantidad * (d.precio_unitario - p.costo)),0) AS utilidad, "
                + "COALESCE(SUM(d.cantidad),0) AS unidades "
                + "FROM detalle_venta d "
                + "JOIN venta v ON d.id_venta = v.id_venta "
                + "JOIN producto p ON d.id_producto = p.id_producto "
                + "WHERE DATE(v.fecha) BETWEEN ? AND ? AND v.estado = 'VIGENTE'";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, desde);
            ps.setString(2, hasta);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    r[0] = rs.getDouble("ingresos");
                    r[1] = rs.getDouble("utilidad");
                    r[2] = rs.getDouble("unidades");
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(VentaDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return r;
    }

    /**
     * Calcula el siguiente id de venta (para mostrarlo en el formulario).
     * Se usa el MAX(id_venta) por portabilidad.
     *
     * @return el siguiente id (1 si la tabla esta vacia)
     */
    public int obtenerSiguienteID() {
        int siguienteID = 1;
        String sql = "SELECT MAX(id_venta) AS maxID FROM venta";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                siguienteID = rs.getInt("maxID") + 1;
            }
        } catch (SQLException ex) {
            Logger.getLogger(VentaDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return siguienteID;
    }
}
