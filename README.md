# Sistema de Ventas — Minimarket "El Sol"

Aplicación web para automatizar el registro de ventas, la emisión del comprobante PDF, el cierre de caja y los reportes del Minimarket "El Sol".

**Curso:** Curso Integrador I — Desarrollo de Software
**Tecnologías:** Java 21 · Jakarta EE (Servlet 6) · JSP/JSTL · MySQL 8 · OpenPDF · Maven · Git/GitHub

---

## 1. Arquitectura (MVC + DAO + DTO + Singleton)

```
src/main/java/
├── modelo/        Entidades POJO (Usuario, Categoria, Producto, Venta, DetalleVenta)
├── dto/           Objetos de transferencia (VentaDTO, DetalleVentaDTO, ProductoDTO)
├── interfaces/    Contrato CRUD<T> + interfaces por entidad
├── modeloDAO/     Acceso a datos (SQL, transacciones)
├── controller/    Lógica de negocio y VALIDACIONES
├── web/           Servlets (controladores HTTP) + filtros + utilidades web
└── util/          Conexión Singleton y hash de contraseñas
src/main/webapp/
├── WEB-INF/jsp/   Vistas (login, menu, venta, comprobante, cierre, reportes, productos, categorias)
├── css/ js/ img/  Recursos estáticos
└── WEB-INF/tlds/  Función de formato de moneda
src/main/resources/
└── logback.xml    Configuración de logs (consola + archivo con rotación diaria)
elsol_db.sql       Script de creación de la base de datos
```

---

## 2. Puesta en marcha

```bash
# 1) Base de datos (crea elsol_db, tablas y datos de ejemplo)
mysql -u root -p < elsol_db.sql

# 2) Compilar y empaquetar
mvn clean package          # genera target/PROYECTO_EL_SOL_WEB.war

# 3) Desplegar
# copiar el .war a <TOMCAT_HOME>/webapps/ e iniciar Tomcat 10.1+
```

La conexión se configura en `src/main/java/util/Conexion.java` (por defecto `jdbc:mysql://localhost:3306/elsol_db`, usuario `elsol`).

### Usuarios de prueba (los 2 perfiles, con la política de contraseñas)

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `Admin#2026` | ADMIN |
| `cajero` | `Cajero2026!` | CAJERO |

**Política de contraseñas** (`util/PasswordPolicy.java`): mínimo **8** posiciones, con al menos **una minúscula**, **una mayúscula**, **un número** y **un símbolo especial**, y máximo 64. La validación de registro/cambio tiene su **espejo en el navegador** (atributo `pattern`).

> **Registro de nuevos usuarios:** en la ventana de login hay un enlace **“Crear cuenta”** (`/registro`, `web/RegistroServlet.java`). Toda cuenta creada ahí nace de forma forzada con perfil **CAJERO** (el rol nunca se lee del formulario: evitaría que cualquiera se autoasigne ADMIN). Las cuentas ADMIN solo se crean en `elsol_db.sql`. No es un CRUD de usuarios: no hay pantalla para listar, editar ni eliminar usuarios.

---

## 3. Dónde está cada cosa (mapa para la revisión del código)

| Funcionalidad | Archivo | Qué mirar |
|---|---|---|
| Autenticación / login | `web/LoginServlet.java` | `doPost()` valida y guarda el usuario en sesión |
| Verificación de contraseña | `controller/UsuarioController.java` | `iniciarSesion()` compara el hash |
| **Política de contraseñas** | `util/PasswordPolicy.java` | `validar()`: 8+, minuscula, mayuscula, numero y simbolo |
| **Registro de usuarios desde el login** | `web/RegistroServlet.java` + `WEB-INF/jsp/registro.jsp` | rol forzado a CAJERO en `UsuarioController.registrar()` |
| Hash SHA-256 | `util/HashUtil.java` | `sha256()` |
| **Los 2 perfiles (roles)** | `web/AuthFilter.java` | `RUTAS_ADMIN` y `esRutaAdmin()` |
| Menú según el perfil | `WEB-INF/jsp/menu.jsp` y `cabecera.jsp` | `<c:if test="${sessionScope.usuario.rol == 'ADMIN'}">` |
| Conexión Singleton | `util/Conexion.java` | `getConexion()` (lanza `SQLException`, nunca devuelve `null`) |
| Registro de venta | `web/VentaServlet.java` | `doPost()` acción `vender` |
| Transacción de la venta (todo o nada) | `modeloDAO/VentaDAO.java` | `guardarVentaCompleta()` |
| Cálculo del total en el servidor | `controller/VentaController.java` | `registrarVentaEnBD()` |
| Comprobante PDF | `web/ComprobanteServlet.java` | `doGet()` con OpenPDF |
| Cierre de caja | `web/CierreServlet.java` | `doGet()` |
| **Reportes** | `web/ReportesServlet.java` | `doGet()` (incluye anular venta, solo ADMIN) |
| CRUD de productos | `web/ProductoServlet.java` | `doGet()`/`doPost()` (agregar / modificar / desactivar / eliminar) |
| **Eliminación lógica (UPDATE)** | `modeloDAO/ProductoDAO.java` | `desactivar()` y `activar()` |
| **Eliminación física (DELETE)** | `modeloDAO/ProductoDAO.java` | `eliminar()` |
| Consultas de 1 tabla | `modeloDAO/ProductoDAO.java` | `listar()`, `listarInactivos()`, `buscarPorTexto()` |
| Consultas de 2 tablas (JOIN) | `modeloDAO/ProductoDAO.java` | `listarDTO()` (producto + categoria) |
| Consultas de 3 tablas (JOIN) | `modeloDAO/VentaDAO.java` | `ingresosUtilidad()` (detalle + venta + producto) |
| Esquema de la base de datos | `elsol_db.sql` | tablas, claves foráneas, rol con `CHECK` y datos de ejemplo |
| Exportación del reporte a Excel | `web/ExportarExcelServlet.java` | `doGet()` con Apache POI |
| Registro de eventos (logs) | `logback.xml` y DAOs | `LoggerFactory.getLogger(...)` |

