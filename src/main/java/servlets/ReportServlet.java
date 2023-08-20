package servlets;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import beans.User;
import util.Response;
import com.google.gson.Gson;
import dao.PostDao;
import beans.Post;

/**
 * Servlet implementation class ReportServlet
 */
@WebServlet("/api/v1/reports/*")
public class ReportServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ReportServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.setStatus(404);
		response.setCharacterEncoding("utf-8");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	
	class RequestForm {
		public int post_id;
	}
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		if(request.getAttribute("user") == null) {
			Response.sendFaildResponse(response, 401, "you unauthorized to make this operation !");
			return;
		}
		User currentUser = (User)request.getAttribute("user");
		int postId = -1;
		try {
			postId = ((new Gson()).fromJson(request.getReader(), RequestForm.class)).post_id;
		} catch(Exception e) {
			Response.sendFaildResponse(response, 400, "date is not valid !");
			return;
		}
		if(postId == -1) {
			Response.sendFaildResponse(response, 400, "date is not valid !");
			return;
		}
		try {
			PostDao dao = new PostDao();
			Post post = dao.getPost(postId);
			if(post == null) {
				Response.sendFaildResponse(response, 400, "post_id is not valid !");
				return;
			}
			boolean ok = dao.createReport(currentUser.id, postId);
			dao.close();
			if(ok) {
				Response.sendSuccessedResponse(response, 201, "the report stored");
				return;
			} else {
				Response.sendFaildResponse(response, 500, "unknown error occurred !");
				return;
			}
		} catch(Exception e) {
			Response.sendFaildResponse(response, 500, "unknown error occurred !");
			return;
		}
	}

	/**
	 * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
