<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="cabecera.jsp">
    <jsp:param name="titulo" value="Menu"/>
</jsp:include>
<%@ include file="flash.jsp" %>

<!-- VISTA MENU: pagina principal del Minimarket El Sol.
     El menu cambia segun el PERFIL (rol) del usuario logueado:
       - ADMIN  : ve todos los modulos.
       - CAJERO : solo venta y cierre de caja. -->
<section class="tarjeta tarjeta-bienvenida">
    <h1 class="titulo-pagina">Bienvenido, <c:out value="${sessionScope.usuario.nombre}"/>!</h1>
    <p>Perfil: <strong><c:out value="${sessionScope.usuario.rol}"/></strong>. Elija una opcion para comenzar a trabajar.</p>
    <c:if test="${not empty stockBajo}">
        <p class="alerta-stock">⚠ Hay ${stockBajo.size()} producto(s) con stock bajo que requieren reposicion.
            <a href="${pageContext.request.contextPath}/productos">Ver productos</a>
        </p>
    </c:if>
</section>

<section class="parrilla-menu" aria-label="Opciones del sistema">
    <a href="${pageContext.request.contextPath}/venta" class="tarjeta tarjeta-opcion">
        <span class="icono-opcion">&#128176;</span>
        <h2>Registrar Venta</h2>
        <p>Venda por codigo o descripcion, calcule el total y emita el comprobante.</p>
    </a>
    <a href="${pageContext.request.contextPath}/cierre" class="tarjeta tarjeta-opcion">
        <span class="icono-opcion">&#128187;</span>
        <h2>Cierre de Caja</h2>
        <p>Total del turno, transacciones y desglose por metodo de pago.</p>
    </a>

    <c:if test="${sessionScope.usuario.rol == 'ADMIN'}">
        <a href="${pageContext.request.contextPath}/reportes" class="tarjeta tarjeta-opcion">
            <span class="icono-opcion">&#128202;</span>
            <h2>Reportes</h2>
            <p>Ventas por fecha, cajero y producto, con indicadores.</p>
        </a>
        <a href="${pageContext.request.contextPath}/productos" class="tarjeta tarjeta-opcion">
            <span class="icono-opcion">&#128722;</span>
            <h2>Ver Productos</h2>
            <p>Administre el catalogo y el stock disponible.</p>
        </a>
        <a href="${pageContext.request.contextPath}/categorias" class="tarjeta tarjeta-opcion">
            <span class="icono-opcion">&#128193;</span>
            <h2>Categorias</h2>
            <p>Clasifique los productos por categoria.</p>
        </a>
    </c:if>
</section>

<jsp:include page="pie.jsp"/>

