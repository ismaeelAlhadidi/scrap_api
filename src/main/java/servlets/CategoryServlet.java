package servlets;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import dao.CategoryDao;
import util.Response;
import java.util.List;

/**
 * Servlet implementation class CategoryServlet
 */
@WebServlet("/api/v1/categories")
public class CategoryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CategoryServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		List<String> categories = null;
		try {
			CategoryDao dao = new CategoryDao();
			categories = dao.getCategories();
			dao.close();
		} catch(Exception e) {
			Response.sendFaildResponse(response, 500, "unknown error occurred !");
			return;
		}
		if(categories == null) {
			Response.sendFaildResponse(response, 500, "unknown error occurred !");
		} else {
			Response.sendResponse(response, 200, categories);
		}
	}

}
