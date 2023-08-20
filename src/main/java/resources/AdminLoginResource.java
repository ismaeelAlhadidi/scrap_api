package resources;

import beans.Admin;

public class AdminLoginResource {
	public boolean status;
	public String jwt;
	public int id;
	public String email;
	public AdminLoginResource(Admin admin, String jwt, boolean status) {
		this.status = status;
		id = admin.id;
		email = admin.email;
		this.jwt = jwt;
	}
}
