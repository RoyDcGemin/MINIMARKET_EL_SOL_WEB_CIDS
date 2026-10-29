<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${param.titulo != null ? param.titulo : 'Minimarket El Sol'}</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
</head>
<body>

<header class="cabecera">
    <div class="cabecera-contenido">
        <a href="${pageContext.request.contextPath}/menu" class="marca">
            <img src="${pageContext.request.contextPath}/img/icono-usuario2.png" alt="" class="marca-icono">
            <span class="marca-nombre">Minimarket El Sol</span>
        </a>
        <nav class="navegacion" aria-label="Navegacion principal">
            <a href="${pageContext.request.contextPath}/menu" class="enlace-nav">Inicio</a>
            <a href="${pageContext.request.contextPath}/venta" class="enlace-nav">Venta</a>
            <a href="${pageContext.request.contextPath}/cierre" class="enlace-nav">Cierre</a>
            <a href="${pageContext.request.contextPath}/productos" class="enlace-nav">Productos</a>
            <c:if test="${sessionScope.usuario.rol == 'ADMIN'}">
                <a href="${pageContext.request.contextPath}/categorias" class="enlace-nav">Categorias</a>
                <a href="${pageContext.request.contextPath}/reportes" class="enlace-nav">Reportes</a>
            </c:if>
        </nav>
        <div class="usuario-sesion">
            <span class="usuario-nombre" title="Rol: ${sessionScope.usuario.rol}">
                <c:out value="${sessionScope.usuario.nombre}"/>
            </span>
            <a href="${pageContext.request.contextPath}/cambiarpass" class="enlace-nav enlace-secundario">Cambiar contrasena</a>
            <a href="${pageContext.request.contextPath}/logout" class="btn btn-secundario btn-pequeno">Cerrar sesion</a>
        </div>
    </div>
</header>

<main class="contenido">
