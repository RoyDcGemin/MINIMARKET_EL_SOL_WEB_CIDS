<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Iniciar sesion - Minimarket El Sol</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
</head>
<body class="cuerpo-login">

<!-- VISTA LOGIN: reemplaza a la JFrame Logueo. La validacion la hace
     UsuarioController (igual que antes); aqui solo se capturan los datos. -->
<main class="tarjeta-login">
    <img src="${pageContext.request.contextPath}/img/icono-usuario2.png" alt="" class="logotipo-login">
    <h1 class="titulo-login">Minimarket El Sol</h1>
    <p class="subtitulo-login">Ingrese sus credenciales para continuar</p>

    <c:if test="${not empty requestScope.error}">
        <div class="mensaje mensaje-error" role="alert">
            <c:out value="${requestScope.error}"/>
        </div>
    </c:if>

    <form action="${pageContext.request.contextPath}/login" method="post" class="formulario" novalidate>
        <div class="campo">
            <label for="usuario">Usuario</label>
            <input type="text" id="usuario" name="usuario" required autofocus
                   placeholder="Su nombre de usuario"
                   value="${requestScope.usuarioIngresado}">
        </div>

        <div class="campo">
            <label for="clave">Contrasena</label>
            <input type="password" id="clave" name="clave" required
                   placeholder="Su contrasena">
        </div>

        <button type="submit" class="btn btn-primario btn-ancho">Iniciar sesion</button>
    </form>

    <!-- Opcion de REGISTRAR NUEVOS USUARIOS pedia en la indicacion. -->
    <p class="nota-login">
        No tiene cuenta? <a href="${pageContext.request.contextPath}/registro">Crear cuenta</a>
    </p>

    <p class="nota-login">Acceso de prueba: <strong>admin</strong> / <strong>Admin#2026</strong> · cajero <strong>cajero</strong> / <strong>Cajero2026!</strong></p>
</main>

<script src="${pageContext.request.contextPath}/js/app.js"></script>
</body>
</html>