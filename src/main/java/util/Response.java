package util;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

public class Response {
	public boolean status;
	public String message;
	public Object data;
	public Response(boolean status, String message) {
		this.status = status;
		this.message = message;
	}
	public Response(boolean status, String message, Object data) {
		this.status = status;
		this.message = message;
		this.data = data;
	}
	
	public static void sendFaildResponse(HttpServletResponse response, int statusCode, String message) throws IOException {
		response.setStatus(statusCode);
		response.setCharacterEncoding("utf-8");
		Response r = new Response(false, message);
		response.setContentType("application/json");
		Gson gson = new Gson();
		PrintWriter out = response.getWriter();
		gson.toJson(r, out);
		out.flush();
	}
	public static void sendSuccessedResponse(HttpServletResponse response, int statusCode, String message) throws IOException {
		response.setStatus(statusCode);
		response.setCharacterEncoding("utf-8");
		Response r = new Response(true, message);
		response.setContentType("application/json");
		Gson gson = new Gson();
		PrintWriter out = response.getWriter();
		gson.toJson(r, out);
		out.flush();
	}
	
	public static <T> void sendResponse(HttpServletResponse response, int statusCode, T resource) throws IOException {
		response.setStatus(statusCode);
		response.setCharacterEncoding("utf-8");
		response.setContentType("application/json");
		Gson gson = new Gson();
		PrintWriter out = response.getWriter();
		gson.toJson(resource, out);
		out.flush();
	}
}
