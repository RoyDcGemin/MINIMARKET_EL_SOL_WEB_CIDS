package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase encargada de entregar conexiones a la base de datos MySQL.
 *
 * Patron usado: Singleton "simple".
 * - El constructor es privado, por lo que NO se pueden crear instancias.
 * - Se accede siempre a traves del metodo estatico getConexion().
 */
public class Conexion {

    /** URL de conexion (sobreescribible con -Dapp.bd.url). */
    private static final String URL = System.getProperty(
            "app.bd.url",
            "jdbc:mysql://localhost:3306/elsol_db"
                    + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Lima");

    /** Usuario de la BD (sobreescribible con -Dapp.bd.usuario). */
    private static final String USUARIO = System.getProperty("app.bd.usuario", "elsol");

    /** Clave de la BD (sobreescribible con -Dapp.bd.clave). */
    private static final String CLAVE = System.getProperty("app.bd.clave", "elsol123");

    // Constructor privado: evita que alguien haga "new Conexion()".
    private Conexion() {
    }

    /**
     * Devuelve una conexion nueva a la base de datos.
     *
     * @return Connection lista para usarse, o null si no se pudo conectar.
     */
    public static Connection getConexion() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USUARIO, CLAVE);
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(Conexion.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
