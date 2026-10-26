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

### Usuarios de prueba (los 2 perfiles)

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `1234` | ADMIN |
| `cajero` | `1234` | CAJERO |

---

## 3. Dónde está cada cosa (mapa para la revisión del código)

| Funcionalidad | Archivo | Qué mirar |
|---|---|---|
| Autenticación / login | `web/LoginServlet.java` | `doPost()` valida y guarda el usuario en sesión |
| Verificación de contraseña | `controller/UsuarioController.java` | `iniciarSesion()` compara el hash |
| Hash SHA-256 | `util/HashUtil.java` | `sha256()` |
| **Los 2 perfiles (roles)** | `web/AuthFilter.java` | `RUTAS_ADMIN` y `esRutaAdmin()` |
| Menú según el perfil | `WEB-INF/jsp/menu.jsp` y `cabecera.jsp` | `<c:if test="${sessionScope.usuario.rol == 'ADMIN'}">` |
| Conexión Singleton | `util/Conexion.java` | `getConexion()` |
| Registro de venta | `web/VentaServlet.java` | `doPost()` acción `guardar` |
| Transacción de la venta (todo o nada) | `modeloDAO/VentaDAO.java` | `guardarVentaCompleta()` |
| Cálculo del total en el servidor | `controller/VentaController.java` | `registrarVentaEnBD()` |
| Comprobante PDF | `web/ComprobanteServlet.java` | `doGet()` con OpenPDF |
| Cierre de caja | `web/CierreServlet.java` | `doGet()` |
| **Reportes** | `web/ReportesServlet.java` | `doGet()` |
| CRUD de productos | `web/ProductoServlet.java` | `doPost()` (agregar / modificar / desactivar / eliminar) |
| **Eliminación lógica (UPDATE)** | `modeloDAO/ProductoDAO.java` | `desactivar()` y `activar()` |
| **Eliminación física (DELETE)** | `modeloDAO/ProductoDAO.java` | `eliminar()` |
| Consultas de 1 tabla | `modeloDAO/ProductoDAO.java` | `listar()`, `listarInactivos()` |
| Consultas de 2 tablas (JOIN) | `modeloDAO/ProductoDAO.java` | `listarDTO()` (producto + categoria) |
| Consultas de 3 tablas (JOIN) | `modeloDAO/VentaDAO.java` | `ingresosUtilidad()` (detalle + venta + producto) |
| Esquema de la base de datos | `elsol_db.sql` | tablas, claves foráneas y datos de ejemplo |

---

## 4. Los 2 perfiles y su comportamiento diferente (indicación 10)

| Aspecto | ADMIN | CAJERO |
|---|---|---|
| Menú | Ve todos los módulos | Solo Venta y Cierre de caja |
| Productos / Categorías | Puede entrar | **Bloqueado** por el filtro |
| Reportes | Puede entrar | **Bloqueado** por el filtro |
| Registrar venta | Sí | Sí |
| Cierre de caja | Sí | Sí |

**Implementación:** `web/AuthFilter.java` intercepta **todas** las rutas; si el usuario no es ADMIN y la ruta está en `RUTAS_ADMIN` (`/productos`, `/categorias`, `/reportes`), lo redirige al menú con un mensaje. Además, `menu.jsp` y `cabecera.jsp` ocultan las opciones con `<c:if>` según `sessionScope.usuario.rol`.

---

## 5. Validaciones (indicación 2)

Se aplican en **dos niveles**: en el navegador (atributos `required`, `min`, `maxlength` y JavaScript) y en el **servidor** (controladores), que es el que realmente garantiza la integridad.

| Campo | Pantalla | Validación | Dónde (servidor) |
|---|---|---|---|
| Usuario / contraseña | login | No vacíos, credenciales válidas | `UsuarioController.iniciarSesion()` |
| Código de producto | venta | Debe existir en el catálogo | `VentaServlet.doPost()` |
| Cantidad | venta | Entero > 0 y ≤ stock disponible | `VentaServlet.doPost()` |
| DNI del cliente | venta | Exactamente 8 dígitos (`\\d{8}`) | `VentaController.registrarVentaEnBD()` |
| Nombre del cliente | venta | No vacío | `VentaController.registrarVentaEnBD()` |
| Descuento | venta | ≥ 0 y ≤ subtotal | `VentaController.registrarVentaEnBD()` |
| Método de pago | venta | EFECTIVO / YAPE / TRANSFERENCIA | `VentaController.esMetodoValido()` |
| Código de producto | productos | No vacío y único | `ProductoController.validar()` |
| Nombre de producto | productos | No vacío | `ProductoController.validar()` |
| Precio | productos | > 0 | `ProductoController.validar()` |
| Costo | productos | ≥ 0 | `ProductoController.validar()` |
| Stock / stock mínimo | productos | ≥ 0 | `ProductoController.validar()` |
| Categoría | productos | Seleccionada (> 0) | `ProductoController.validar()` |
| Contraseña nueva | cambiar contraseña | Mínimo 4 caracteres y confirmación | `UsuarioController.cambiarContrasena()` |

---

## 6. CRUD y tipos de eliminación (indicación 4)

- **Consultas de 1 tabla:** `ProductoDAO.listar()`, `ProductoDAO.listarInactivos()`, `CategoriaDAO.listar()`.
- **Consultas de 2 tablas (JOIN):** `ProductoDAO.listarDTO()` → producto + categoría.
- **Consultas de 3 tablas (JOIN):** `VentaDAO.ingresosUtilidad()` → detalle_venta + venta + producto.

**Eliminación lógica vs física:**

| Tipo | SQL | Método | Efecto |
|---|---|---|---|
| Lógica | `UPDATE producto SET activo = 0` | `ProductoDAO.desactivar()` | La fila **no** se borra; deja de listarse y aparece en "Registros eliminados" |
| Reactivación | `UPDATE producto SET activo = 1` | `ProductoDAO.activar()` | Vuelve al catálogo |
| Física | `DELETE FROM producto` | `ProductoDAO.eliminar()` | Borra la fila; solo si el producto nunca se vendió |

> **No existe CRUD de usuarios:** el módulo de administración de usuarios fue retirado. Los dos perfiles se crean en la base de datos (`elsol_db.sql`) y el usuario solo puede cambiar su propia contraseña.

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

Repositorio: `https://github.com/<usuario>/<repositorio>`
