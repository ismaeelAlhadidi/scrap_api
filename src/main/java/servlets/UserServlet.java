package servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import beans.User;
import dao.UserDao;
import resources.LoginResource;
import resources.PostsResource;
import resources.UserResource;
import util.Response;
import java.util.HashMap;
import util.Validator;
import dao.PostDao;
import beans.Post;
import java.util.List;
import beans.Comment;
import resources.CommentsResource;

/**
 * Servlet implementation class UserServlet
 */
@WebServlet("/api/v1/users/*")
public class UserServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

    /**
     * Default constructor. 
     */
    public UserServlet() {
        // TODO Auto-generated constructor stub
    }
    
    public int getUserId(HttpServletRequest request) {
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
    
    protected void doGetUsersComments(HttpServletRequest request, HttpServletResponse response, User currentUser) throws ServletException, IOException {
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
    		List<Comment> comments = dao.getUserComments(currentUser.id, page, search);
    		int pages = dao.getPagesOfUserComments(currentUser.id, search);
			String prev_page = (page == 0) ? "" : (page+"");
			String current_page = (page+1)+"";
			String next_page = (page+1) >= pages ? "" : ((page+2)+"");
    		dao.close();
    		Response.sendResponse(response, 200, new CommentsResource(comments, pages, prev_page, current_page, next_page));
    	} catch(Exception e) {
    		Response.sendFaildResponse(response, 500, "unknown error occurred !");
			return;
    	}
    }
    
    protected void doGetUserPosts(HttpServletRequest request, HttpServletResponse response, int userId, User currentUser) throws ServletException, IOException {
    	try {
    		PostDao postDao = new PostDao();
    		UserDao userDao = new UserDao(postDao.dbConnection);
    		User user = userDao.getUser(userId);
    		if(user == null) {
    			Response.sendFaildResponse(response, 404, "user not exist !");
    			return;
    		}
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
    		List<Post> posts = postDao.getUserPosts(userId, currentUser == null ? false : userId == currentUser.id, page, search);
    		int pages = postDao.getPagesOfUserPosts(userId, currentUser == null ? false : userId == currentUser.id, search);
			String prev_page = (page == 0) ? "" : (page+"");
			String current_page = (page+1)+"";
			String next_page = (page+1) >= pages ? "" : ((page+2)+"");
			postDao.close();
			for(Post post : posts) {
				post.user = null;
			}
			Response.sendResponse(response, 200, new PostsResource(posts, pages, prev_page, current_page, next_page));
    	} catch(Exception e) {
    		Response.sendFaildResponse(response, 500, "unknown error occurred !");
			return;
    	}
    }
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		User currentUser = null;
		if(request.getAttribute("user") != null) {
			currentUser = (User)request.getAttribute("user");
		}
		int id = getUserId(request);
		if(id == -1) {
			Response.sendFaildResponse(response, 404, "user not exist !");
			return;
		}
		String[] paths = request.getPathInfo().split("/");
		if(paths.length > 2 && paths[2].equals("posts")) {
			doGetUserPosts(request, response, id, currentUser);
			return;
		}
		if(paths.length > 2 && paths[2].equals("comments")) {
			if(currentUser == null || id != currentUser.id) {
				Response.sendFaildResponse(response, 401, "you unauthorized to make this operation !");
				return;
			}
			doGetUsersComments(request, response, currentUser);
			return;
		}
		User user = null;
		try {
			UserDao dao = new UserDao();
			user = dao.getUser(id);
			dao.close();
		} catch(Exception e) {
			Response.sendFaildResponse(response, 500, "unknown error occurred !");
			return;
		}
		if(user == null) {
			Response.sendFaildResponse(response, 404, "user not exist !");
			return;
		}
		if(currentUser == null || currentUser.id != id) {
			if(! user.is_phone_visible) user.phone = null;
			user.birthday = null;
			user.sex = null;
			user.profession = null;
		}
		Response.sendResponse(response, 200, new UserResource(user));
	}
	
	class ChangePasswordRequest {
		public String phone;
		public String code;
		public String new_password;
	}
	
	protected void changePassword(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ChangePasswordRequest req = null;
		try {
			req = (new Gson()).fromJson(request.getReader(), ChangePasswordRequest.class);
		} catch(Exception e) {
			Response.sendFaildResponse(response, 400, "data is not valid !");
			return;
		}
		if(req == null) {
			Response.sendFaildResponse(response, 400, "data is not valid !");
			return;
		}
		if(req.phone == null || ! util.Validator.isValidPhone(req.phone)) {
			Response.sendFaildResponse(response, 400, "phone is not valid !");
			return;
		}
		if(req.code == null) {
			Response.sendFaildResponse(response, 400, "code is not valid !");
			return;
		}
		if(req.new_password == null || req.new_password.length() < 1 || req.new_password.length() > 254) {
			Response.sendFaildResponse(response, 400, "new_password is not valid !");
			return;
		}
		try {
			UserDao dao = new UserDao();
			User user = dao.getUser(req.phone);
			if(user == null) {
				dao.close();
				Response.sendFaildResponse(response, 400, "phone number is not valid !");
				return;
			}
			if(! user.verification_type.equals("change_password")) {
				dao.close();
				Response.sendFaildResponse(response, 400, "data is not valid !");
				return;
			}
			if(! user.sent_code.equals(req.code) || ! util.Time.isBeforeLessThan1Hour(user.sent_time)) {
				dao.close();
				Response.sendFaildResponse(response, 400, "code is not valid !");
				return;
			}
			boolean ok = dao.changePassword(user.id, req.new_password);
			if(! ok) {
				dao.close();
				Response.sendFaildResponse(response, 500, "unknown error occurred !");
				return;
			}
			dao.login(user.id);
			dao.close();
			HashMap<String, String> claims = new HashMap<String, String>();
			claims.put("id", user.getId()+"");
			String token = null;
			try {
				token = util.JwtGenerator.generateJwt(claims);
			} catch(Exception e) {
				Response.sendFaildResponse(response, 500, "unknown error occurred !");
				return;
			}
			Response.sendResponse(response, 201, new LoginResource(user, token, true));
		} catch(Exception e) {
			Response.sendFaildResponse(response, 500, "unknown error occurred !");
			return;
		}
	}
	
	class VerificationPhoneRequest {
		public String phone;
		public String code;
	}
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String path = request.getPathInfo();
		if(path == null) {
			path = "/";
		}
		if(path.startsWith("/password")) {
			changePassword(request, response);
			return;
		} else if(path.startsWith("/phone")) {
			String[] p = request.getPathInfo().split("/");
			if(p.length <= 1 || ! p[1].equals("phone")) {
				Response.sendFaildResponse(response, 404, "the url is wrong !");
				return;
			}
			if(p.length > 2 && p[2].equals("verification")) {
				VerificationPhoneRequest req = null;
				try {
					req = (new Gson()).fromJson(request.getReader(), VerificationPhoneRequest.class);
				} catch(Exception e) {
					Response.sendFaildResponse(response, 400, "data is not valid !");
					return;
				}
				if(req == null) {
					Response.sendFaildResponse(response, 400, "data is not valid !");
					return;
				}
				if(req.phone == null || ! util.Validator.isValidPhone(req.phone)) {
					Response.sendFaildResponse(response, 400, "phone number is not valid !");
					return;
				}
				if(req.code == null || req.code.length() != 6) {
					Response.sendFaildResponse(response, 400, "code number is not valid !");
					return;
				}
				try {
					UserDao dao = new UserDao();
					User user = dao.getUser(req.phone);
					if(user == null) {
						dao.close();
						Response.sendFaildResponse(response, 400, "phone number is not valid !");
						return;
					}
					if(! user.verification_type.equals("phone")) {
						dao.close();
						Response.sendFaildResponse(response, 400, "data is not valid !");
						return;
					}
					if(! user.sent_code.equals(req.code) || ! util.Time.isBeforeLessThan1Hour(user.sent_time)) {
						dao.close();
						Response.sendFaildResponse(response, 400, "code is not valid !");
						return;
					}
					boolean ok = dao.setUserPhoneAsVerified(user.id);
					if(! ok) {
						dao.close();
						Response.sendFaildResponse(response, 500, "unknown error occurred !");
						return;
					}
					dao.login(user.id);
					dao.close();
					HashMap<String, String> claims = new HashMap<String, String>();
					claims.put("id", user.getId()+"");
					String token = null;
					try {
						token = util.JwtGenerator.generateJwt(claims);
					} catch(Exception e) {
						Response.sendFaildResponse(response, 500, "unknown error occurred !");
						return;
					}
					Response.sendResponse(response, 201, new LoginResource(user, token, true));
				} catch(Exception e) {
					Response.sendFaildResponse(response, 500, "unknown error occurred !");
					return;
				}
			}
		} else {
			User user = null;
			try {
				user = new Gson().fromJson(request.getReader(), User.class);
			} catch(Exception e) {};
			if(user == null) {
				Response.sendFaildResponse(response, 400, "data is not valid !");
				return;
			}
			String[] validateResult = validateUserData(user);
			if(! validateResult[0].equals("ok")) {
				Response.sendFaildResponse(response, 400, validateResult[1]);
				return;
			}
			try {
				UserDao dao = new UserDao();
				if(! dao.weHaveGovernorate(user.getGovernorateName())) {
					dao.close();
					Response.sendFaildResponse(response, 400, "governorate is not valid ! ");
					return;
				}
				if(dao.isEmailUsed(user.getEmail())) {
					dao.close();
					Response.sendFaildResponse(response, 400, "email is not valid !");
					return;
				}
				if(dao.isPhoneUsed(user.getPhone())) {
					dao.close();
					Response.sendFaildResponse(response, 400, "phone is not valid !");
					return;
				}
				if(! dao.createUser(user)) {
					dao.close();
					Response.sendFaildResponse(response, 500, "create user feild !");
					return;
				}
				String generatedCode = util.Generator.generate6DigitsCode();
				try {
					services.SMSSender.send("+962" + user.getPhone().substring(1, user.phone.length()), "Scrap: your verification code is " + generatedCode);
				} catch(Exception e) {}
				dao.setVerificationCode(user.id, generatedCode, "phone");
				dao.close();
			} catch(Exception e) {
				Response.sendFaildResponse(response, 500, "unknown error occurred !");
				return;
			}
			Response.sendResponse(response, 201, new ResponseBody());
		}
	}
	
	class ResponseBody {
		public boolean status = true;
		public String message = "we sent code to your phone, please verify it !";
	}
	
	/**
	 * @see HttpServlet#doPut(HttpServletRequest, HttpServletResponse)
	 */
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String p = request.getPathInfo();
		if(p == null) {
			p = "/";
		}
		if(p.startsWith("/phone")) {
			String[] path = request.getPathInfo().split("/");
			if(path.length <= 1 || ! path[1].equals("phone")) {
				Response.sendFaildResponse(response, 404, "the url is wrong !");
				return;
			}
			if(path.length > 2 && path[2].equals("verification")) {
				VerificationPhoneRequest req = null;
				try {
					req = (new Gson()).fromJson(request.getReader(), VerificationPhoneRequest.class);
				} catch(Exception e) {
					Response.sendFaildResponse(response, 400, "data is not valid !");
					return;
				}
				if(req == null) {
					Response.sendFaildResponse(response, 400, "data is not valid !");
					return;
				}
				if(req.phone == null || ! util.Validator.isValidPhone(req.phone)) {
					Response.sendFaildResponse(response, 400, "phone number is not valid !");
					return;
				}
				try {
					UserDao dao = new UserDao();
					User user = dao.getUser(req.phone);
					String generatedCode = util.Generator.generate6DigitsCode();
					try {
						services.SMSSender.send("+962" + user.getPhone().substring(1, user.phone.length()), "Scrap: your verification code is " + generatedCode);
					} catch(Exception e) {
						System.out.println(e.getMessage());
					}
					dao.setVerificationCode(user.id, generatedCode, "phone");
					dao.close();
				} catch(Exception e) {
					Response.sendFaildResponse(response, 500, "unknown error occurred !");
					return;
				}
				Response.sendResponse(response, 201, new ResponseBody());
				return;
			}
		}
		if(request.getAttribute("user") == null) {
			Response.sendFaildResponse(response, 401, "you unauthorized to make this operation !");
			return;
		}
		User currentUser = (User)request.getAttribute("user");
		int id = getUserId(request);
		if(id != currentUser.getId()) {
			Response.sendFaildResponse(response, 401, "you unauthorized to make this operation !");
			return;
		}
		User user = null;
		try {
			user = (new Gson()).fromJson(request.getReader(), User.class);
		} catch(Exception e) {}
		if(user == null) {
			Response.sendFaildResponse(response, 400, "data is not valid !");
			return;
		}
		user.setEmail(currentUser.getEmail());
		user.setPhone(currentUser.getPhone());
		user.setPassword("test");
		String[] validateResult = validateUserData(user);
		if(! validateResult[0].equals("ok")) {
			Response.sendFaildResponse(response, 400, validateResult[1]);
			return;
		}
		try {
			UserDao dao = new UserDao();
			if(! dao.weHaveGovernorate(user.getGovernorateName())) {
				dao.close();
				Response.sendFaildResponse(response, 400, "governorate is not valid ! ");
				return;
			}
			if(! dao.updateUser(user, currentUser)) {
				dao.close();
				Response.sendFaildResponse(response, 500, "update user feild !");
				return;
			}
			dao.close();
		} catch(Exception e) {
			Response.sendFaildResponse(response, 500, "unknown error occurred !");
			return;
		}
		Response.sendSuccessedResponse(response, 200, "user data changed !");
	}

	class DeletePasswordRequest {
		String phone;
	}
	/**
	 * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String p = request.getPathInfo();
		if(p == null) p = "/";
		String[] path = p.split("/");
		if(path.length > 1 && path[1].equals("password")) {
			DeletePasswordRequest req = null;
			try {
				req = (new Gson()).fromJson(request.getReader(), DeletePasswordRequest.class);
			} catch(Exception e) {
				Response.sendFaildResponse(response, 400, "data is not valid ! ");
				return;
			}
			if(req == null) {
				Response.sendFaildResponse(response, 400, "data is not valid ! ");
				return;
			}
			if(req.phone == null || ! util.Validator.isValidPhone(req.phone)) {
				Response.sendFaildResponse(response, 400, "phone is not valid ! ");
				return;
			}
			try {
				UserDao dao = new UserDao();
				User user = dao.getUser(req.phone);
				if(user == null) {
					Response.sendFaildResponse(response, 400, "phone is not valid ! ");
					return;
				}
				String generatedCode = util.Generator.generate6DigitsCode();
				try {
					services.SMSSender.send("+962" + user.getPhone().substring(1, user.phone.length()), "Scrap: your verification code is " + generatedCode);
				} catch(Exception e) {}
				dao.setVerificationCode(user.id, generatedCode, "change_password");
				dao.close();
				Response.sendResponse(response, 200, new ResponseBody());
			} catch(Exception e) {
				Response.sendFaildResponse(response, 500, "unknown error occurred !");
				return;
			}
		} else {
			Response.sendFaildResponse(response, 404, "this page is not exist !");
		}
	}

	public String[] validateUserData(User user) {
		String[] result = new String[2];
		result[0] = "ok";
		if(user.getName() == null || user.getName().trim().length() < 1 || user.getName().trim().length() > 254) {
			result[0] = "not valid";
			result[1] = "name is not valid !";
			return result;
		} else {
			user.setName(user.getName().trim());
		}
		if(user.getEmail() == null || user.getEmail().length() < 1 || user.getEmail().length() > 254 || ! Validator.isValidMail(user.getEmail())) {
			result[0] = "not valid";
			result[1] = "email is not valid !";
			return result;
		}
		if(user.getPhone() == null || user.getPhone().length() > 15 || ! Validator.isValidPhone(user.getPhone())) {
			result[0] = "not valid";
			result[1] = "phone is not valid !";
			return result;
		}
		if(user.getPassword() == null || user.getPassword().length() < 1 || user.getPassword().length() > 254) {
			result[0] = "not valid";
			result[1] = "password is not valid !";
			return result;
		}
		if(user.getGovernorateName() == null || user.getGovernorateName().length() > 254) {
			result[0] = "not valid";
			result[1] = "governorate is not valid !";
			return result;
		}
		if(user.getRegion() == null || user.getRegion().length() < 1 || user.getRegion().length() > 254) {
			result[0] = "not valid";
			result[1] = "region is not valid !";
			return result;
		}
		if(user.birthday != null && ! Validator.isDate(user.birthday)) {
			result[0] = "not valid";
			result[1] = "birthday is not valid !";
			return result;
		}
		if(user.sex != null && ! user.sex.equals("male") && user.sex.equals("female")) {
			result[0] = "not valid";
			result[1] = "sex is not valid !";
			return result;
		}
		if(user.profession != null && ( user.profession.length() < 1 || user.profession.length() > 254) ) {
			result[0] = "not valid";
			result[1] = "profession is not valid !";
			return result;
		}
		return result;
	}
}
