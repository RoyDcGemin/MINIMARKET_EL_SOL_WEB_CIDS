package controller;

import java.util.ArrayList;
import modelo.Usuario;
import modeloDAO.UsuarioDAO;
import util.HashUtil;

/**
 * Controlador de la entidad Usuario.
 *
 * CAPA INTERMEDIA (logica): aca vive la logica del LOGIN.
 *
 * FLUJO: Vista (Logueo) -> UsuarioController.iniciarSesion ->
 *        UsuarioDAO.buscarPorNombre -> Base de datos
 *        y de vuelta: Usuario -> controlador (compara contraseñas) -> vista.
 *
 * SEGURIDAD: la clave que el usuario escribe se hashea y se compara contra
 * el hash guardado en la BD. Nunca se compara texto plano.
 */
public class UsuarioController {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    /**
     * Valida las credenciales de un usuario para iniciar sesion.
     *
     * @param nombre nombre de usuario ingresado
     * @param clave contrasenia ingresada
     * @return el Usuario autenticado (se guarda como "sesion" para pasarlo
     *         entre ventanas)
     * @throws ValidacionException con el mensaje de error correspondiente
     *         (campos vacios, usuario inexistente o contrasenia incorrecta)
     */
    public Usuario iniciarSesion(String nombre, String clave) throws ValidacionException {
        // 1) Validacion de campos vacios.
        if (nombre.isEmpty() || clave.isEmpty()) {
            throw new ValidacionException("Ingresar usuario y contraseña");
        }

        // 2) Buscar al usuario por su nombre en la BD.
        Usuario u = usuarioDAO.buscarPorNombre(nombre);
        if (u == null) {
            throw new ValidacionException("Usuario no existe");
        }

        // 3) Comparar el hash de lo ingresado con el hash guardado.
        if (!u.getClave().equals(HashUtil.sha256(clave))) {
            throw new ValidacionException("Usuario o contraseña invalido");
        }

        // Credenciales correctas: devolvemos el usuario logueado.
        return u;
    }

    /**
     * Devuelve todos los usuarios (se usa para el filtro "cajero" de reportes).
     */
    public ArrayList<Usuario> listar() {
        return usuarioDAO.listar();
    }

    /**
     * Cambio de contrasena de un usuario logueado.
     *
     * @param idUsuario el usuario que cambia su clave
     * @param claveActual clave que tiene hoy (se verifica contra el hash)
     * @param claveNueva nueva clave
     * @param confirmacion repeticion de la nueva clave
     */
    public void cambiarContrasena(int idUsuario, String claveActual, String claveNueva, String confirmacion)
            throws ValidacionException {
        if (claveActual == null || claveActual.isEmpty()) {
            throw new ValidacionException("Ingrese su contrasena actual");
        }
        if (claveNueva == null || claveNueva.isEmpty()) {
            throw new ValidacionException("Ingrese la nueva contrasena");
        }
        if (claveNueva.length() < 4) {
            throw new ValidacionException("La nueva contrasena debe tener al menos 4 caracteres");
        }
        if (!claveNueva.equals(confirmacion)) {
            throw new ValidacionException("La confirmacion no coincide con la nueva contrasena");
        }

        Usuario u = usuarioDAO.buscar(idUsuario);
        if (u == null) {
            throw new ValidacionException("El usuario no existe");
        }
        if (!u.getClave().equals(HashUtil.sha256(claveActual))) {
            throw new ValidacionException("La contrasena actual es incorrecta");
        }

        u.setClave(claveNueva); // el DAO guardara el HASH
        usuarioDAO.modificar(u);
    }
}