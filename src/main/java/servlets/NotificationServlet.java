package servlets;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import beans.User;
import util.Response;
import beans.Notification;
import dao.NotificationDao;
import java.util.List;
import resources.NotificationsResource;
import com.google.gson.Gson;
import dao.UserDao;

/**
 * Servlet implementation class NotificationServlet
 */
@WebServlet("/api/v1/notifications/*")
public class NotificationServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public NotificationServlet() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    class NotificationSettings {
    	public Boolean get_notifications_on_comments;
    	public Boolean get_notifications_on_messages;
    	public Boolean get_notifications_on_your_ads;
    	public Boolean get_notifications_on_favorites;
    	public Boolean get_notifications_on_scrapi;
    	public Boolean get_notifications_on_scrap;
    }
    
    protected void doGetNotificationsSettings(HttpServletRequest request, HttpServletResponse response, User currentUser) throws ServletException, IOException {
    	NotificationSettings notificationSettings = new NotificationSettings();
    	notificationSettings.get_notifications_on_comments = currentUser.get_notifications_on_comments;
    	notificationSettings.get_notifications_on_messages = currentUser.get_notifications_on_messages;
    	notificationSettings.get_notifications_on_your_ads = currentUser.get_notifications_on_your_ads;
    	notificationSettings.get_notifications_on_favorites = currentUser.get_notifications_on_favorites;
    	notificationSettings.get_notifications_on_scrapi = currentUser.get_notifications_on_scrapi;
    	notificationSettings.get_notifications_on_scrap = currentUser.get_notifications_on_scrap;
    	Response.sendResponse(response, 200, notificationSettings);
    }
    
    protected void doPutNotificationsSettings(HttpServletRequest request, HttpServletResponse response, User currentUser) throws ServletException, IOException {
    	NotificationSettings notificationSettings = null;
    	try {
    		notificationSettings = (new Gson()).fromJson(request.getReader(), NotificationSettings.class);
    	} catch(Exception e) {
    		Response.sendFaildResponse(response, 401, "data is not valid !");
    	}
    	if(notificationSettings == null) {
    		Response.sendFaildResponse(response, 401, "data is not valid !");
    	}
    	if(notificationSettings.get_notifications_on_comments != null) {
    		currentUser.get_notifications_on_comments = notificationSettings.get_notifications_on_comments;
    	}
    	if(notificationSettings.get_notifications_on_messages != null) {
    		currentUser.get_notifications_on_messages = notificationSettings.get_notifications_on_messages;
    	}
    	if(notificationSettings.get_notifications_on_your_ads != null) {
    		currentUser.get_notifications_on_your_ads = notificationSettings.get_notifications_on_your_ads;
    	}
    	if(notificationSettings.get_notifications_on_favorites != null) {
    		currentUser.get_notifications_on_favorites = notificationSettings.get_notifications_on_favorites;
    	}
    	if(notificationSettings.get_notifications_on_scrapi != null) {
    		currentUser.get_notifications_on_scrapi = notificationSettings.get_notifications_on_scrapi;
    	}
    	if(notificationSettings.get_notifications_on_scrap != null) {
    		currentUser.get_notifications_on_scrap = notificationSettings.get_notifications_on_scrap;
    	}
    	try {
    		UserDao dao = new UserDao();
    		boolean ok = dao.setNotificationsSettings(currentUser);
    		dao.close();
    		if(! ok) {
    			Response.sendFaildResponse(response, 500, "unknown error occurred !");
    			return;
    		}
    		Response.sendFaildResponse(response, 201, "notifications settings changed !");
    	} catch(Exception e) {
    		Response.sendFaildResponse(response, 500, "unknown error occurred !");
			return;
    	}
    }
    
    private int getNotificationId(HttpServletRequest request) {
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
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		if(request.getAttribute("user") == null) {
			Response.sendFaildResponse(response, 401, "you unauthorized to make this operation !");
			return;
		}
		User currentUser = (User)request.getAttribute("user");
		String p = request.getPathInfo();
		if(p == null) p = "/";
		String[] path = p.split("/");
		if(path.length > 1 && path[1].equals("settings")) {
			doGetNotificationsSettings(request, response, currentUser);
			return;
		}
		try {
			NotificationDao dao = new NotificationDao();
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
			List<Notification> notifications = dao.getNotifications(currentUser.id, page, search);
			int pages = dao.getPages(currentUser.id, search);
			String prev_page = (page == 0) ? "" : (page+"");
			String current_page = (page+1)+"";
			String next_page = (page+1) >= pages ? "" : ((page+2)+"");
			Integer new_notifications = search != null ? null : dao.getNewNotifications(currentUser.id);
			dao.readNotifications(notifications);
			dao.close();
			Response.sendResponse(response, 200, new NotificationsResource(notifications, pages, prev_page, current_page, next_page, new_notifications));
		} catch(Exception e) {
			Response.sendFaildResponse(response, 500, "unknown error occurred !");
			return;
		}
	}
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		if(request.getAttribute("user") == null) {
			Response.sendFaildResponse(response, 401, "you unauthorized to make this operation !");
			return;
		}
		User currentUser = (User)request.getAttribute("user");
		int notificationId = getNotificationId(request);
		if(notificationId == -1) {
			Response.sendFaildResponse(response, 400, "data is not valid !");
			return;
		}
		String[] path = request.getPathInfo().split("/");
		if(path.length < 3 || ! path[2].equals("open")) {
			Response.sendFaildResponse(response, 404, "the page not exist !");
			return;
		}
		try {
			NotificationDao dao = new NotificationDao();
			Notification notification = dao.getNotification(notificationId);
			if(notification == null) {
				Response.sendFaildResponse(response, 404, "notification not exist !");
				return;
			}
			if(notification.user_id != currentUser.id) {
				Response.sendFaildResponse(response, 401, "you unauthorized to make this operation !");
				return;
			}
			dao.openNotification(notificationId);
			dao.close();
			Response.sendSuccessedResponse(response, 201, "the notification is set opened");
		} catch(Exception e) {
			Response.sendFaildResponse(response, 500, "unknown error occurred !");
			return;
		}
	}

	/**
	 * @see HttpServlet#doPut(HttpServletRequest, HttpServletResponse)
	 */
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(request.getAttribute("user") == null) {
			Response.sendFaildResponse(response, 401, "you unauthorized to make this operation !");
			return;
		}
		User currentUser = (User)request.getAttribute("user");
		String p = request.getPathInfo();
		if(p == null) p = "/";
		String[] path = p.split("/");
		if(path.length > 1 && path[1].equals("settings")) {
			doPutNotificationsSettings(request, response, currentUser);
		} else {
			Response.sendFaildResponse(response, 404, "this page is not exist !");
		}
	}

}
