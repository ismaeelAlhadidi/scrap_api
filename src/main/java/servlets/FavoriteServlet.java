package servlets;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import beans.Post;
import beans.User;
import util.Response;
import dao.PostDao;
import resources.PostsResource;

import java.util.List;

/**
 * Servlet implementation class FavoriteServlet
 */
@WebServlet("/api/v1/favorites/*")
public class FavoriteServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FavoriteServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		if(request.getAttribute("user") == null) {
			Response.sendFaildResponse(response, 401, "you unauthorized to make this operation !");
			return;
		}
		User currentUser = (User)request.getAttribute("user");
		try {
			PostDao dao = new PostDao();
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
			String search = request.getParameter("search");
			List<Post> posts = dao.getFavoritersPost(currentUser.id, page, search);
			for(Post post : posts) {
				post.user = null;
			}
			int pages = dao.getPagesOfFavoritesPost(currentUser.id, search);
			String prev_page = (page == 0) ? "" : (page+"");
			String current_page = (page+1)+"";
			String next_page = (page+1) >= pages ? "" : ((page+2)+"");
			dao.close();
			Response.sendResponse(response, 200, new PostsResource(posts, pages, prev_page, current_page, next_page));
		} catch(Exception e) {
			Response.sendFaildResponse(response, 500, "unknown error occurred !");
			return;
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	class RequestBody {
		public int post_id;
	}
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		if(request.getAttribute("user") == null) {
			Response.sendFaildResponse(response, 401, "you unauthorized to make this operation !");
			return;
		}
		User currentUser = (User)request.getAttribute("user");
		RequestBody requestBody = null;
		try {
			requestBody = (new Gson()).fromJson(request.getReader(), RequestBody.class);
		} catch(Exception e) {
			Response.sendFaildResponse(response, 400, "data is not valid !");
			return;
		}
		try {
			PostDao dao = new PostDao();
			Post post = dao.getPost(requestBody.post_id);
			if(post == null) {
				Response.sendFaildResponse(response, 400, "post not exist !");
				return;
			}
			if(! dao.isInFavorites(post.id, currentUser.id) && ! dao.addToFavorite(post.id, currentUser.id)) {
				Response.sendFaildResponse(response, 500, "unknown error occurred !");
				return;
			}
			Response.sendSuccessedResponse(response, 201, "the post added to your favorites list");
		} catch(Exception e) {
			Response.sendFaildResponse(response, 500, "unknown error occurred !");
			return;
		}
	}

	/**
	 * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(request.getAttribute("user") == null) {
			Response.sendFaildResponse(response, 401, "you unauthorized to make this operation !");
			return;
		}
		User currentUser = (User)request.getAttribute("user");
		RequestBody requestBody = null;
		try {
			requestBody = (new Gson()).fromJson(request.getReader(), RequestBody.class);
		} catch(Exception e) {
			Response.sendFaildResponse(response, 400, "data is not valid !");
			return;
		}
		try {
			PostDao dao = new PostDao();
			Post post = dao.getPost(requestBody.post_id);
			if(post == null) {
				Response.sendFaildResponse(response, 400, "post not exist !");
				return;
			}
			if(dao.isInFavorites(post.id, currentUser.id) && ! dao.deleteFromFavorites(post.id, currentUser.id)) {
				Response.sendFaildResponse(response, 500, "unknown error occurred !");
				return;
			}
			Response.sendSuccessedResponse(response, 200, "the post deleted from favorites list");
		} catch(Exception e) {
			Response.sendFaildResponse(response, 500, "unknown error occurred !");
			return;
		}
	}

}
