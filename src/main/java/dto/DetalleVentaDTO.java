package dto;

import modelo.Producto;

/**
 * DTO DetalleVentaDTO.
 * Union de DetalleVenta + Producto (nombre del producto).
 * Se usa para mostrar cada renglon de una venta con el nombre del
 * producto en lugar del idProducto.
 */
public class DetalleVentaDTO {

    private int idDetalle;
    private Producto producto;
    private int cantidad;
    private double subtotal;

    public DetalleVentaDTO() {
    }

    public DetalleVentaDTO(int idDetalle, Producto producto, int cantidad, double subtotal) {
        this.idDetalle = idDetalle;
        this.producto = producto;
        this.cantidad = cantidad;
        this.subtotal = subtotal;
    }

    public int getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(int idDetalle) {
        this.idDetalle = idDetalle;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }
}
