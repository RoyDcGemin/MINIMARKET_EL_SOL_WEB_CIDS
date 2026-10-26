package modelo;

/**
 * MODELO Venta.
 * Representa una fila de la tabla "venta" (cabecera).
 * POJO: solo datos y getters/setters.
 *
 * A diferencia de la antigua Factura del proyecto base:
 * - Ya NO hay idCliente ni tabla cliente: el cliente queda como
 *   snapshot denormalizado (dniCliente + nombreCliente) por alcance.
 * - horaInicio/horaFin sostienen el indicador TPRV (tiempo promedio).
 * - estado (VIGENTE/ANULADA/CORREGIDA) sostiene %ERR y %OK.
 * - descuento y metodoPago sostienen RF04 (descuentos) y RF06 (cierre).
 */
public class Venta {
    private int idVenta;
    private String fecha;          // yyyy-MM-dd HH:mm:ss
    private String horaInicio;     // yyyy-MM-dd HH:mm:ss
    private String horaFin;        // yyyy-MM-dd HH:mm:ss
    private double total;
    private double descuento;
    private String estado;         // VIGENTE, ANULADA, CORREGIDA
    private String dniCliente;     // snapshot, 8 digitos
    private String nombreCliente;  // snapshot
    private String metodoPago;     // EFECTIVO, YAPE, TRANSFERENCIA
    private int idUsuario;         // cajero que vendio

    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(String horaInicio) {
        this.horaInicio = horaInicio;
    }

    public String getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(String horaFin) {
        this.horaFin = horaFin;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getDescuento() {
        return descuento;
    }

    public void setDescuento(double descuento) {
        this.descuento = descuento;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getDniCliente() {
        return dniCliente;
    }

    public void setDniCliente(String dniCliente) {
        this.dniCliente = dniCliente;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }
}
