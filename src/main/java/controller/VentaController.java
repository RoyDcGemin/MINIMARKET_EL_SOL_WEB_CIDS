package controller;

import dto.DetalleVentaDTO;
import dto.VentaDTO;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import modelo.Usuario;
import modeloDAO.UsuarioDAO;
import modeloDAO.VentaDAO;

/**
 * Controlador de Ventas.
 *
 * CAPA INTERMEDIA (logica): orquesta el proceso de VENTA.
 *
 * FLUJO (venta):
 *   Vista (venta.jsp) -> VentaController.registrarVentaEnBD
 *     -> VentaDAO.guardarVentaCompleta (transaccion: venta + detalles + stock)
 *     -> Base de datos
 *
 * FLUJO (reportes):
 *   Vista (reportes.jsp) -> VentaController.listarConFiltros
 *     -> VentaDAO.listarConFiltros -> Base de datos
 *     -> devuelve List<VentaDTO> ya completas.
 */
public class VentaController {

    /** Metodos de pago aceptados en caja (RF06 pide desglose por metodo). */
    public static final String[] METODOS_PAGO = {"EFECTIVO", "YAPE", "TRANSFERENCIA"};

    private final VentaDAO ventaDAO = new VentaDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    /**
     * Devuelve el siguiente id de venta, para mostrarlo en pantalla.
     */
    public int obtenerSiguienteId() {
        return ventaDAO.obtenerSiguienteID();
    }

    /**
     * Devuelve todas las ventas con sus detalles (para reportes).
     */
    public ArrayList<VentaDTO> listarVentasConDetalles() {
        return ventaDAO.listarVentasConDetalles();
    }

    /**
     * Devuelve las ventas filtradas por fechas, cajero y/o producto.
     *
     * @param desde    fecha "yyyy-MM-dd" inicial
     * @param hasta    fecha "yyyy-MM-dd" final
     * @param idUsuario 0 = todos los cajeros
     * @param idProducto 0 = todos los productos
     */
    public ArrayList<VentaDTO> listarConFiltros(String desde, String hasta, int idUsuario, int idProducto) {
        return ventaDAO.listarConFiltros(desde, hasta, idUsuario, idProducto);
    }

    /**
     * Marca una venta como ANULADA (para el indicador %ERR).
     */
    public void anularVenta(int idVenta) throws ValidacionException {
        if (ventaDAO.buscar(idVenta) == null) {
            throw new ValidacionException("La venta no existe");
        }
        ventaDAO.anular(idVenta);
    }

    /**
     * DNIs ya usados antes (para autocompletar en la venta).
     * Cada elemento es {dni, nombre}.
     */
    public ArrayList<String[]> listarDniRegistrados() {
        return ventaDAO.listarDniRegistrados();
    }

    /**
     * REPORTE: maximo, minimo y promedio de los totales del periodo.
     *
     * @return {maximo, minimo, promedio}
     */
    public double[] maxMinPromedio(String desde, String hasta) {
        return ventaDAO.maxMinPromedio(desde, hasta);
    }

    /**
     * REPORTE: ingresos, utilidad y unidades vendidas del periodo.
     *
     * @return {ingresos, utilidad, unidades}
     */
    public double[] ingresosUtilidad(String desde, String hasta) {
        return ventaDAO.ingresosUtilidad(desde, hasta);
    }

    /**
     * Registra una venta completa en la base de datos (transaccion).
     *
     * @param dniCliente    DNI digitado (8 digitos, snapshot sin tabla cliente)
     * @param nombreCliente nombre digitado (sale en el comprobante)
     * @param idUsuario     id del cajero logueado
     * @param horaInicio    marca de inicio del registro (base del TPRV)
     * @param detalles      lineas del carrito (producto + cantidad + subtotal)
     * @param descuento     monto de descuento de la venta (RF04)
     * @param metodoPago    EFECTIVO, YAPE o TRANSFERENCIA (RF06)
     * @return el id generado de la venta
     * @throws ValidacionException si los datos no son validos o falla el guardado
     */
    public int registrarVentaEnBD(String dniCliente, String nombreCliente, int idUsuario,
                                  String horaInicio, ArrayList<DetalleVentaDTO> detalles,
                                  double descuento, String metodoPago) throws ValidacionException {

        // 1) Validaciones de negocio.
        if (dniCliente == null || !dniCliente.trim().matches("\\d{8}")) {
            throw new ValidacionException("El DNI debe tener 8 digitos");
        }
        if (nombreCliente == null || nombreCliente.trim().isEmpty()) {
            throw new ValidacionException("Ingrese el nombre del cliente");
        }
        if (detalles == null || detalles.isEmpty()) {
            throw new ValidacionException("Debe agregar productos a la venta");
        }
        if (!esMetodoValido(metodoPago)) {
            throw new ValidacionException("Seleccione el metodo de pago");
        }
        if (horaInicio == null || horaInicio.isEmpty()) {
            throw new ValidacionException("Falta la marca de inicio de la venta");
        }

        // 2) El cajero debe existir.
        Usuario usuario = usuarioDAO.buscar(idUsuario);
        if (usuario == null) {
            throw new ValidacionException("El cajero no existe");
        }

        // El total nunca se toma de un campo de texto de la vista:
        // se recalcula sumando los subtotales de los detalles menos el descuento.
        double subtotal = 0;
        for (DetalleVentaDTO d : detalles) {
            subtotal += d.getSubtotal();
        }
        if (descuento < 0) {
            throw new ValidacionException("El descuento no puede ser negativo");
        }
        if (descuento > subtotal) {
            throw new ValidacionException("El descuento no puede superar el subtotal");
        }

        VentaDTO ventaDTO = new VentaDTO();
        String ahora = ahora();
        ventaDTO.setFecha(ahora);
        ventaDTO.setHoraInicio(horaInicio);
        ventaDTO.setHoraFin(ahora);
        ventaDTO.setTotal(subtotal - descuento);
        ventaDTO.setDescuento(descuento);
        ventaDTO.setEstado("VIGENTE");
        ventaDTO.setDniCliente(dniCliente.trim());
        ventaDTO.setNombreCliente(nombreCliente.trim());
        ventaDTO.setMetodoPago(metodoPago);
        ventaDTO.setUsuario(usuario);
        ventaDTO.setDetalles(detalles);

        // 3) El DAO guarda TODO en una transaccion (venta + detalles + stock).
        int idVenta = ventaDAO.guardarVentaCompleta(ventaDTO);
        if (idVenta == -1) {
            throw new ValidacionException("Error al guardar la venta");
        }
        return idVenta;
    }

    /** Indica si el metodo de pago esta entre los permitidos. */
    private boolean esMetodoValido(String metodoPago) {
        if (metodoPago == null) {
            return false;
        }
        for (String m : METODOS_PAGO) {
            if (m.equals(metodoPago)) {
                return true;
            }
        }
        return false;
    }

    /** Fecha-hora actual en formato MySQL DATETIME ("yyyy-MM-dd HH:mm:ss"). */
    private String ahora() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
