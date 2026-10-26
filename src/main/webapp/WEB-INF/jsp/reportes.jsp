<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="sol" uri="http://elsol/funciones" %>
<jsp:include page="cabecera.jsp">
    <jsp:param name="titulo" value="Reportes"/>
</jsp:include>
<%@ include file="flash.jsp" %>

<h1 class="titulo-pagina">Reportes de ventas</h1>

<!-- Tarjetas de indicadores (TPRV, %ERR, TPV, %OK) sobre el filtro actual. -->
<section class="parrilla-menu" aria-label="Indicadores">
    <div class="tarjeta">
        <h2>Tiempo promedio (TPRV)</h2>
        <p class="total-facturacion">${tprv} seg/venta</p>
    </div>
    <div class="tarjeta">
        <h2>Errores (%ERR)</h2>
        <p class="total-facturacion">${porcErr} %</p>
    </div>
    <div class="tarjeta">
        <h2>Ticket promedio (TPV)</h2>
        <p class="total-facturacion">${sol:moneda(tpv)}</p>
    </div>
    <div class="tarjeta">
        <h2>Ventas correctas (%OK)</h2>
        <p class="total-facturacion">${porcOk} %</p>
    </div>
</section>

<!-- REPORTES DE LA PLANTILLA (recuadro):
     maximo/minimo/promedio, ingresos/utilidad/stock y registros eliminados. -->
<section class="parrilla-menu" aria-label="Reportes solicitados">
    <div class="tarjeta">
        <h2>Maximo, minimo y promedio</h2>
        <table class="tabla">
            <tbody>
                <tr><td>Venta maxima</td><td class="texto-derecha monto">${sol:moneda(maxVenta)}</td></tr>
                <tr><td>Venta minima</td><td class="texto-derecha monto">${sol:moneda(minVenta)}</td></tr>
                <tr><td>Venta promedio</td><td class="texto-derecha monto">${sol:moneda(promVenta)}</td></tr>
            </tbody>
        </table>
        <p class="ayuda-campo">Calculado con MAX, MIN y AVG de SQL sobre la columna total.</p>
    </div>
    <div class="tarjeta">
        <h2>Ingresos, utilidad y unidades</h2>
        <table class="tabla">
            <tbody>
                <tr><td>Ingresos del periodo</td><td class="texto-derecha monto">${sol:moneda(ingresos)}</td></tr>
                <tr><td>Utilidad (precio - costo)</td><td class="texto-derecha monto">${sol:moneda(utilidad)}</td></tr>
                <tr><td>Unidades vendidas</td><td class="texto-derecha">${unidades}</td></tr>
            </tbody>
        </table>
        <p class="ayuda-campo">JOIN de detalle_venta + venta + producto.</p>
    </div>
</section>

<!-- REPORTE DE REGISTROS ELIMINADOS (eliminacion logica: activo = 0) -->
<section class="tarjeta">
    <h2>Registros eliminados (eliminacion logica)</h2>
    <div class="envoltorio-tabla">
        <table class="tabla">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Codigo</th>
                    <th>Nombre</th>
                    <th class="texto-derecha">Precio</th>
                    <th class="texto-derecha">Stock</th>
                    <th>Accion</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="p" items="${inactivos}">
                    <tr>
                        <td>${p.idProducto}</td>
                        <td><c:out value="${p.codigo}"/></td>
                        <td><c:out value="${p.nombre}"/></td>
                        <td class="texto-derecha monto">${sol:moneda(p.precio)}</td>
                        <td class="texto-derecha">${p.stock}</td>
                        <td>
                            <form action="${pageContext.request.contextPath}/productos" method="post">
                                <input type="hidden" name="accion" value="activar">
                                <input type="hidden" name="id" value="${p.idProducto}">
                                <button type="submit" class="btn btn-secundario btn-pequeno">Reactivar</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty inactivos}">
                    <tr><td colspan="6">No hay registros desactivados.</td></tr>
                </c:if>
            </tbody>
        </table>
    </div>
    <p class="ayuda-campo">Estos registros NO se borraron: tienen activo = 0 (UPDATE).</p>
</section>