---

## 4. Los 2 perfiles y su comportamiento diferente (indicación 10)

| Aspecto | ADMIN | CAJERO |
|---|---|---|
| Menú | Todos los módulos | Venta, Cierre y consulta de Productos |
| Productos | **Administra** (agregar / editar / desactivar / eliminar) | **Solo lectura** (la tabla, sin botones ni formulario) |
| Categorías | Puede entrar | **Bloqueado** por el filtro |
| Reportes (indicadores, Excel) | Puede entrar | **Bloqueado** por el filtro |
| Anular venta | Sí (en Reportes) | No |
| Reactivar eliminación lógica | Sí (en Reportes) | No |
| Crear cuentas ADMIN | Solo por script (`elsol_db.sql`) | No (el registro público crea solo CAJERO) |
| Registrar venta / cierre de caja | Sí | Sí |

**Implementación, en 2 niveles de autorización:**

1. **Por ruta:** `web/AuthFilter.java` intercepta **todas** las solicitudes (patrón *Intercepting Filter*); si el usuario no es ADMIN y la ruta está en `RUTAS_ADMIN` (`/categorias`, `/reportes`), lo redirige al menú con un mensaje.
2. **Por acción:** `/productos` abre para los dos perfiles, pero `ProductoServlet.doPost()` vuelve a verificar el rol antes de aceptar cambios; y `productos.jsp` oculta botones y formulario con `<c:if test="${sessionScope.usuario.rol == 'ADMIN'}">`. Manipular el HTML del navegador no evade el bloqueo: el control está en el servidor.

---

## 5. Validaciones (indicación 2)

Se aplican en **dos niveles**: en el navegador (atributos `required`, `min`, `max`, `pattern`, `maxlength` y JavaScript) y en el **servidor** (controladores), que es el que realmente garantiza la integridad. Se valida el **tipo** del dato, su **longitud** y su **rango**.

| Campo | Pantalla | Validación (tipo / longitud / rango) | Dónde (servidor) |
|---|---|---|---|
| Usuario / contraseña | login y registro | Usuario 3–50 con `A-Za-z0-9._-`; contraseña según la política (≥8, minúscula, mayúscula, número, símbolo) | `UsuarioController` + `PasswordPolicy` |
| Nombre de la cuenta | registro | Texto, 3–100 | `UsuarioController.registrar()` |
| Código de producto | venta | Debe existir en el catálogo | `VentaServlet.doPost()` |
| Cantidad | venta | Entero > 0 y ≤ stock disponible | `VentaServlet.doPost()` |
| DNI del cliente | venta | Rango: exactamente 8 dígitos (`\\d{8}`) | `VentaController.registrarVentaEnBD()` |
| Nombre del cliente | venta | No vacío | `VentaController.registrarVentaEnBD()` |
| Descuento | venta | Rango: ≥ 0 y ≤ subtotal | `VentaController.registrarVentaEnBD()` |
| Método de pago | venta | EFECTIVO / YAPE / TRANSFERENCIA | `VentaController.esMetodoValido()` |
| Código de producto | productos | Texto, 3–30, con `A-Za-z0-9._-`, y único | `ProductoController.validar()` |
| Nombre de producto | productos | Texto, 3–100 | `ProductoController.validar()` |
| Descripción | productos | Texto, hasta 100 | `ProductoController.validar()` |
| Precio | productos | Número, 0 < precio ≤ 9 999 999.99 | `ProductoController.validar()` |
| Costo | productos | Número, 0 ≤ costo ≤ 9 999 999.99 | `ProductoController.validar()` |
| Stock / stock mínimo | productos | Entero, 0–1 000 000 (mínimo ≥ 1) | `ProductoController.validar()` |
| Categoría | productos | Seleccionada y existente en la BD | `ProductoController.validar()` |
| Contraseña nueva | cambiar contraseña | Política + confirmación | `UsuarioController.cambiarContrasena()` |

