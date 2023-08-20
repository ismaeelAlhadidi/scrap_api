package dao;

import beans.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import util.PasswordHandler;

public class UserDao {
	
	public DBConnection dbConnection;
	
	public UserDao() throws Exception {
		dbConnection = new DBConnection();
	}
	
	public UserDao(DBConnection dbConnection) {
		this.dbConnection = dbConnection;
	}
	
	public boolean setNotificationsSettings(User user) throws Exception {
		String sql = "update users set ";
		sql += "get_notifications_on_comments=?";
		sql += ", get_notifications_on_messages=?"; 
		sql += ", get_notifications_on_your_ads=?";
		sql += ", get_notifications_on_favorites=?";
		sql += ", get_notifications_on_scrapi=?";
		sql += ", get_notifications_on_scrap=?";
		sql += " where id=" + user.id;
		PreparedStatement statement = dbConnection.getConnection().prepareStatement(sql);
		statement.setBoolean(1, user.get_notifications_on_comments);
		statement.setBoolean(2, user.get_notifications_on_messages);
		statement.setBoolean(3, user.get_notifications_on_your_ads);
		statement.setBoolean(4, user.get_notifications_on_favorites);
		statement.setBoolean(5, user.get_notifications_on_scrapi);
		statement.setBoolean(6, user.get_notifications_on_scrap);
		return statement.executeUpdate() == 1;
	}
	
	public boolean setUserPhoneAsVerified(int userId) throws Exception {
		String sql = "update users set phone_verified=true where id=" + userId;
		return dbConnection.executeUpdate(sql) == 1;
	}
	
	public boolean setVerificationCode(int userId, String code, String verificationType) throws Exception { 
		String sql = "update users set sent_code=?, sent_time=CURRENT_TIMESTAMP, verification_type=? where id=?;";
		PreparedStatement statement = dbConnection.getConnection().prepareStatement(sql);
		statement.setString(1, code);
		statement.setString(2, verificationType);
		statement.setInt(3, userId);
		return statement.executeUpdate() == 1;
	}
	
	public void login(int userId) throws Exception {
		String sql = "update users set is_active=true where id=" + userId + ";";
		dbConnection.executeUpdate(sql);
	}
	
	public void logout(int userId) throws Exception {
		String sql = "update users set is_active=false where id=" + userId + ";";
		dbConnection.executeUpdate(sql);
	}
	
	public boolean weHaveGovernorate(String name) throws Exception {
		String sql = "select name from governorates where name=?;";
		Connection connection = dbConnection.getConnection();
		PreparedStatement statement = connection.prepareStatement(sql);
		statement.setString(1, name);
		ResultSet result = statement.executeQuery();
		if(result.next()) {
			return true;
		}
		return false;
	}
	
	public boolean isUser(int id) throws Exception {
		String sql = "select id from users where id=" + id + ";";
		ResultSet result = dbConnection.executeQuery(sql);
		if(result.next()) {
			return true;
		}
		return false;
	}
	
	public boolean isEmailUsed(String email) throws Exception {
		String sql = "select email from users where email=?;";
		Connection connection = dbConnection.getConnection();
		PreparedStatement statement = connection.prepareStatement(sql);
		statement.setString(1, email);
		ResultSet result = statement.executeQuery();
		if(result.next()) {
			return true;
		}
		return false;
	}
	
	public boolean isPhoneUsed(String phone) throws Exception {
		String sql = "select phone from users where phone=?;";
		Connection connection = dbConnection.getConnection();
		PreparedStatement statement = connection.prepareStatement(sql);
		statement.setString(1, phone);
		ResultSet result = statement.executeQuery();
		if(result.next()) {
			return true;
		}
		return false;
	}
	
	public boolean updateUser(User user, User currentUser) throws Exception {
		String sql = "update users set name=?, is_phone_visible=?, governorate_name=?, region_name=?";
		if(user.birthday != null) {
			sql += ", birthday=?";
		}
		if(user.sex != null) {
			sql += ", sex=?";
		}
		if(user.profession != null) {
			sql += ", profession=?";
		}
		sql += " where id=" + currentUser.getId() + ";";
		Connection connection = dbConnection.getConnection();
		PreparedStatement statement = connection.prepareStatement(sql);
		statement.setString(1, user.getName());
		statement.setBoolean(2, user.is_phone_visible);
		statement.setString(3, user.getGovernorateName());
		statement.setString(4, user.getRegion());
		int i = 5;
		if(user.birthday != null) {
			statement.setString(i, user.birthday);
			++i;
		}
		if(user.sex != null) {
			statement.setString(i, user.sex);
			++i;
		}
		if(user.profession != null) {
			statement.setString(i, user.profession);
		}
		return statement.executeUpdate() == 1;
	}
	
