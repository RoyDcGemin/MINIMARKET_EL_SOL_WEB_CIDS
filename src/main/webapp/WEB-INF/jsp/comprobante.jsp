<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="sol" uri="http://elsol/funciones" %>
<jsp:include page="cabecera.jsp">
    <jsp:param name="titulo" value="Comprobante"/>
</jsp:include>

<div class="mensaje mensaje-exito" role="status">
    Venta registrada correctamente.
</div>

<!-- VISTA COMPROBANTE: muestra la venta y ofrece la descarga en PDF. -->
<section class="tarjeta tarjeta-boleta">
    <h1 class="titulo-pagina">MINIMARKET EL SOL</h1>
    <p>Av. Los Alamos 123 - Surco, Lima</p>
    <h2>COMPROBANTE DE VENTA N° ${venta.idVenta}</h2>

    <table class="tabla tabla-boleta-datos">
        <tbody>
            <tr>
                <td class="celda-etiqueta">Fecha</td>
                <td>${venta.fecha}</td>
            </tr>
            <tr>
                <td class="celda-etiqueta">Cajero</td>
                <td><c:out value="${venta.usuario.nombre}"/></td>
            </tr>
            <tr>
                <td class="celda-etiqueta">Cliente</td>
                <td><c:out value="${venta.nombreCliente}"/> (DNI ${venta.dniCliente})</td>
            </tr>
            <tr>
                <td class="celda-etiqueta">Metodo de pago</td>
                <td><c:out value="${venta.metodoPago}"/></td>
            </tr>
            <tr>
                <td class="celda-etiqueta">Estado</td>
                <td><c:out value="${venta.estado}"/></td>
            </tr>
        </tbody>
    </table>

    <h2>Detalles</h2>
    <div class="envoltorio-tabla">
        <table class="tabla">
            <thead>
                <tr>
                    <th>Producto</th>
                    <th class="texto-derecha">Cantidad</th>
                    <th class="texto-derecha">Precio</th>
                    <th class="texto-derecha">Subtotal</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="d" items="${venta.detalles}">
                    <tr>
                        <td><c:out value="${d.producto.nombre}"/></td>
                        <td class="texto-derecha">${d.cantidad}</td>
                        <td class="texto-derecha monto">${sol:moneda(d.producto.precio)}</td>
                        <td class="texto-derecha monto">${sol:moneda(d.subtotal)}</td>
                    </tr>
                </c:forEach>
            </tbody>
            <tfoot>
                <tr>
                    <th colspan="3" class="texto-derecha">Descuento</th>
                    <th class="texto-derecha monto">${sol:moneda(venta.descuento)}</th>
                </tr>
                <tr>
                    <th colspan="3" class="texto-derecha">TOTAL</th>
                    <th class="texto-derecha monto">${sol:moneda(venta.total)}</th>
                </tr>
            </tfoot>
        </table>
    </div>

    <p>Gracias por su compra</p>

    <div class="grupo-botones">
        <a href="${pageContext.request.contextPath}/comprobante?id=${venta.idVenta}" class="btn btn-secundario">Descargar PDF</a>
        <a href="${pageContext.request.contextPath}/venta" class="btn btn-primario">Nueva venta</a>
        <a href="${pageContext.request.contextPath}/reportes" class="btn btn-secundario">Ver reportes</a>
    </div>
</section>

<jsp:include page="pie.jsp"/>
