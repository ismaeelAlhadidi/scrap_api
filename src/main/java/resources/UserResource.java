package resources;

import beans.User;

public class UserResource {
	
	public int id;
	public String name;
	public String email;
	public String phone;
	public String governorateName;
	public String region;
	public boolean is_phone_visible;
	public Boolean is_active;
	public String birthday;
	public String sex;
	public String profession;
	
	public UserResource(User user) {
		id = user.getId();
		name = user.getName();
		email = user.getEmail();
		phone = user.getPhone();
		governorateName = user.getGovernorateName();
		region = user.getRegion();
		is_phone_visible = user.is_phone_visible;
		is_active = user.is_active;
		birthday = user.birthday;
		sex = user.sex;
		profession = user.profession;
	}
}
