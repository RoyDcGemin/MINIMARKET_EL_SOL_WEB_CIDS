package dto;

import java.util.ArrayList;
import modelo.Usuario;

/**
 * DTO VentaDTO.
 * Union de Venta + Usuario + lista de DetalleVentaDTO.
 * Se construye en VentaDAO.listarVentasConDetalles() y se muestra
 * en reportes.jsp. Asi la vista no necesita tocar la BD.
 * El cliente ya NO es un objeto: son 2 Strings snapshot
 * (dniCliente + nombreCliente) denormalizados por alcance.
 */
public class VentaDTO {

    private int idVenta;
    private String fecha;
    private String horaInicio;
    private String horaFin;
    private double total;
    private double descuento;
    private String estado;
    private String dniCliente;
    private String nombreCliente;
    private String metodoPago;

    private Usuario usuario;

    private ArrayList<DetalleVentaDTO> detalles;

    public VentaDTO() {
        detalles = new ArrayList<>();
    }

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

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public ArrayList<DetalleVentaDTO> getDetalles() {
        return detalles;
    }

    public void setDetalles(ArrayList<DetalleVentaDTO> detalles) {
        this.detalles = detalles;
    }
}
