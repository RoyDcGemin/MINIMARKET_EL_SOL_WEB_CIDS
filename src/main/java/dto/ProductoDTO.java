package dto;

import modelo.Categoria;

/**
 * DTO ProductoDTO (Data Transfer Object = Objeto para transferir datos).
 * No representa una tabla: es la UNION de Producto + Categoria.
 * Se usa para mostrar en la tabla de productos el nombre de la
 * categoria junto al producto, sin escribir SQL en la vista.
 * Incluye descripcion y stockMinimo para la vista del Minimarket El Sol.
 */
public class ProductoDTO {
    private int idProducto;
    private String codigo;
    private String nombre;
    private String descripcion;
    private double precio;
    private double costo;
    private int stock;
    private int stockMinimo;
    private int activo;

    private Categoria categoria;

    public double getCosto() {
        return costo;
    }

    public void setCosto(double costo) {
        this.costo = costo;
    }

    public int getActivo() {
        return activo;
    }

    public void setActivo(int activo) {
        this.activo = activo;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }
}