<!-- VISTA REPORTES: filtros por fecha, cajero y producto. -->
<section class="panel-lateral">
    <div class="tarjeta">
        <h2>Filtros</h2>
        <form action="${pageContext.request.contextPath}/reportes" method="get" class="formulario">
            <div class="campo">
                <label for="desde">Desde</label>
                <input type="date" id="desde" name="desde" value="${desde}">
            </div>
            <div class="campo">
                <label for="hasta">Hasta</label>
                <input type="date" id="hasta" name="hasta" value="${hasta}">
            </div>
            <div class="campo">
                <label for="cajero">Cajero</label>
                <select id="cajero" name="cajero">
                    <option value="0">Todos</option>
                    <c:forEach var="u" items="${cajeros}">
                        <option value="${u.idusuario}" <c:if test="${u.idusuario == idCajero}">selected</c:if>>
                            <c:out value="${u.nombre}"/>
                        </option>
                    </c:forEach>
                </select>
            </div>
            <div class="campo">
                <label for="producto">Producto</label>
                <select id="producto" name="producto">
                    <option value="0">Todos</option>
                    <c:forEach var="p" items="${productos}">
                        <option value="${p.idProducto}" <c:if test="${p.idProducto == idProducto}">selected</c:if>>
                            <c:out value="${p.nombre}"/>
                        </option>
                    </c:forEach>
                </select>
            </div>
            <div class="grupo-botones">
                <button type="submit" class="btn btn-primario">Filtrar</button>
                <!-- Apache POI: descarga el mismo reporte en Excel (.xlsx) -->
                <a class="btn btn-secundario"
                   href="${pageContext.request.contextPath}/reportes/excel?desde=${desde}&amp;hasta=${hasta}">
                    Exportar a Excel (POI)
                </a>
            </div>
        </form>

        <h2>Ventas (${ventas.size()} resultados)</h2>
        <div class="envoltorio-tabla">
            <table class="tabla">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Fecha</th>
                        <th>Cajero</th>
                        <th>Cliente</th>
                        <th>Estado</th>
                        <th class="texto-derecha">Total</th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="v" items="${ventas}">
                        <tr>
                            <td>${v.idVenta}</td>
                            <td>${v.fecha}</td>
                            <td><c:out value="${v.usuario.nombre}"/></td>
                            <td><c:out value="${v.nombreCliente}"/> (${v.dniCliente})</td>
                            <td><c:out value="${v.estado}"/></td>
                            <td class="texto-derecha monto">${sol:moneda(v.total)}</td>
                            <td>
                                <div class="grupo-botones">
                                    <a class="btn btn-secundario btn-pequeno"
                                       href="${pageContext.request.contextPath}/reportes?ver=${v.idVenta}&desde=${desde}&hasta=${hasta}&cajero=${idCajero}&producto=${idProducto}">Ver detalle</a>
                                    <c:if test="${v.estado == 'VIGENTE'}">
                                        <form action="${pageContext.request.contextPath}/reportes" method="post"
                                              onsubmit="return confirmarEliminacion(this, 'Anular la venta N° ${v.idVenta}?')">
                                            <input type="hidden" name="accion" value="anular">
                                            <input type="hidden" name="id" value="${v.idVenta}">
                                            <button type="submit" class="btn btn-peligro btn-pequeno">Anular</button>
                                        </form>
                                    </c:if>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty ventas}">
                        <tr>
                            <td colspan="7">No hay ventas para este filtro.</td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </div>

    <div class="tarjeta">
        <h2>Detalle de la venta</h2>
        <c:choose>
            <c:when test="${detalle != null}">
                <p><strong>VENTA N° ${detalle.idVenta}</strong> · ${detalle.fecha}</p>
                <p>Cliente: <c:out value="${detalle.nombreCliente}"/> (${detalle.dniCliente})</p>
                <p>Estado: <c:out value="${detalle.estado}"/> · Pago: <c:out value="${detalle.metodoPago}"/></p>

                <div class="envoltorio-tabla">
                    <table class="tabla">
                        <thead>
                            <tr>
                                <th>Producto</th>
                                <th class="texto-derecha">Cant.</th>
                                <th class="texto-derecha">Precio</th>
                                <th class="texto-derecha">Subtotal</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="d" items="${detalle.detalles}">
                                <tr>
                                    <td><c:out value="${d.producto.nombre}"/></td>
                                    <td class="texto-derecha">${d.cantidad}</td>
                                    <td class="texto-derecha monto">${sol:moneda(d.producto.precio)}</td>
                                    <td class="texto-derecha monto">${sol:moneda(d.subtotal)}</td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
                <p class="total-facturacion">
                    Descuento: ${sol:moneda(detalle.descuento)} ·
                    TOTAL: ${sol:moneda(detalle.total)}
                </p>
                <a href="${pageContext.request.contextPath}/comprobante?id=${detalle.idVenta}" class="btn btn-secundario">
                    Descargar PDF
                </a>
            </c:when>
            <c:otherwise>
                <p class="ayuda-campo">Seleccione una venta de la lista para ver su detalle.</p>
            </c:otherwise>
        </c:choose>
    </div>
</section>

<jsp:include page="pie.jsp"/>
