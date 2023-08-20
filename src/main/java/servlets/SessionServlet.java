package servlets;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import beans.User;
import util.PasswordHandler;
import util.Response;
import util.Validator;

import dao.UserDao;
import resources.LoginResource;

/**
 * Servlet implementation class SessionServlet
 */
@WebServlet("/api/v1/sessions/*")
public class SessionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SessionServlet() {
        super();
        // TODO Auto-generated constructor stub
    }
    
	
    class ResponseBody {
    	public String jwt;
    }
    protected void refreshToken(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	if(request.getAttribute("user") == null) {
			Response.sendFaildResponse(response, 401, "you unauthorized to make this operation !");
			return;
		}
		User currentUser = (User)request.getAttribute("user");
		HashMap<String, String> claims = new HashMap<String, String>();
		claims.put("id", currentUser.getId()+"");
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
    
    class LoginRequest {
    	public String identifier;
    	public String password;
    }
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		LoginRequest loginRequest = (new Gson()).fromJson(request.getReader(), LoginRequest.class);
		String[] path = new String[0];
		if(request.getPathInfo() != null) {
			path = request.getPathInfo().split("/");
		}
		if(path.length > 1 && path[1].equals("refresh")) {
			refreshToken(request, response);
			return;
		}
		if(loginRequest.identifier == null || loginRequest.password == null) {
			Response.sendFaildResponse(response, 400, "data is not valid !");
			return;
		}
		String column = "";
		if(Validator.isValidMail(loginRequest.identifier)) {
			column = "email";
		} else if(Validator.isValidPhone(loginRequest.identifier)) {
			column = "phone";
		} else {
			Response.sendFaildResponse(response, 400, "data is not valid !");
			return;
		}
		try {
			UserDao dao = new UserDao();
			String[] saltAndPasswordAndId = dao.getSaltAndPasswordAndId(column, loginRequest.identifier);
			if(saltAndPasswordAndId == null) {
				Response.sendFaildResponse(response, 400, "data is not valid !");
				dao.close();
				return;
			}
			if(PasswordHandler.validate(saltAndPasswordAndId[0], loginRequest.password, saltAndPasswordAndId[1])) {
				User user = dao.getUser(Integer.parseInt(saltAndPasswordAndId[2]));
				if(! user.is_phone_verified) {
					dao.close();
					Response.sendFaildResponse(response, 401, "you need to verify your phone number !");
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
				Response.sendResponse(response, 200, new LoginResource(user, token, true));
				return;
			}
			dao.close();
		} catch(Exception e) {
			Response.sendFaildResponse(response, 500, "unknown error occurred !");
			return;
		}
		Response.sendFaildResponse(response, 400, "data is not valid !");
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
		try {
			UserDao dao = new UserDao();
			dao.logout(currentUser.id);
			dao.close();
		} catch(Exception e) {}
		Response.sendSuccessedResponse(response, 200, "the session is deleted");
	}

}
