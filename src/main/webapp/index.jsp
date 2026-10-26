<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    // Punto de entrada de la aplicacion: si hay sesion va al menu, si no, al login.
    if (session.getAttribute("usuario") != null) {
        response.sendRedirect(request.getContextPath() + "/menu");
    } else {
        response.sendRedirect(request.getContextPath() + "/login");
    }
%>