package resources;

import beans.User;

public class LoginResource {
	
	public boolean status;
	public String jwt;
	public int id;
	public String name;
	public String email;
	public String phone;
	public String governorateName;
	public String region;
	public boolean is_phone_visible;
	public String birthday;
	public String sex;
	public String profession;
	
	public LoginResource(User user, String jwt, boolean status) {
		this.status = status;
		id = user.getId();
		name = user.getName();
		email = user.getEmail();
		phone = user.getPhone();
		governorateName = user.getGovernorateName();
		region = user.getRegion();
		is_phone_visible = user.is_phone_visible;
		birthday = user.birthday;
		sex = user.sex;
		profession = user.profession;
		this.jwt = jwt;
	}
}
