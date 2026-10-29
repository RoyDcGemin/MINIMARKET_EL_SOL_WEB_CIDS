<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="cabecera.jsp">
    <jsp:param name="titulo" value="Cambiar contrasena"/>
</jsp:include>
<%@ include file="flash.jsp" %>

<h1 class="titulo-pagina">Cambiar contrasena</h1>

<section class="tarjeta tarjeta-cambiar-pass">
    <form action="${pageContext.request.contextPath}/cambiarpass" method="post"
          class="formulario" id="formulario-cambiar-pass" novalidate>
        <div class="campo">
            <label for="claveActual">Contrasena actual</label>
            <input type="password" id="claveActual" name="claveActual" required autocomplete="current-password">
        </div>
        <div class="campo">
            <!-- POLITICA DE CONTRASENA: el servidor la repite (PasswordPolicy). -->
            <label for="claveNueva">Nueva contrasena</label>
            <input type="password" id="claveNueva" name="claveNueva" required autocomplete="new-password"
                   aria-describedby="ayuda-clave-nueva"
                   minlength="8" maxlength="64"
                   pattern="(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[^A-Za-z0-9]).{8,64}"
                   title="Minimo 8 caracteres, con minuscula, mayuscula, numero y simbolo especial">
            <span class="ayuda-campo" id="ayuda-clave-nueva">Minimo 8 caracteres, con una minuscula, una mayuscula, un numero y un simbolo especial.</span>
        </div>
        <div class="campo">
            <label for="claveNueva2">Confirmar nueva contrasena</label>
            <input type="password" id="claveNueva2" name="claveNueva2" required autocomplete="new-password">
        </div>

        <div class="grupo-botones">
            <button type="submit" class="btn btn-primario">Actualizar contrasena</button>
        </div>
    </form>
</section>

<script>
    // Validacion de coincidencia en el navegador (ademas existe en el servidor).
    document.getElementById("formulario-cambiar-pass").addEventListener("submit", function (evt) {
        var nueva = document.getElementById("claveNueva").value;
        var confirmacion = document.getElementById("claveNueva2").value;
        if (nueva !== confirmacion) {
            evt.preventDefault();
            window.alert("La confirmacion no coincide con la nueva contrasena");
        }
    });
</script>

<jsp:include page="pie.jsp"/>