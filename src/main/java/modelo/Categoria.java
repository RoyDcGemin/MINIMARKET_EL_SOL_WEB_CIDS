package modelo;

/**
 * MODELO Categoria.
 * Representa una fila de la tabla "categoria".
 * Solo lleva datos y getters/setters: no tiene logica (POJO).
 * Los atributos se llaman igual que las columnas de la tabla,
 * por eso el DAO puede mapearlos directamente.
 */
public class Categoria {
    private int idCategoria;
    private String descripcion;

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

}
