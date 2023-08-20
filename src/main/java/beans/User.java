package beans;

public class User {
	public int id;
	public String name;
	public String email;
	public String phone;
	public boolean is_phone_visible;
	public String password;
	public String governorateName;
	public String region;
	public Boolean is_active;
	public String birthday;
	public String sex;
	public String profession;
	public String sent_code;
	public String sent_time;
	public String verification_type;
	public Boolean is_phone_verified;
	public Boolean get_notifications_on_comments;
	public Boolean get_notifications_on_messages;
	public Boolean get_notifications_on_your_ads;
	public Boolean get_notifications_on_favorites;
	public Boolean get_notifications_on_scrapi;
	public Boolean get_notifications_on_scrap;
	
	public int getId() {
		return this.id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getGovernorateName() {
		return governorateName;
	}
	public void setGovernorateName(String governorateName) {
		this.governorateName = governorateName;
	}
	
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}
}
