<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="sol" uri="http://elsol/funciones" %>
<jsp:include page="cabecera.jsp">
    <jsp:param name="titulo" value="Productos"/>
</jsp:include>
<%@ include file="flash.jsp" %>

<c:set var="esAdmin" value="${sessionScope.usuario.rol == 'ADMIN'}"/>

<!-- VISTA PRODUCTOS: catalogo del Minimarket.
     AUTORIZACION en dos niveles:
       - El perfil ADMIN administra el catalogo (agregar/editar/desactivar/eliminar).
       - El perfil CAJERO entra en modo LECTURA (sin botones ni formulario);
         el servidor vuelve a verificar el rol en el POST (ProductoServlet).
     Incluye las DOS formas de eliminacion que pide el curso:
       - Desactivar = ELIMINACION LOGICA (UPDATE producto SET activo = 0).
       - Eliminar   = ELIMINACION FISICA (DELETE), solo si nunca se vendio. -->

<h1 class="titulo-pagina">Productos</h1>

<section class="panel-lateral">
    <div class="tarjeta">
        <div class="fila-acciones">
            <h2>Lista de productos</h2>
            <!-- CONSULTA con filtro (texto): codigo, nombre o descripcion -->
            <form action="${pageContext.request.contextPath}/productos" method="get" class="campo-buscador">
                <input type="search" id="q" name="q" placeholder="Buscar por codigo, nombre o descripcion"
                       maxlength="100" value="<c:out value='${requestScope.q}'/>">
                <button type="submit" class="btn btn-secundario btn-pequeno">Buscar</button>
                <c:if test="${not empty requestScope.q}">
                    <a href="${pageContext.request.contextPath}/productos" class="btn btn-secundario btn-pequeno">Limpiar</a>
                </c:if>
            </form>
        </div>

        <c:if test="${not empty requestScope.q}">
            <p class="ayuda-campo">Mostrando productos que coinciden con "${requestScope.q}"
                (${productos.size()} resultado(s)). La tabla se arma con el JOIN producto + categoria.</p>
        </c:if>

        <div class="envoltorio-tabla">
            <table class="tabla">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Codigo</th>
                        <th>Nombre</th>
                        <th>Descripcion</th>
                        <th class="texto-derecha">Precio</th>
                        <th class="texto-derecha">Costo</th>
                        <th class="texto-derecha">Stock</th>
                        <th class="texto-derecha">Stock min.</th>
                        <th>Categoria</th>
                        <th>Acciones</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="p" items="${productos}">
                        <tr>
                            <td>${p.idProducto}</td>
                            <td><strong><c:out value="${p.codigo}"/></strong></td>
                            <td><c:out value="${p.nombre}"/></td>
                            <td><c:out value="${p.descripcion}"/></td>
                            <td class="texto-derecha monto">${sol:moneda(p.precio)}</td>
                            <td class="texto-derecha monto">${sol:moneda(p.costo)}</td>
                            <td class="texto-derecha">
                                ${p.stock}
                                <c:if test="${p.stock <= p.stockMinimo}">
                                    <span class="etiqueta-alerta">Stock bajo</span>
                                </c:if>
                            </td>
                            <td class="texto-derecha">${p.stockMinimo}</td>
                            <td><c:out value="${p.categoria.descripcion}"/></td>
                            <td>
                                <c:choose>
                                    <c:when test="${esAdmin}">
                                        <div class="grupo-botones">
                                            <!-- Los datos viajan en atributos data-* escapados
                                                 con <c:out>: el nombre nunca se interpola dentro
                                                 de un onclick (evita romper el HTML con comillas). -->
                                            <button type="button" class="btn btn-secundario btn-pequeno btn-editar"
                                                    data-id="${p.idProducto}"
                                                    data-codigo="<c:out value='${p.codigo}'/>"
                                                    data-nombre="<c:out value='${p.nombre}'/>"
                                                    data-descripcion="<c:out value='${p.descripcion}'/>"
                                                    data-precio="${p.precio}"
                                                    data-costo="${p.costo}"
                                                    data-stock="${p.stock}"
                                                    data-stock-min="${p.stockMinimo}"
                                                    data-categoria="${p.categoria.idCategoria}">
                                                Editar
                                            </button>
                                            <form action="${pageContext.request.contextPath}/productos" method="post"
                                                  onsubmit="return confirmarEliminacion(this, '¿Desactivar el producto? (eliminacion logica: UPDATE activo = 0)')">
                                                <input type="hidden" name="accion" value="desactivar">
                                                <input type="hidden" name="id" value="${p.idProducto}">
                                                <button type="submit" class="btn btn-secundario btn-pequeno">Desactivar</button>
                                            </form>
                                            <form action="${pageContext.request.contextPath}/productos" method="post"
                                                  onsubmit="return confirmarEliminacion(this, '¿Eliminar DEFINITIVAMENTE el producto? (DELETE; no se permite si ya se vendio)')">
                                                <input type="hidden" name="accion" value="eliminar">
                                                <input type="hidden" name="id" value="${p.idProducto}">
                                                <button type="submit" class="btn btn-peligro btn-pequeno">Eliminar</button>
                                            </form>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="etiqueta-alerta" title="El perfil CAJERO consulta, el ADMIN administra">Solo lectura</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty productos}">
                        <tr>
                            <td colspan="10">No hay productos registrados${not empty requestScope.q ? " para esa busqueda" : ""}.</td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </div>

    <c:if test="${esAdmin}">
    <div class="tarjeta">
        <h2 id="titulo-formulario">Nuevo producto</h2>
        <form action="${pageContext.request.contextPath}/productos" method="post" class="formulario" id="formulario-producto">
            <input type="hidden" name="accion" id="accion" value="agregar">
            <input type="hidden" name="id" id="id">

            <div class="campo">
                <!-- VALIDACION: 3 a 30 caracteres, con letras/numeros (el servidor la repite) -->
                <label for="codigo">Codigo del producto</label>
                <input type="text" id="codigo" name="codigo" required
                       placeholder="Ej.: A013" maxlength="30"
                       pattern="[A-Za-z0-9._-]{3,30}"
                       title="3 a 30 caracteres: letras, numeros, punto, guion o guion bajo">
            </div>
            <div class="campo">
                <!-- VALIDACION: 3 a 100 caracteres -->
                <label for="nombre">Nombre</label>
                <input type="text" id="nombre" name="nombre" required
                       placeholder="Ej.: Arroz 1 kg" maxlength="100">
            </div>
            <div class="campo">
                <!-- VALIDACION: maximo 100 caracteres; se valida tambien en el servidor -->
                <label for="descripcion">Descripcion / presentacion</label>
                <input type="text" id="descripcion" name="descripcion"
                       placeholder="Ej.: Bolsa de 1 kg" maxlength="100">
            </div>
            <div class="campo">
                <!-- VALIDACION: RANGO numero mayor a 0 (cliente: min 0.01 / servidor: precio <= 0) -->
                <label for="precio">Precio de venta (S/.)</label>
                <input type="number" id="precio" name="precio" step="0.01" min="0.01" max="9999999.99" required
                        placeholder="Ej.: 4.50">
            </div>
            <div class="campo">
                <!-- VALIDACION: RANGO numero no negativo (cliente: min 0 / servidor: costo < 0) -->
                <label for="costo">Costo de compra (S/.)</label>
                <input type="number" id="costo" name="costo" step="0.01" min="0" max="9999999.99" required
                        placeholder="Ej.: 3.80">
            </div>
            <div class="campo">
                <!-- VALIDACION: RANGO entero >= 0 -->
                <label for="stock">Stock</label>
                <input type="number" id="stock" name="stock" step="1" min="0" max="1000000" required
                       placeholder="Ej.: 50">
            </div>
            <div class="campo">
                <!-- VALIDACION: RANGO entero >= 1 (umbral de reposicion) -->
                <label for="stockMinimo">Stock minimo (alerta de reposicion)</label>
                <input type="number" id="stockMinimo" name="stockMinimo" step="1" min="1" max="1000000" required
                       placeholder="Ej.: 10">
            </div>
            <div class="campo">
                <label for="idcategoria">Categoria</label>
                <select id="idcategoria" name="idcategoria" required>
                    <option value="">-- Seleccionar --</option>
                    <c:forEach var="cat" items="${categorias}">
                        <option value="${cat.idCategoria}"><c:out value="${cat.descripcion}"/></option>
                    </c:forEach>
                </select>
            </div>

            <div class="grupo-botones">
                <button type="submit" class="btn btn-primario" id="boton-guardar">Agregar producto</button>
                <button type="button" class="btn btn-secundario" onclick="nuevoProducto()">Cancelar edicion</button>
            </div>
        </form>
    </div>
    </c:if>
