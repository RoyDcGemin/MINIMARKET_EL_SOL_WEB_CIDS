<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="cabecera.jsp">
    <jsp:param name="titulo" value="Categorias"/>
</jsp:include>
<%@ include file="flash.jsp" %>

<h1 class="titulo-pagina">Categorias</h1>

<!-- VISTA NUEVA: las categorias no tenian pantalla en el proyecto de escritorio. -->
<section class="panel-lateral">
    <div class="tarjeta">
        <h2>Lista de categorias</h2>
        <div class="envoltorio-tabla">
            <table class="tabla">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Descripcion</th>
                        <th>Acciones</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="cat" items="${categorias}">
                        <tr>
                            <td>${cat.idCategoria}</td>
                            <td><c:out value="${cat.descripcion}"/></td>
                            <td>
                                <div class="grupo-botones">
                                    <button type="button" class="btn btn-secundario btn-pequeno"
                                            onclick="editarCategoria(${cat.idCategoria}, '${cat.descripcion}')">
                                        Editar
                                    </button>
                                    <form action="${pageContext.request.contextPath}/categorias" method="post"
                                          onsubmit="return confirmarEliminacion(this, 'Esta seguro de eliminar la categoria ${cat.descripcion}?')">
                                        <input type="hidden" name="accion" value="eliminar">
                                        <input type="hidden" name="id" value="${cat.idCategoria}">
                                        <button type="submit" class="btn btn-peligro btn-pequeno">Eliminar</button>
                                    </form>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty categorias}">
                        <tr>
                            <td colspan="3">No hay categorias registradas.</td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </div>

    <div class="tarjeta">
        <h2 id="titulo-formulario">Nueva categoria</h2>
        <form action="${pageContext.request.contextPath}/categorias" method="post" class="formulario" id="formulario-categoria">
            <input type="hidden" name="accion" id="accion" value="agregar">
            <input type="hidden" name="id" id="id">

            <div class="campo">
                <label for="descripcion">Descripcion</label>
                <input type="text" id="descripcion" name="descripcion" required
                       placeholder="Ej.: Analgesicos, Antibioticos..." maxlength="60">
            </div>

            <div class="grupo-botones">
                <button type="submit" class="btn btn-primario" id="boton-guardar">Agregar categoria</button>
                <button type="button" class="btn btn-secundario" onclick="nuevaCategoria()">Cancelar edicion</button>
            </div>
        </form>
    </div>
</section>

<script>
    function editarCategoria(id, descripcion) {
        document.getElementById("accion").value = "modificar";
        document.getElementById("id").value = id;
        document.getElementById("descripcion").value = descripcion;
        document.getElementById("titulo-formulario").textContent = "Editando categoria #" + id;
        document.getElementById("boton-guardar").textContent = "Guardar cambios";
        document.getElementById("descripcion").focus();
    }

    function nuevaCategoria() {
        document.getElementById("formulario-categoria").reset();
        document.getElementById("accion").value = "agregar";
        document.getElementById("id").value = "";
        document.getElementById("titulo-formulario").textContent = "Nueva categoria";
        document.getElementById("boton-guardar").textContent = "Agregar categoria";
    }
</script>

<jsp:include page="pie.jsp"/>