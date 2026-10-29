<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Crear cuenta - Minimarket El Sol</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
</head>
<body class="cuerpo-login">

<!-- VISTA REGISTRO: opcion de "registrar nuevos usuarios" que pide la
     indicacion. La validacion la hace UsuarioController.registrar() y
     aqui solo se capturan los datos. El rol asignado es SIEMPRE CAJERO
     (se fuerza en el servidor, no existe un campo de rol en el form). -->
<main class="tarjeta-login">
    <img src="${pageContext.request.contextPath}/img/icono-usuario2.png" alt="" class="logotipo-login">
    <h1 class="titulo-login">Crear cuenta</h1>
    <p class="subtitulo-login">Minimarket El Sol - nuevo usuario de caja</p>

    <c:if test="${not empty requestScope.error}">
        <div class="mensaje mensaje-error" role="alert">
            <c:out value="${requestScope.error}"/>
        </div>
    </c:if>

    <!-- POLITICA DE CONTRASENA en el cliente: minimo 8, minuscula,
         mayuscula, numero y simbolo (el servidor la repite). -->
    <form action="${pageContext.request.contextPath}/registro" method="post" class="formulario" novalidate>
        <div class="campo">
            <label for="usuario">Usuario</label>
            <input type="text" id="usuario" name="usuario" required autofocus
                   placeholder="Ej.: cajero2" maxlength="50"
                   pattern="[A-Za-z0-9._-]{3,50}"
                   title="3 a 50 caracteres: letras, numeros, punto, guion o guion bajo"
                   value="${requestScope.usuarioIngresado}">
        </div>

        <div class="campo">
            <label for="nombre">Nombre completo</label>
            <input type="text" id="nombre" name="nombre" required
                   placeholder="Ej.: Maria Perez" maxlength="100"
                   value="${requestScope.nombreIngresado}">
        </div>

        <div class="campo">
            <label for="clave">Contrasena</label>
            <input type="password" id="clave" name="clave" required
                   placeholder="Su contrasena" minlength="8" maxlength="64"
                   pattern="(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[^A-Za-z0-9]).{8,64}"
                   title="Minimo 8 caracteres, con minuscula, mayuscula, numero y simbolo especial">
        </div>

        <div class="campo">
            <label for="clave2">Confirmar contrasena</label>
            <input type="password" id="clave2" name="clave2" required
                   placeholder="Repita su contrasena" minlength="8" maxlength="64">
        </div>

        <button type="submit" class="btn btn-primario btn-ancho">Crear cuenta</button>
    </form>

    <p class="ayuda-campo">La cuenta se crea con perfil CAJERO (consulta y venta).
        Las cuentas ADMIN las crea el administrador del sistema.</p>

    <p class="nota-login">
        <a href="${pageContext.request.contextPath}/login">Volver al inicio de sesion</a>
    </p>
</main>

<script src="${pageContext.request.contextPath}/js/app.js"></script>
</body>
</html>
