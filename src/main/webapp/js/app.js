/* ============================================================================
   app.js - Utilidades del frontend (validacion ligera y confirmaciones)
   ============================================================================ */

/**
 * Pide confirmacion antes de ejecutar una accion destructiva (eliminar).
 * Uso en las vistas:
 *   <form action="..." method="post" onsubmit="return confirmarEliminacion(this, 'mensaje')">
 */
function confirmarEliminacion(formulario, mensaje) {
    return window.confirm(mensaje || "Esta seguro de que desea eliminar el registro?");
}

/**
 * Oculta automaticamente los mensajes flash de exito despues de unos segundos
 * para no entorpecer la lectura de la pagina.
 */
document.addEventListener("DOMContentLoaded", function () {
    var mensajes = document.querySelectorAll(".mensaje-exito");
    mensajes.forEach(function (mjs) {
        setTimeout(function () {
            mjs.style.transition = "opacity .4s ease";
            mjs.style.opacity = "0";
            setTimeout(function () { mjs.remove(); }, 450);
        }, 6000);
    });
});