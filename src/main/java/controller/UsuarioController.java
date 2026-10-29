package controller;

import java.util.ArrayList;
import modelo.Usuario;
import modeloDAO.UsuarioDAO;
import org.apache.commons.lang3.StringUtils;
import util.HashUtil;
import util.PasswordPolicy;

/**
 * Controlador de la entidad Usuario.
 *
 * CAPA INTERMEDIA (logica): aqui vive la logica del LOGIN y del REGISTRO.
 *
 * FLUJO: Vista (Logueo) -> UsuarioController.iniciarSesion ->
 *        UsuarioDAO.buscarPorNombre -> Base de datos
 *        y de vuelta: Usuario -> controlador (compara contraseñas) -> vista.
 *
 * SEGURIDAD:
 *  - la clave que el usuario escribe se hashea y se compara contra el hash
 *    guardado en la BD. Nunca se compara texto plano.
 *  - toda clave nueva (registro o cambio) pasa por la POLITICA de
 *    util.PasswordPolicy: >= 8 caracteres con minuscula, mayuscula,
 *    numero y simbolo especial.
 *  - registrar() SOLO crea perfiles CAJERO: el rol no se toma de la vista,
 *    de modo que nadie puede autoasignarse el perfil ADMIN.
 */
public class UsuarioController {

    /** Rol que se asigna a toda cuenta creada desde el registro publico. */
    public static final String ROL_REGISTRO = "CAJERO";

    /** Largo del nombre de usuario: 3..50 (columna usuario VARCHAR(50)). */
    private static final int USUARIO_MIN = 3, USUARIO_MAX = 50;

    /** Largo del nombre completo: 3..100 (columna nombre VARCHAR(100)). */
    private static final int NOMBRE_MIN = 3, NOMBRE_MAX = 100;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    /**
     * Valida las credenciales de un usuario para iniciar sesion.
     *
     * @param nombre nombre de usuario ingresado
     * @param clave contrasenia ingresada
     * @return el Usuario autenticado (se guarda como "sesion" para pasarlo
     *         entre ventanas)
     * @throws ValidacionException con el mensaje de error correspondiente
     *         (campos invalidos, usuario inexistente o contrasenia incorrecta)
     */
    public Usuario iniciarSesion(String nombre, String clave) throws ValidacionException {
        // 1) Validacion del formato de los campos (Apache Commons Lang).
        validarNombreUsuario(nombre);
        String mensajeClave = PasswordPolicy.validar(clave);
        if (mensajeClave != null) {
            throw new ValidacionException(mensajeClave);
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
     * REGISTRO de un nuevo usuario desde la ventana de logueo.
     *
     * Valida formato/longitud de los campos, la POLITICA de contrasena y
     * que el nombre no exista; el ROL siempre es CAJERO (no se acepta del
     * formulario, evita la escalada de privilegios).
     *
     * @param usuario nombre de usuario deseado (unico)
     * @param nombre nombre completo de la persona
     * @param clave contrasenia propuesta (debe cumplir la politica)
     * @param confirmacion repeticion de la contrasenia
     */
    public void registrar(String usuario, String nombre, String clave, String confirmacion)
            throws ValidacionException {
        validarNombreUsuario(usuario);

        if (StringUtils.isBlank(nombre)) {
            throw new ValidacionException("Ingrese su nombre completo");
        }
        if (nombre.trim().length() < NOMBRE_MIN || nombre.trim().length() > NOMBRE_MAX) {
            throw new ValidacionException(
                    "El nombre debe tener entre " + NOMBRE_MIN + " y " + NOMBRE_MAX + " caracteres");
        }

        // POLITICA: la misma validacion que el login y el cambio de clave.
        String mensajeClave = PasswordPolicy.validar(clave);
        if (mensajeClave != null) {
            throw new ValidacionException(mensajeClave);
        }
        if (!clave.equals(confirmacion)) {
            throw new ValidacionException("La confirmacion no coincide con la contraseña");
        }

        if (usuarioDAO.buscarPorNombre(usuario) != null) {
            throw new ValidacionException("El usuario \"" + usuario + "\" ya existe");
        }

        Usuario nuevo = new Usuario();
        nuevo.setUsuario(usuario);
        nuevo.setNombre(nombre.trim());
        nuevo.setClave(clave); // el DAO guarda el HASH, nunca el texto plano
        nuevo.setRol(ROL_REGISTRO);
        usuarioDAO.agregar(nuevo);
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

        // POLITICA: la nueva contrasena debe cumplir las 5 reglas.
        String mensajeClave = PasswordPolicy.validar(claveNueva);
        if (mensajeClave != null) {
            throw new ValidacionException(mensajeClave);
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

    /**
     * Valida el nombre de usuario: no vacio, 3..50 caracteres y sin
     * espacios (solo letras, numeros, punto, guion o guion bajo).
     */
    private void validarNombreUsuario(String nombre) throws ValidacionException {
        if (StringUtils.isBlank(nombre)) {
            throw new ValidacionException("Ingresar usuario");
        }
        nombre = nombre.trim();
        if (nombre.length() < USUARIO_MIN || nombre.length() > USUARIO_MAX) {
            throw new ValidacionException(
                    "El usuario debe tener entre " + USUARIO_MIN + " y " + USUARIO_MAX + " caracteres");
        }
        if (!nombre.matches("[A-Za-z0-9._-]+")) {
            throw new ValidacionException(
                    "El usuario solo puede tener letras, numeros, punto, guion o guion bajo");
        }
    }
}
