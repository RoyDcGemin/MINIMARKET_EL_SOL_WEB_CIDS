<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!-- Mensajes "flash" (una sola vez) y errores de validacion -->
<c:if test="${not empty sessionScope.flash}">
    <div class="mensaje mensaje-${sessionScope.flash[0]}" role="status">
        <c:out value="${sessionScope.flash[1]}"/>
    </div>
    <c:remove var="flash" scope="session"/>
</c:if>
<c:if test="${not empty requestScope.error}">
    <div class="mensaje mensaje-error" role="alert">
        <c:out value="${requestScope.error}"/>
    </div>
</c:if>
