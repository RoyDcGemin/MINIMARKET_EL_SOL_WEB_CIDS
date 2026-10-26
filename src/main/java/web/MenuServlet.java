package web;

import controller.ProductoController;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Pagina principal (menu) de la aplicacion web.
 * Muestra el conteo de productos con stock bajo para la Minimarket.
 */
@WebServlet(name = "MenuServlet", urlPatterns = {"/menu", "/inicio"})
public class MenuServlet extends HttpServlet {

    private final ProductoController productoController = new ProductoController();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("stockBajo", productoController.listarStockBajo());
        req.getRequestDispatcher("/WEB-INF/jsp/menu.jsp").forward(req, resp);
    }
}