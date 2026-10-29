package interfaces;

import modelo.Usuario;

/**
 * Interfaz de Usuario.
 * Especializa {@code CRUD<Usuario>}. UsuarioDAO la implementa.
 * (El login se hace en UsuarioController usando HashUtil, no aqui).
 */
public interface UsuarioInterface extends CRUD<Usuario>{
    Usuario buscarPorNombre(String usuario);
}