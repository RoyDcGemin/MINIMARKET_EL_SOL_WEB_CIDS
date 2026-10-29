package modelo;

/**
 * MODELO Producto.
 * Representa una fila de la tabla "producto" del Minimarket "El Sol".
 * POJO: solo datos y getters/setters.
 *
 * - codigo: UNICO; es lo que el cajero digita en la venta para emular
 *   el scanner de codigo de barras (sin hardware dedicado).
 * - precio: precio de venta al publico.
 * - costo: precio de compra; sirve para calcular la utilidad en el
 *   reporte de ingresos/utilidad/stock.
 * - stock / stockMinimo: existencias y umbral de reposicion.
 * - descripcion: presentacion del producto (bolsa 1 kg, botella 1.5 L...).
 * - activo: marca de ELIMINACION LOGICA. 1 = vigente, 0 = dado de baja.
 *   Cuando se "desactiva" un producto NO se borra la fila (UPDATE),
 *   de modo que las ventas historicas siguen siendo consistentes.
 */
public class Producto {
    private int idProducto;
    private String codigo;
    private String nombre;
    private String descripcion;
    private double precio;
    private double costo;
    private int stock;
    private int stockMinimo;
    private int idCategoria;
    private int activo;

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

    public double getCosto() {
        return costo;
    }

    public void setCosto(double costo) {
        this.costo = costo;
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

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    /** 1 = vigente, 0 = eliminado logicamente. */
    public int getActivo() {
        return activo;
    }

    public void setActivo(int activo) {
        this.activo = activo;
    }
}
