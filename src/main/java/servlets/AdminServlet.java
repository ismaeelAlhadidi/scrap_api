package servlets;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import beans.Admin;
import beans.User;
import util.Response;
import dao.AdminDao;
import servlets.SessionServlet.ResponseBody;

import java.util.List;
import beans.Post;
/**
 * Servlet implementation class AdminServlet
 */
@WebServlet("/api/v1/admins/*")
public class AdminServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AdminServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		if(request.getAttribute("admin") == null) {
			Response.sendFaildResponse(response, 401, "you unauthorized to make this operation !");
			return;
		}
		Admin currentAdmin = (Admin)request.getAttribute("admin");
		String[] path = request.getPathInfo().split("/");
		if(path.length > 1 && path[1].equals("posts")) {
			try {
				AdminDao dao = new AdminDao();
				String temp = request.getParameter("page");
				int page = -1;
				if(temp != null) {
					try {
						page = Integer.parseInt(temp);
						--page;
						if(page < 0) {
							throw new Exception();
						}
					} catch(Exception e) {
						Response.sendFaildResponse(response, 500, "page not valid !");
						return;
					}
				}
				if(page == -1) page = 0;
				List<Post> posts = dao.getPosts(page);
				int pages = dao.getPages();
				String prev_page = (page == 0) ? "" : (page+"");
				String current_page = (page+1)+"";
				String next_page = (page+1) >= pages ? "" : ((page+2)+"");
				dao.close();
				Response.sendResponse(response, 200, new resources.PostsResource(posts, pages, prev_page, current_page, next_page));
			} catch(Exception e) {
				Response.sendFaildResponse(response, 500, "unknown error occurred !");
				return;
			}
		} else {
			response.setStatus(404);
		}
	}

	class ResponseBody {
    	public String jwt;
    }
	
	protected void refreshToken(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(request.getAttribute("admin") == null) {
			Response.sendFaildResponse(response, 401, "you unauthorized to make this operation !");
			return;
		}
		Admin currentAdmin = (Admin)request.getAttribute("admin");
		HashMap<String, String> claims = new HashMap<String, String>();
		claims.put("admin_id", currentAdmin.id+"");
		String token = null;
		try {
			token = util.JwtGenerator.generateJwt(claims);
		} catch(Exception e) {
			Response.sendFaildResponse(response, 500, "unknown error occurred !");
			return;
		}
		ResponseBody responseBody = new ResponseBody();
		responseBody.jwt = token;
		Response.sendResponse(response, 200, responseBody);
	}
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String[] path = request.getPathInfo().split("/");
		if(path.length > 1 && path[1].equals("sessions")) {
			if(path.length > 2 && path[2].equals("refresh")) {
				refreshToken(request, response);
				return;
			}
			Admin admin = null;
			try {
				admin = (new Gson()).fromJson(request.getReader(), Admin.class);
			} catch(Exception e) {
				Response.sendFaildResponse(response, 400, "date is not valid !");
				return;
			}
			if(admin == null || admin.email == null || admin.password == null) {
				Response.sendFaildResponse(response, 400, "date is not valid !");
				return;
			}
			if(admin.email.length() > 254 || ! util.Validator.isValidMail(admin.email)) {
				Response.sendFaildResponse(response, 400, "date is not valid !");
				return;
			}
			if(admin.password.length() > 254 || admin.password.length() < 1) {
				Response.sendFaildResponse(response, 400, "date is not valid !");
				return;
			}
			try {
				AdminDao dao = new AdminDao();
				if(! dao.login(admin)) {
					Response.sendFaildResponse(response, 400, "date is not valid !");
					return;
				}
				dao.close();
				HashMap<String, String> claims = new HashMap<String, String>();
				claims.put("admin_id", admin.id+"");
				String token = null;
				try {
					token = util.JwtGenerator.generateJwt(claims);
				} catch(Exception e) {
					Response.sendFaildResponse(response, 500, "unknown error occurred !");
					return;
				}
				Response.sendResponse(response, 200, new resources.AdminLoginResource(admin, token, true));
			} catch(Exception e) {
				Response.sendFaildResponse(response, 500, "unknown error occurred !");
				return;
			}
		} else if(path.length > 3 && path[1].equals("posts")) {
			int postId = -1;
			String temp = path[2];
			try {
				postId = Integer.parseInt(temp);
			} catch(Exception e) {
				Response.sendFaildResponse(response, 400, "post_id is not valid !");
				return;
			}
			if(postId == -1) {
				Response.sendFaildResponse(response, 400, "post_id is not valid !");
				return;
			}
			if(path[3].equals("visible")) {
				try {
					AdminDao dao = new AdminDao();
					if(! dao.isPostExist(postId)) {
						Response.sendFaildResponse(response, 400, "post not exist !");
						return;
					}
					boolean ok = dao.setPostVisible(postId);
					dao.close();
					if(ok) {
						Response.sendSuccessedResponse(response, 201, "the post is visible now");
						return;
					} else {
						Response.sendFaildResponse(response, 500, "unknown error occurred !");
						return;
					}
				} catch(Exception e) {
					Response.sendFaildResponse(response, 500, "unknown error occurred !");
					return;
				}
			} else if(path[3].equals("hidden")) {
				try {
					AdminDao dao = new AdminDao();
					if(! dao.isPostExist(postId)) {
						Response.sendFaildResponse(response, 400, "post not exist !");
						return;
					}
					boolean ok = dao.setPostHidden(postId);
					dao.close();
					if(ok) {
						Response.sendSuccessedResponse(response, 201, "the post is hidden now");
						return;
					} else {
						Response.sendFaildResponse(response, 500, "unknown error occurred !");
						return;
					}
				} catch(Exception e) {
					Response.sendFaildResponse(response, 500, "unknown error occurred !");
					return;
				}
			} else {
				response.setStatus(404);
			}
		} else {
			response.setStatus(404);
		}
	}

}