	public boolean changePassword(int userId, String new_password) throws Exception {
		String sql = "update users set password=?, password_salt=? where id=" + userId;
		PreparedStatement statement = dbConnection.getConnection().prepareStatement(sql);
		String salt = PasswordHandler.generateSalt();
		String password = PasswordHandler.generateHash(salt, new_password);
		statement.setString(1, password);
		statement.setString(2, salt);
		return statement.executeUpdate() == 1;
	}
	
	public boolean createUser(User user) throws Exception {
		String sql = "insert into users(name, email, phone, is_phone_visible, password, password_salt, governorate_name, region_name) values(?,?,?,?,?,?,?,?)";
		Connection connection = dbConnection.getConnection();
		PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		statement.setString(1, user.getName());
		statement.setString(2, user.getEmail());
		statement.setString(3, user.getPhone());
		statement.setBoolean(4, user.is_phone_visible);
		String salt = PasswordHandler.generateSalt();
		String password = PasswordHandler.generateHash(salt, user.getPassword());
		statement.setString(5, password);
		statement.setString(6, salt);
		statement.setString(7, user.getGovernorateName());
		statement.setString(8, user.getRegion());
		if(statement.executeUpdate() != 1) return false;
		ResultSet result = statement.getGeneratedKeys();
		int userId = -1;
		if(result.next()) userId = result.getInt(1);
		user.setId(userId);
		return userId != -1;
	}
	
	public User getUser(String phoneNumber) throws Exception {
		String sql = "select * from users where phone=?;";
		Connection connection = dbConnection.getConnection();
		PreparedStatement statement = connection.prepareStatement(sql);
		statement.setString(1, phoneNumber);
		ResultSet result = statement.executeQuery();
		User user = getUserData(result);
		if(user != null) {
			user.sent_code = result.getString("sent_code");
			user.sent_time = result.getString("sent_time");
			user.verification_type = result.getString("verification_type");
		}
		return user;
	}
	
	public User getUser(int id) throws Exception {
		String sql = "select * from users where id=?;";
		Connection connection = dbConnection.getConnection();
		PreparedStatement statement = connection.prepareStatement(sql);
		statement.setInt(1, id);
		ResultSet result = statement.executeQuery();
		return getUserData(result);
	}
	
	public User getUserData(ResultSet result) throws Exception {
		User user = null;
		if(result.next()) {
			user = new User();
			user.setId(result.getInt("id"));
			user.setName(result.getString("name"));
			user.setEmail(result.getString("email"));
			user.setGovernorateName(result.getString("governorate_name"));
			user.setRegion(result.getString("region_name"));
			user.setPhone(result.getString("phone"));
			user.is_phone_visible = result.getBoolean("is_phone_visible");
			user.birthday = result.getString("birthday");
			user.sex = result.getString("sex");
			user.profession = result.getString("profession");
			user.is_phone_verified = result.getBoolean("phone_verified");
			user.get_notifications_on_comments = result.getBoolean("get_notifications_on_comments");
			user.get_notifications_on_messages = result.getBoolean("get_notifications_on_messages");
			user.get_notifications_on_your_ads = result.getBoolean("get_notifications_on_your_ads");
			user.get_notifications_on_favorites = result.getBoolean("get_notifications_on_favorites");
			user.get_notifications_on_scrapi = result.getBoolean("get_notifications_on_scrapi");
			user.get_notifications_on_scrap = result.getBoolean("get_notifications_on_scrap");
		}
		return user;
	}
	
	public String[] getSaltAndPasswordAndId(String column, String value) throws Exception {
		String[] getSaltAndPasswordAndId = null;
		String sql = "select id, password, password_salt from users where " + column + "=?;";
		Connection connection = dbConnection.getConnection();
		PreparedStatement statement = connection.prepareStatement(sql);
		statement.setString(1, value);
		ResultSet result = statement.executeQuery();
		if(result.next()) {
			getSaltAndPasswordAndId = new String[3];
			getSaltAndPasswordAndId[0] = result.getString("password_salt");
			getSaltAndPasswordAndId[1] = result.getString("password");
			getSaltAndPasswordAndId[2] = result.getInt("id")+"";
		}
		return getSaltAndPasswordAndId;
	}
	
	public void close() {
		dbConnection.close();
	}
}
