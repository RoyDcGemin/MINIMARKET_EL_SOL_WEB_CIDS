package controller;

import java.util.ArrayList;
import modelo.Categoria;
import modeloDAO.CategoriaDAO;

/**
 * Controlador de la entidad Categoria.
 *
 * CAPA INTERMEDIA (logica): la vista le pide categorias y este controlador
 * se las consigue al DAO. Es el punto de entrada para los datos; las vistas
 * nunca instancian un DAO directamente.
 *
 * FLUJO: Vista -> CategoriaController -> CategoriaDAO -> Base de datos
 *                              y de vuelta con los datos.
 */
public class CategoriaController {

    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    /**
     * Devuelve todas las categorias (para llenar el combo de productos).
     *
     * @return lista de Categoria
     */
    public ArrayList<Categoria> listar() {
        return categoriaDAO.listar();
    }

    /**
     * Agrega una categoria nueva (la descripcion es obligatoria).
     *
     * @param descripcion nombre de la categoria
     * @throws ValidacionException si la descripcion esta vacia
     */
    public void agregar(String descripcion) throws ValidacionException {
        validarDescripcion(descripcion);

        Categoria c = new Categoria();
        c.setDescripcion(descripcion);
        categoriaDAO.agregar(c);
    }

    /**
     * Modifica la descripcion de una categoria existente.
     */
    public void modificar(int id, String descripcion) throws ValidacionException {
        validarDescripcion(descripcion);

        Categoria c = new Categoria();
        c.setIdCategoria(id);
        c.setDescripcion(descripcion);
        categoriaDAO.modificar(c);
    }

    /**
     * Elimina una categoria, pero solo si no tiene productos asignados.
     *
     * @param id id de la categoria
     * @throws ValidacionException si la categoria tiene productos
     */
    public void eliminar(int id) throws ValidacionException {
        if (categoriaDAO.contarProductos(id) > 0) {
            throw new ValidacionException(
                    "No se puede eliminar: la categoria tiene productos asignados");
        }
        categoriaDAO.eliminar(id);
    }

    /**
     * Validacion centralizada: la descripcion es obligatoria.
     */
    private void validarDescripcion(String descripcion) throws ValidacionException {
        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new ValidacionException("Ingrese la descripcion de la categoria");
        }
    }
}