<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="sol" uri="http://elsol/funciones" %>
<jsp:include page="cabecera.jsp">
    <jsp:param name="titulo" value="Cierre de caja"/>
</jsp:include>
<%@ include file="flash.jsp" %>

<h1 class="titulo-pagina">Cierre de caja</h1>

<!-- VISTA CIERRE (RF06): totales del turno. Solo cuentan VIGENTES. -->
<section class="tarjeta">
    <form action="${pageContext.request.contextPath}/cierre" method="get" class="formulario">
        <div class="campo">
            <label for="fecha">Fecha del turno</label>
            <input type="date" id="fecha" name="fecha" value="${fecha}">
        </div>
        <div class="grupo-botones">
            <button type="submit" class="btn btn-primario">Ver cierre</button>
        </div>
    </form>
</section>

<section class="parrilla-menu" aria-label="Resumen del dia">
    <div class="tarjeta">
        <h2>Total vendido (${fecha})</h2>
        <p class="total-facturacion">${sol:moneda(total)}</p>
    </div>
    <div class="tarjeta">
        <h2>Transacciones</h2>
        <p class="total-facturacion">${n}</p>
    </div>
    <div class="tarjeta">
        <h2>Ticket promedio</h2>
        <p class="total-facturacion">${sol:moneda(ticket)}</p>
    </div>
</section>

<section class="panel-lateral">
    <div class="tarjeta">
        <h2>Desglose por metodo de pago</h2>
        <div class="envoltorio-tabla">
            <table class="tabla">
                <thead>
                    <tr>
                        <th>Metodo</th>
                        <th class="texto-derecha">Transacciones</th>
                        <th class="texto-derecha">Total</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="e" items="${porMetodo}">
                        <tr>
                            <td><c:out value="${e.key}"/></td>
                            <td class="texto-derecha">${e.value[1]}</td>
                            <td class="texto-derecha monto">${sol:moneda(e.value[0])}</td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </div>

    <div class="tarjeta">
        <h2>Ventas del dia</h2>
        <div class="envoltorio-tabla">
            <table class="tabla">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Hora</th>
                        <th>Cajero</th>
                        <th>Estado</th>
                        <th class="texto-derecha">Total</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="v" items="${ventas}">
                        <tr>
                            <td>${v.idVenta}</td>
                            <td>${v.fecha}</td>
                            <td><c:out value="${v.usuario.nombre}"/></td>
                            <td><c:out value="${v.estado}"/></td>
                            <td class="texto-derecha monto">${sol:moneda(v.total)}</td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty ventas}">
                        <tr>
                            <td colspan="5">No hay ventas en esta fecha.</td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </div>
</section>

<jsp:include page="pie.jsp"/>
