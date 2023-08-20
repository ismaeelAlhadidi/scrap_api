package servlets;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import util.ImageHandler;

/**
 * Servlet implementation class UIServlet
 */
@WebServlet("/*")
public class UIServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	//private String rootDir = "/opt/tomcat/static_files";
    private String rootDir = "C:\\Users\\esmae\\Desktop";
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UIServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String p = request.getPathInfo();
		if(p == null) p = "/";
		String[] path = p.split("/");
		if(p.equals("/vite.svg") || path.length > 1 && (path[1].equals("assets") || path[1].equals("storage"))) {
			try {
				String temp = "";
				String type = "image/" + p.split("\\.")[p.split("\\.").length-1];
				if(p.endsWith(".js")) {
					temp = ImageHandler.getFile(rootDir + p);
					type = "application/javascript";
					response.setContentType(type);
					response.getWriter().append(temp);
				} else if(p.endsWith(".svg")) {
					temp = ImageHandler.getFile(rootDir + p);
					type = "image/svg+xml";
					response.setContentType(type);
					response.getWriter().append(temp);
				} else if(p.endsWith(".css")) {
					temp = ImageHandler.getFile(rootDir + p);
					type = "text/css";
					response.setContentType(type);
					response.getWriter().append(temp);
				} else {
					response.setContentType(type);
					response.setContentLength(ImageHandler.writeImageToOutputSteam(rootDir + p, response.getOutputStream()));
				}
			} catch (Exception e) {}
		} else {
			try {
				response.setCharacterEncoding("UTF-8");
				response.getWriter().append(ImageHandler.getFile(rootDir + "/index.html"));
				response.setCharacterEncoding("UTF-8");
			} catch (Exception e) {}
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
