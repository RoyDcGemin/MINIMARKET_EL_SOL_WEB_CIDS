package modelo;

/**
 * MODELO Usuario.
 * Representa una fila de la tabla "usuario".
 * POJO: solo datos y getters/setters.
 * La clave guardada es el HASH SHA-256 (nunca texto plano).
 */
public class Usuario {
    private int idUsuario;     
    private String usuario;    
    private String clave;      
    private String nombre;     
    private String rol;        

    public int getIdusuario() {
        return idUsuario;
    }

    public void setIdusuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}