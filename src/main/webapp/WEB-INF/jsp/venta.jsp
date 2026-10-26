<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="sol" uri="http://elsol/funciones" %>
<jsp:include page="cabecera.jsp">
    <jsp:param name="titulo" value="Registrar Venta"/>
</jsp:include>
<%@ include file="flash.jsp" %>

<h1 class="titulo-pagina">Registrar Venta</h1>

<!-- VISTA VENTA: punto de venta de la Minimarket El Sol.
     El personal digita el CODIGO del producto + OK (emula el scanner),
     indica la CANTIDAD y el carrito vive en la sesion del servidor. -->
<section class="panel-lateral">

    <!-- Columna izquierda: agregar productos por codigo -->
    <div class="tarjeta">
        <h2>Agregar producto (venta N° ${siguienteId})</h2>
        <form action="${pageContext.request.contextPath}/venta" method="post" class="formulario">
            <input type="hidden" name="accion" value="agregar">

            <div class="campo">
                <label for="codigo">Codigo del producto o principio activo</label>
                <input type="text" id="codigo" name="codigo" required autofocus
                       placeholder="Ej.: F001 o paracetamol" maxlength="100" autocomplete="off">
                <span class="ayuda-campo" id="info-producto">Digite el codigo y pulse OK (emula el scanner). Tambien acepta principio activo si es unico.</span>
            </div>
            <div class="campo">
                <label for="cantidad">Cantidad</label>
                <input type="number" id="cantidad" name="cantidad" min="1" step="1" required
                       placeholder="Ej.: 2">
            </div>

            <div class="grupo-botones">
                <button type="submit" class="btn btn-primario">OK - Agregar</button>
            </div>
        </form>

        <h2>Pool de productos del minimarket</h2>
        <form action="${pageContext.request.contextPath}/venta" method="get" class="formulario">
            <div class="campo">
                <label for="q">Buscar por codigo, nombre o principio activo</label>
                <input type="text" id="q" name="q" value="<c:out value='${q}'/>"
                       placeholder="Ej.: F001, paracetamol, Panadol" maxlength="100" autocomplete="off">
            </div>
            <div class="grupo-botones">
                <button type="submit" class="btn btn-secundario">Buscar</button>
                <a href="${pageContext.request.contextPath}/venta" class="btn btn-secundario">Ver todos</a>
            </div>
        </form>
        <div class="envoltorio-tabla">
            <table class="tabla">
                <thead>
                    <tr>
                        <th>Codigo</th>
                        <th>Nombre comercial</th>
                        <th>Principio activo</th>
                        <th class="texto-derecha">Precio</th>
                        <th class="texto-derecha">Stock</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="p" items="${productos}">
                        <tr>
                            <td><strong><c:out value="${p.codigo}"/></strong></td>
                            <td><c:out value="${p.nombre}"/></td>
                            <td><c:out value="${p.principioActivo}"/></td>
                            <td class="texto-derecha monto">${sol:moneda(p.precio)}</td>
                            <td class="texto-derecha">
                                ${p.stock}
                                <c:if test="${p.stock <= p.stockMinimo}">
                                    <span class="etiqueta-alerta">Stock bajo</span>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty productos}">
                        <tr>
                            <td colspan="5">No hay productos registrados.</td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </div>

    <!-- Columna derecha: carrito + datos del cliente y cobro -->
    <div class="tarjeta">
        <h2>Carrito de la venta</h2>
        <div class="envoltorio-tabla">
            <table class="tabla">
                <thead>
                    <tr>
                        <th>Codigo</th>
                        <th>Producto</th>
                        <th class="texto-derecha">Cantidad</th>
                        <th class="texto-derecha">Precio</th>
                        <th class="texto-derecha">Subtotal</th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="d" items="${carrito}" varStatus="pos">
                        <tr>
                            <td><c:out value="${d.producto.codigo}"/></td>
                            <td><c:out value="${d.producto.nombre}"/></td>
                            <td class="texto-derecha">${d.cantidad}</td>
                            <td class="texto-derecha monto">${sol:moneda(d.producto.precio)}</td>
                            <td class="texto-derecha monto">${sol:moneda(d.subtotal)}</td>
                            <td>
                                <a class="btn btn-peligro btn-pequeno"
                                   href="${pageContext.request.contextPath}/venta?accion=quitar&i=${pos.index}">Quitar</a>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty carrito}">
                        <tr>
                            <td colspan="6">El carrito esta vacio.</td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>

        <p class="total-facturacion">
            Subtotal: ${sol:moneda(subtotalCarrito)}
        </p>

        <div class="grupo-botones">
            <a href="${pageContext.request.contextPath}/venta?accion=limpiar" class="btn btn-secundario">Limpiar carrito</a>
        </div>

        <h2>Cliente y cobro</h2>
        <form action="${pageContext.request.contextPath}/venta" method="post"
              class="formulario" onsubmit="return validarVenta()">
            <input type="hidden" name="accion" value="vender">

            <div class="campo">
                <label for="dni">DNI del cliente</label>
                <input type="text" id="dni" name="dni" required
                       placeholder="8 digitos" maxlength="8" pattern="[0-9]{8}"
                       list="lista-dnis" autocomplete="off">
                <datalist id="lista-dnis">
                    <c:forEach var="s" items="${dniSugeridos}">
                        <option value="${s[0]}">${s[1]}</option>
                    </c:forEach>
                </datalist>
            </div>
            <div class="campo">
                <label for="nombre">Nombre del cliente</label>
                <input type="text" id="nombre" name="nombre" required
                       placeholder="Nombres y apellidos" maxlength="100">
            </div>
            <div class="campo">
                <label for="descuento">Descuento (S/.)</label>
                <input type="number" id="descuento" name="descuento" step="0.01" min="0"
                       value="0" placeholder="0.00">
            </div>
            <div class="campo">
                <label for="metodo">Metodo de pago</label>
                <select id="metodo" name="metodo" required>
                    <option value="">-- Seleccionar --</option>
                    <c:forEach var="m" items="${metodos}">
                        <option value="${m}">${m}</option>
                    </c:forEach>
                </select>
            </div>

            <button type="submit" class="btn btn-primario btn-ancho"
                    <c:if test="${empty carrito}">disabled</c:if>>Registrar venta</button>
        </form>
    </div>

</section>

<script>
    // Datos del pool armados en el servidor para mostrar info al digitar.
    const PRODUCTOS = {
        <c:forEach var="p" items="${productos}">"${p.codigo}": {nombre: "${p.nombre}", precio: ${p.precio}, stock: ${p.stock}},
        </c:forEach>
    };

    document.getElementById("codigo").addEventListener("input", function () {
        var info = document.getElementById("info-producto");
        var p = PRODUCTOS[this.value.trim()];
        info.textContent = (p !== undefined)
            ? p.nombre + " - S/. " + p.precio.toFixed(2) + " (stock: " + p.stock + ")"
            : "Digite el codigo y pulse OK (emula el scanner).";
    });

    // DNI: solo digitos, 8 posiciones (ademas existe en el servidor).
    function validarVenta() {
        var dni = document.getElementById("dni").value.trim();
        if (!/^[0-9]{8}$/.test(dni)) {
            window.alert("El DNI debe tener 8 digitos");
            return false;
        }
        var desc = parseFloat(document.getElementById("descuento").value || "0");
        if (isNaN(desc) || desc < 0) {
            window.alert("El descuento no puede ser negativo");
            return false;
        }
        return true;
    }
</script>

<jsp:include page="pie.jsp"/>
