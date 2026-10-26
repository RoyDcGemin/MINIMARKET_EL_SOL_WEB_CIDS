package controller;

/**
 * Excepcion simple para las VALIDACIONES de negocio.
 *
 * La lanza el controlador cuando un dato ingresado no es valido
 * (campo vacio, precio incorrecto, credenciales equivocadas, etc.)
 * y la vista la captura para mostrar ex.getMessage() en un JOptionPane.
 *
 * Asi, los mensajes de error "viven" en el controlador (zona de logica)
 * y la vista solo se encarga de mostrarlos.
 */
public class ValidacionException extends Exception {

    public ValidacionException(String mensaje) {
        super(mensaje);
    }
}