Además, los tipos se validan al convertir los parámetros: si el navegador envía texto cuando se esperaba un número (o si se manipula el POST a mano), `ProductoServlet.entero()/decimal()` responden “El campo … debe ser un número”, en vez de interpretarlo como 0.

---

## 6. CRUD y tipos de eliminación (indicación 4)

- **Consultas de 1 tabla:** `ProductoDAO.buscarPorTexto()` (búsqueda usada en la venta), `ProductoDAO.listarInactivos()`, `CategoriaDAO.listar()`.
- **Consultas de 2 tablas (JOIN):** `ProductoDAO.listarDTO()` → producto + categoría (es la tabla que se muestra en `/productos`).
- **Consultas de 3 tablas (JOIN):** `VentaDAO.ingresosUtilidad()` → detalle_venta + venta + producto.

**Eliminación lógica vs física:**

| Tipo | SQL | Método | Efecto |
|---|---|---|---|
| Lógica | `UPDATE producto SET activo = 0` | `ProductoDAO.desactivar()` | La fila **no** se borra; deja de listarse y aparece en "Registros eliminados" |
| Reactivación | `UPDATE producto SET activo = 1` | `ProductoDAO.activar()` | Vuelve al catálogo |
| Física | `DELETE FROM producto` | `ProductoDAO.eliminar()` | Borra la fila; solo si el producto nunca se vendió |

> **No existe CRUD de usuarios:** no hay pantalla para listar/editar/eliminar usuarios. Sí existe el **auto-registro** desde el login (`/registro`), que siempre crea perfiles CAJERO; el ADMIN mantiene el control porque no se puede elegir rol. El usuario logueado solo puede cambiar su propia contraseña.

---

## 7. Reportes implementados (indicación 3)

En `web/ReportesServlet.java` + `reportes.jsp`:

1. **Reporte por fechas** — ventas filtradas por rango de fechas, cajero y producto.
2. **Reporte de indicadores** — TPRV (tiempo promedio), %ERR (errores), TPV (ticket promedio) y %OK (ventas correctas).
3. **Reporte de máximo, mínimo y promedio** — `MAX`, `MIN` y `AVG` de los totales.
4. **Reporte de ingresos, utilidad y stock** — ingresos, utilidad (`cantidad × (precio − costo)`) y unidades vendidas.
5. **Reporte de registros eliminados** — productos con `activo = 0` (eliminación lógica), con opción de reactivar.

---

## 8. Control de versiones

```bash
git log --oneline
git remote -v
```

Repositorio: `https://github.com/RoyDcGemin/MINIMARKET_EL_SOL_WEB_CIDS`

---

## 9. Librerías de apoyo (rúbrica APF3)

Todas son de código abierto y están declaradas en `pom.xml` con versión fija.

| Librería | Versión | Dónde se usa | Para qué |
|---|---|---|---|
| **Logback + SLF4J** | 1.5.6 / 2.0.13 | `src/main/resources/logback.xml`; DAOs y `util/Conexion.java` | Registro de eventos con rotación diaria en `logs/elsol.log` |
| **Apache Commons Lang** | 3.14.0 | `controller/ProductoController.java`, `controller/UsuarioController.java` | Validación de textos con `StringUtils.isBlank()` |
| **Google Guava** | 33.2.1-jre | `modeloDAO/VentaDAO.java` | Precondiciones (`Preconditions`) para fallo rápido |
| **Apache POI** | 5.2.5 | `web/ExportarExcelServlet.java` (`/reportes/excel`) | Exportar el reporte de ventas a Excel `.xlsx` |
| **OpenPDF** | 1.3.30 | `web/ComprobanteServlet.java` | Generar el comprobante de venta en PDF |
| **MySQL Connector/J** | 8.4.0 | `util/Conexion.java` | Conexión con MySQL 8 |

**Seguridad:** versiones fijas para evitar dependencias transitivas vulnerables, logs sin contraseñas ni datos sensibles, exportación restringida al perfil ADMIN (`AuthFilter`) y consultas con `PreparedStatement` en toda la capa DAO.
