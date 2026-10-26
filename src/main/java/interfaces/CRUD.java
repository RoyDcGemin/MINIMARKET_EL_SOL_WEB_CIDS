package interfaces;

import java.util.ArrayList;

/**
 * Interfaz GENERICA de CRUD (Create, Read, Update, Delete).
 * Define el "contrato" basico que deben cumplir todos los DAOs:
 * quien implemente CRUD<T> debe ofrecer estos metodos.
 *
 * <T> es el tipo de entidad (modelo) que maneja cada DAO.
 * Ejemplo: CRUD<Producto> -> lista productos, agrega productos, etc.
 *
 * Uso de genericos: una sola interfaz sirve para Producto,
 * Categoria, Usuario, Venta y DetalleVenta sin repetir codigo.
 */
public interface CRUD<T> {
    public void agregar(T t);
    public void modificar(T t);
    public void eliminar(int id);
    public ArrayList<T> listar();
    public T buscar(int id);
}