</section>

<script>
    // Edicion: los datos viajan en los atributos data-* del boton
    // (escapados en la vista), no se concatenan dentro de onclick.
    document.querySelectorAll(".btn-editar").forEach(function (boton) {
        boton.addEventListener("click", function () {
            var d = boton.dataset;
            document.getElementById("accion").value = "modificar";
            document.getElementById("id").value = d.id;
            document.getElementById("codigo").value = d.codigo;
            document.getElementById("nombre").value = d.nombre;
            document.getElementById("descripcion").value = d.descripcion;
            document.getElementById("precio").value = d.precio;
            document.getElementById("costo").value = d.costo;
            document.getElementById("stock").value = d.stock;
            document.getElementById("stockMinimo").value = d.stockMin;
            document.getElementById("idcategoria").value = d.categoria;
            document.getElementById("titulo-formulario").textContent = "Editando producto #" + d.id;
            document.getElementById("boton-guardar").textContent = "Guardar cambios";
            document.getElementById("nombre").focus();
        });
    });

    function nuevoProducto() {
        document.getElementById("formulario-producto").reset();
        document.getElementById("accion").value = "agregar";
        document.getElementById("id").value = "";
        document.getElementById("titulo-formulario").textContent = "Nuevo producto";
        document.getElementById("boton-guardar").textContent = "Agregar producto";
    }
</script>

<jsp:include page="pie.jsp"/>
