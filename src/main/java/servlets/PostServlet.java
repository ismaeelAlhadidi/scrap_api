package servlets;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import beans.Comment;
import beans.Post;
import beans.User;
import dao.CategoryDao;
import dao.NotificationDao;
import dao.PostDao;
import dao.UserDao;
import resources.PostsResource;
import util.Response;

/**
 * Servlet implementation class PostServlet
 */
@WebServlet("/api/v1/posts/*")
public class PostServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	HashSet<String> types = new HashSet<String>(List.of("request", "sell", "exchange"));
    HashSet<String> negotiationValues = new HashSet<String>(List.of("negotiable", "not_negotiable", "auction", "depend_on_quality"));
    HashSet<String> sellingValues = new HashSet<String>(List.of("for_sell", "not_for_sell", "with_difference_price"));
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public PostServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

    public int getPostId(HttpServletRequest request) {
    	String path = request.getPathInfo();
		if(path == null) {
			path = "/";
		}
		path = path.substring(1, path.length()).split("/")[0];
		int id = -1;
		try {
			id = Integer.parseInt(path);
		} catch(Exception e) {}
    	return id;
    }
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    private boolean isDate(String test) {
    	String[] temp = test.split("-");
    	if(temp.length != 3) {
    		return false;
    	}
    	for(int i = 0; i < test.length(); ++i) {
    		if(test.charAt(i) != '-') {
    			if(test.charAt(i) < '0' || test.charAt(i) > '9') {
    				return false;
    			}
    		}
    	}
    	return true;
    }
    protected void doGetComments(HttpServletRequest request, HttpServletResponse response, int postId) throws ServletException, IOException {
    	try {
			PostDao dao = new PostDao();
			Post post = dao.getPost(postId);
			if(post == null) {
				Response.sendFaildResponse(response, 400, "post not exist !");
				return;
			}
			if(! post.with_comments) {
				Response.sendFaildResponse(response, 400, "the post without comments !");
				return;
			}
			List<Comment> comments = dao.getComments(postId);
			dao.close();
			Response.sendResponse(response, 200, comments);
    	} catch(Exception e) {
    		Response.sendFaildResponse(response, 500, "unknown error occurred !");
			return;
		}
    }
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int postId = getPostId(request);
		if(postId != -1) {
			String[] temp = request.getPathInfo().split("/");
			if(temp.length > 2 && temp[2].equals("comments")) {
				doGetComments(request, response, postId);
				return;
			}
			Post post = null;
			try {
				PostDao dao = new PostDao();
				post = dao.getPost(postId);
				if(post == null) {
					Response.sendFaildResponse(response, 400, "post not exist !");
					return;
				}
				if(! post.user.is_phone_visible || ! post.is_phone_visible) {
					post.user.phone = "";
				}
				if(request.getAttribute("user") != null) {
					User currentUser = (User)request.getAttribute("user");
					dao.setFavorites(List.of(post), currentUser);
				}
				dao.close();
			} catch(Exception e) {
				Response.sendFaildResponse(response, 500, "unknown error occurred !");
				return;
			}
			Response.sendResponse(response, 200, post);
		} else {
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
				String type = request.getParameter("type");
				if(type != null && ! types.contains(type)) {
					Response.sendFaildResponse(response, 500, "type not valid !");
					return;
				}
				String search = request.getParameter("search");
				String category_name = request.getParameter("category_name");
				if(category_name != null) {
					HashSet<String> categories = new HashSet<String>((new CategoryDao(dao.dbConnection)).getCategories());
					if(! categories.contains(category_name)) {
						Response.sendFaildResponse(response, 400, "category is not valid !");
						return;
					}
				}
				String governorateName = request.getParameter("governorateName");
				if(governorateName != null) {
					if(! (new UserDao(dao.dbConnection)).weHaveGovernorate(governorateName)) {
						Response.sendFaildResponse(response, 400, "governorate is not valid !");
						return;
					}
				}
				String date_after = request.getParameter("date_after");
				if(date_after != null && ! isDate(date_after)) {
					Response.sendFaildResponse(response, 400, "date is not valid !");
					return;
				}
				List<Post> posts = dao.getPosts(page, type, search, category_name, governorateName, date_after);
				int pages = dao.getPages(page, type, search, category_name, governorateName, date_after);
				String prev_page = (page == 0) ? "" : (page+"");
				String current_page = (page+1)+"";
				String next_page = (page+1) >= pages ? "" : ((page+2)+"");
				if(request.getAttribute("user") != null) {
					User currentUser = (User)request.getAttribute("user");
					dao.setFavorites(posts, currentUser);
				}
				dao.close();
				Response.sendResponse(response, 200, new PostsResource(posts, pages, prev_page, current_page, next_page));
			} catch(Exception e) {
				Response.sendFaildResponse(response, 500, "unknown error occurred !");
				return;
			}
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPostComment(HttpServletRequest request, HttpServletResponse response, int postId, User currentUser) throws ServletException, IOException {
		try {
			PostDao dao = new PostDao();
			Post post = dao.getPost(postId);
			if(post == null) {
				Response.sendFaildResponse(response, 400, "post not exist !");
				return;
			}
			if(! post.with_comments) {
				Response.sendFaildResponse(response, 400, "the post without comments !");
				return;
			}
			Comment comment;
			try {
				comment = (new Gson()).fromJson(request.getReader(), Comment.class);
			} catch(Exception e) {
				Response.sendFaildResponse(response, 400, "data is not valid !");
				return;
			}
			if(comment == null) {
				Response.sendFaildResponse(response, 400, "data is not valid !");
				return;
			}
			if(comment.content == null || comment.content.length() < 1 || comment.content.length() > 65534) {
				Response.sendFaildResponse(response, 400, "content is not valid !");
				return;
			}
			comment.user_id = currentUser.id;
			comment.post_id = postId;
			if(! dao.addComment(comment)) {
				Response.sendFaildResponse(response, 500, "unknown error occurred !");
				return;
			}
			try {
				ExecutorService executorService = Executors.newFixedThreadPool(10);
				final User tempUser = currentUser;
				final Comment tempComment = comment;
				final Post tempPost = post;
				executorService.execute(new Runnable() {
				    public void run() {
				    	try {
				    		NotificationDao notificationDao = new NotificationDao(dao.dbConnection);
							if(tempUser.get_notifications_on_comments && tempPost.user_id != tempComment.user_id) {
								notificationDao.createNotification(tempPost.user.id, "comment_on_your_post", "قام " + tempUser.name + " بالتعليق على منشورك");
							}
							notificationDao.createNotificationsForUsersHaveThisPostAsFavorite(tempPost.id, tempPost.user_id, tempComment.user_id, tempUser.name);
							dao.close();
				    	} catch(Exception e) {}
				    }
				});
				executorService.shutdown();
			} catch(Exception e) {}
			Response.sendSuccessedResponse(response, 201, "the comment is inserted");
		} catch(Exception e) {
			Response.sendFaildResponse(response, 500, "unknown error occurred !");
			return;
		}
	}
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		if(request.getAttribute("user") == null) {
			Response.sendFaildResponse(response, 401, "you unauthorized to make this operation !");
			return;
		}
		User currentUser = (User)request.getAttribute("user");
		int postId = getPostId(request);
		if(postId != -1) {
			String[] temp = request.getPathInfo().split("/");
			if(temp.length > 2 && temp[2].equals("comments")) {
				doPostComment(request, response, postId, currentUser);
			} else {
				response.setStatus(404);
			}
			return;
		}
		Post post = null;
		try {
			post = (new Gson()).fromJson(request.getReader(), Post.class);
		} catch(Exception e) {
			Response.sendFaildResponse(response, 400, "data is not valid !");
			return;
		}
		if(post == null) {
			Response.sendFaildResponse(response, 400, "data is not valid !");
			return;
		}
		post.user_id = currentUser.id;
		if(post.title == null || post.title.length() < 1 || post.title.length() > 254) {
			Response.sendFaildResponse(response, 400, "title is not valid !");
			return;
		}
		if(post.description == null || post.description.length() < 1 || post.description.length() > 65534) {
			Response.sendFaildResponse(response, 400, "description is not valid !");
			return;
		}
		if(post.categoryName == null) {
			Response.sendFaildResponse(response, 400, "category is not valid !");
			return;
		}
		if(post.governorateName == null) {
			Response.sendFaildResponse(response, 400, "governorate is not valid !");
			return;
		}
		if(post.region == null || post.region.length() < 1 || post.region.length() > 254) {
			Response.sendFaildResponse(response, 400, "region is not valid !");
			return;
		}
		if(post.quantityOrTimeOfUse == null || post.quantityOrTimeOfUse.length() < 1 || post.quantityOrTimeOfUse.length() > 254) {
			Response.sendFaildResponse(response, 400, "quantityOrTimeOfUse is not valid !");
			return;
		}
		if(post.type == null || ! types.contains(post.type)) {
			Response.sendFaildResponse(response, 400, "type is not valid !");
			return;
		}
		if(! post.type.equals("exchange")) {
			if(post.price == (double)-1 || post.price < (double)0 || post.price >= (double)(Math.pow(10, 7))) {
				Response.sendFaildResponse(response, 400, "price is not valid !");
				return;
			}
			if(post.negotiation == null || ! negotiationValues.contains(post.negotiation)) {
				Response.sendFaildResponse(response, 400, "negotiation value is not valid !");
				return;
			}
		} else {
			if(post.selling == null || ! sellingValues.contains(post.selling)) {
				Response.sendFaildResponse(response, 400, "selling value is not valid !");
				return;
			}
			if(post.exchangeTitle == null || post.exchangeTitle.length() < 1 || post.exchangeTitle.length() > 254) {
				Response.sendFaildResponse(response, 400, "exchangeTitle is not valid !");
				return;
			}
			if(post.exchangeDescription == null || post.exchangeDescription.length() < 1 || post.exchangeDescription.length() > 65534) {
				Response.sendFaildResponse(response, 400, "exchangeDescription is not valid !");
				return;
			}
			if(post.exchangeImages == null || post.exchangeImages.length == 0) {
				Response.sendFaildResponse(response, 400, "exchangeImages is not valid !");
				return;
			}
		}
		if(post.images == null || post.images.length == 0 || post.images.length > Post.POST_MAX_IMAGES_COUNT) {
			Response.sendFaildResponse(response, 400, "images is not valid !");
			return;
		}
		if(post.price == -1) post.price = 0;
		try {
			PostDao dao = new PostDao();
			HashSet<String> categories = new HashSet<String>((new CategoryDao(dao.dbConnection)).getCategories());
			if(! categories.contains(post.categoryName)) {
				Response.sendFaildResponse(response, 400, "category is not valid !");
				return;
			}
			if(! (new UserDao(dao.dbConnection)).weHaveGovernorate(post.governorateName)) {
				Response.sendFaildResponse(response, 400, "governorate is not valid !");
				return;
			}
			if(! dao.createPost(post)) {
				Response.sendFaildResponse(response, 500, "unknown error occurred !");
				return;
			}
			dao.close();
		} catch(Exception e) {
			Response.sendFaildResponse(response, 500, "unknown error occurred !");
			return;
		}
		Response.sendResponse(response, 201, new ResponseTemplate(post.id, "the post is created", true));
	}
	
	class ResponseTemplate {
		public int post_id;
		String message;
		boolean status;
		ResponseTemplate(int id, String message, boolean status) {
			this.post_id = id;
			this.message = message;
			this.status = status;
		}
	}
	
	/**
	 * @see HttpServlet#doPut(HttpServletRequest, HttpServletResponse)
	 */
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}
	
}
