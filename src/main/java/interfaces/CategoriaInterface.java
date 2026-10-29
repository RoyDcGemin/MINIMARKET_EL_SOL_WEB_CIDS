package interfaces;

import modelo.Categoria;

/**
 * Interfaz de Categoria.
 * No agrega metodos propios: solo especializa {@code CRUD<Categoria>},
 * es decir, "las categorias se manejan con CRUD normal".
 * CategoriaDAO la implementa.
 */
public interface CategoriaInterface extends CRUD<Categoria> {
    
}