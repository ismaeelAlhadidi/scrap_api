package filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import util.JwtValidator;
import com.auth0.jwt.interfaces.DecodedJWT;

import dao.AdminDao;
import dao.UserDao;
import beans.User;
import beans.Admin;


public class JwtFilter implements Filter {
	
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest)req;
		HttpServletResponse response = (HttpServletResponse)resp;
		String authTokenHeader = request.getHeader("Authorization");
		if(authTokenHeader != null && authTokenHeader.length() > 7) {
			String token = authTokenHeader.substring(7, authTokenHeader.length());
			DecodedJWT jwt = JwtValidator.validate(token);
			if(jwt != null) {
				String test = jwt.getClaim("id").asString();
				int id = -1;
				try {
					id = Integer.parseInt(test);
				} catch(Exception e) {}
				if(id != -1) {
					try {
						UserDao dao = new UserDao();
						User user = dao.getUser(id);
						if(user != null) {
							request.setAttribute("user", user);
						}
						dao.close();
					} catch(Exception e) {}
				}
				
				/* FOR ADMIN */
				test = jwt.getClaim("admin_id").asString();
				int admin_id = -1;
				try {
					admin_id = Integer.parseInt(test);
				} catch(Exception e) {}
				if(admin_id != -1) {
					try {
						AdminDao dao = new AdminDao();
						Admin admin = dao.getAdmin(admin_id);
						if(admin != null) {
							request.setAttribute("admin", admin);
						}
						dao.close();
					} catch(Exception e) {}
				}
			}
		}
		chain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub
	}
	
}