package servlets;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.text.AbstractDocument.Content;

import com.google.gson.Gson;
import beans.User;
import util.Response;
import beans.Message;
import dao.MessageDao;
import dao.UserDao;
import beans.MessageWindow;
import java.util.List;

/**
 * Servlet implementation class MessageServlet
 */
@WebServlet("/api/v1/messages/*")
public class MessageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MessageServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

    private int getUserId(HttpServletRequest request) {
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
    
    protected void doSearchInMessages(HttpServletRequest request, HttpServletResponse response, User currentUser) throws ServletException, IOException {
    	String query = request.getParameter("query");
    	if(query == null) {
    		Response.sendFaildResponse(response, 400, "query is not valid !");
			return;
    	}
    	try {
    		MessageDao dao = new MessageDao();
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
    		List<Message> messages = dao.searchInMessages(query, page, currentUser.id);
    		int pages = dao.getPagesOfSearchInMessages(query, page);
    		String prev_page = (page == 0) ? "" : (page+"");
			String current_page = (page+1)+"";
			String next_page = (page+1) >= pages ? "" : ((page+2)+"");
			dao.close();
			Response.sendResponse(response, 200, new resources.MessagesResource(messages, pages, prev_page, current_page, next_page));
    	} catch(Exception e) {
    		Response.sendFaildResponse(response, 500, "unknown error occurred !");
			return;
    	}
    }
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(request.getAttribute("user") == null) {
			Response.sendFaildResponse(response, 401, "you unauthorized to make this operation !");
			return;
		}
		User currentUser = (User)request.getAttribute("user");
		int userId = getUserId(request);
		if(userId == -1) {
			String p = request.getPathInfo();
			if(p == null) p = "/";
			String[] path = p.split("/");
			if(path.length > 1 && path[1].equals("search")) {
				doSearchInMessages(request, response, currentUser);
				return;
			}
			try {
				MessageDao dao = new MessageDao();
				List<MessageWindow> messageWindows = dao.getMessagesWindowOf(currentUser.id);
				dao.close();
				Response.sendResponse(response, 200, messageWindows);
			} catch(Exception e) {
				Response.sendFaildResponse(response, 500, "unknown error occurred !");
				return;
			}
		} else {
			if(currentUser.id == userId) {
				Response.sendFaildResponse(response, 400, "id is not valid !");
				return;
			}
			try {
				MessageDao dao = new MessageDao();
				UserDao userDao = new UserDao(dao.dbConnection);
				if(! userDao.isUser(userId)) {
					Response.sendFaildResponse(response, 404, "user not exist !!");
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
				List<Message> messages = dao.getMessagesBetween(userId, currentUser.id, page);
				int pages = dao.getPagesOfMessagesBetween(userId, currentUser.id);
				String prev_page = (page == 0) ? "" : (page+"");
				String current_page = (page+1)+"";
				String next_page = (page+1) >= pages ? "" : ((page+2)+"");
				dao.setReaded(currentUser.id, userId);
				dao.close();
				Response.sendResponse(response, 200, new resources.MessagesResource(messages, pages, prev_page, current_page, next_page));
			} catch(Exception e) {
				Response.sendFaildResponse(response, 500, "unknown error occurred !");
				return;
			}
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(request.getAttribute("user") == null) {
			Response.sendFaildResponse(response, 401, "you unauthorized to make this operation !");
			return;
		}
		User currentUser = (User)request.getAttribute("user");
		Message message = null;
		try {
			message  = (new Gson()).fromJson(request.getReader(), Message.class);
		} catch(Exception e) {
			Response.sendFaildResponse(response, 400, "data is not valid !");
			return;
		}
		if(message == null) {
			Response.sendFaildResponse(response, 400, "data is not valid !");
			return;
		}
		if(message.content == null || message.content.length() < 1 || message.content.length() > 65534) {
			Response.sendFaildResponse(response, 400, "content is not valid !");
			return;
		}
		if(message.receiver_id == -1) {
			Response.sendFaildResponse(response, 400, "receiver_id is not valid !");
			return;
		}
		message.sender_id = currentUser.id;
		if(message.receiver_id == message.sender_id) {
			Response.sendFaildResponse(response, 400, "you can not send message to yourself !!");
			return;
		}
		try {
			MessageDao dao = new MessageDao();
			UserDao userDao = new UserDao(dao.dbConnection);
			if(! userDao.isUser(message.receiver_id)) {
				Response.sendFaildResponse(response, 400, "receiver_id is not valid !");
				return;
			}
			boolean ok = dao.createMessage(message);
			dao.close();
			if(! ok) {
				Response.sendFaildResponse(response, 500, "unknown error occurred !");
				return;
			}
			Response.sendSuccessedResponse(response, 201, "messsage sent");
		} catch(Exception e) {
			Response.sendFaildResponse(response, 500, "unknown error occurred !");
			return;
		}
	}

}
