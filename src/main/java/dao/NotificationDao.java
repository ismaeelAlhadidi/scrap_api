package dao;

import java.util.List;

import java.sql.PreparedStatement;
import beans.Notification;
import java.sql.ResultSet;
import java.util.ArrayList;

public class NotificationDao {
	
	public DBConnection dbConnection;
	
	public final int PAGE_SIZE = 15;
	
	public NotificationDao() throws Exception {
		dbConnection = new DBConnection();
	}
	
	public NotificationDao(DBConnection dbConnection) throws Exception {
		this.dbConnection = dbConnection;
	}
	
	public boolean createNotificationsForUsersHaveThisPostAsFavorite(int postId, int postPublisherId, int commenterId, String commenterName) {
		String sql = "select favorites.user_id from favorites";
		sql += " left join users on users.id=favorites.user_id"; 
		sql += " where favorites.post_id=" + postId + " and users.get_notifications_on_favorites=true;";
		try {
			ResultSet result = dbConnection.executeQuery(sql);
			List<Integer> usersIds = new ArrayList<Integer>();
			int id;
			while(result.next()) {
				id = result.getInt("favorites.user_id");
				if(id != postPublisherId && id != commenterId) {
					usersIds.add(id);
				}
			}
			String content = "قام " + commenterName + " بالتعليق على اعلان من الاعلانات المفضلة لديك";
			sql = "insert into notifications(user_id, type, content) values(?, \"comment_on_your_favorite_post\", \"" + content + "\");";
			PreparedStatement statement = dbConnection.getConnection().prepareStatement(sql);
			for(int userId : usersIds) {
				statement.setInt(1, userId);
				statement.executeUpdate();
			}
			return true;
		} catch(Exception e) {
			return false;
		}
	}
	
	public boolean openNotification(int notificationId) {
		try {
			String sql = "update notifications set opened=true where id=" + notificationId + ";";
			return dbConnection.executeUpdate(sql) == 1;
		} catch(Exception e) {
			return false;
		}
	}
	
	private String createInValueFromArrayList(List<Notification> notifications) {
		String result = "(";
		for(int i = 0; i < notifications.size()-1; ++i) {
			result += notifications.get(i).id;
			result += ",";
		}
		result += notifications.get(notifications.size()-1).id;
		result += ")";
		return result;
	}
	
	public Notification getNotification(int id) throws Exception {
		String sql = "select * from notifications where notifications.id=" + id;
		Notification notification = null;
		ResultSet result = dbConnection.executeQuery(sql);
		if(result.next()) {
			notification = new Notification();
			notification.id = result.getInt("notifications.id");
			notification.user_id = result.getInt("notifications.user_id");
		}
		return notification;
	}
	
	public void readNotifications(List<Notification> notifications) {
		if(notifications.isEmpty()) return;
		try {
			String sql = "update notifications set notifications.readed=true where id in " + createInValueFromArrayList(notifications) + ";";
			dbConnection.executeUpdate(sql);
		} catch(Exception e) {}
	}
	
	public boolean createNotification(int userId, String type, String content) {
		try {
			String sql = "insert into notifications(user_id, type, content) values(?, ?, ?);";
			PreparedStatement statement = dbConnection.getConnection().prepareStatement(sql);
			statement.setInt(1, userId);
			statement.setString(2, type);
			statement.setString(3, content);
			return statement.executeUpdate() == 1;
		} catch(Exception e) {
			return false;
		}
	}
	
	public int getNewNotifications(int userId) throws Exception {
		String sql = "select count(notifications.id) as count from notifications where notifications.user_id=" + userId + " and notifications.readed=false;";
		ResultSet result = dbConnection.executeQuery(sql);
		if(result.next()) {
			return result.getInt("count");
		} else {
			throw new Exception();
		}
	}
	
	public int getPages(int userId, String search) throws Exception {
		String sql = "select count(notifications.id) as count from notifications where notifications.user_id=" + userId;
		if(search == null) {
			sql += ";";
		} else {
			search = "%" + search + "%";
			sql += " and notifications.content like ?;";
		}
		PreparedStatement statement = dbConnection.getConnection().prepareStatement(sql);
		if(search != null) {
			statement.setString(1, search);
		}
		ResultSet result = statement.executeQuery();
		if(result.next()) {
			return result.getInt("count")/PAGE_SIZE + (result.getInt("count") % PAGE_SIZE == 0 ? 0 : 1);
		} else {
			throw new Exception();
		}
	}
	
	public List<Notification> getNotifications(int userId, int page, String search) throws Exception {
		String sql = "select * from notifications ";
		sql += "where notifications.user_id=" + userId + " ";
		if(search != null) {
			search = "%" + search + "%";
			sql += "and notifications.content like ? ";
		}
		sql += "order by notifications.time desc ";
		sql += "limit " + PAGE_SIZE + " ";
		sql += "offset " + (page*PAGE_SIZE) + ";";
		PreparedStatement statement = dbConnection.getConnection().prepareStatement(sql);
		if(search != null) {
			statement.setString(1, search);
		}
		ResultSet result = statement.executeQuery();
		List<Notification> notifications = new ArrayList<Notification>();
		while(result.next()) {
			Notification notification = new Notification();
			notification.id = result.getInt("notifications.id");
			notification.content = result.getString("notifications.content");
			notification.type = result.getString("notifications.type");
			notification.readed = result.getBoolean("notifications.readed");
			notification.opened = result.getBoolean("notifications.opened");
			notification.user_id = result.getInt("notifications.user_id");
			notifications.add(notification);
		}
		return notifications;
	}
	
	public void close() {
		dbConnection.close();
	}
}
