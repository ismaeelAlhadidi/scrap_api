package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import beans.Message;
import beans.MessageWindow;
import beans.User;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class MessageDao {
	
	public static final int PAGE_SIZE = 20;
	
	public DBConnection dbConnection;
	
	public MessageDao() throws Exception {
		dbConnection = new DBConnection();
	}
	
	public MessageDao(DBConnection dbConnection) {
		this.dbConnection = dbConnection;
	}
	
	public int getPagesOfSearchInMessages(String search, int userId) throws Exception {
		String sql = "select count(messages.id) as count from messages";
		sql += " where (messages.sender_id="+userId+" or messages.receiver_id="+userId +") ";
		sql += "and messages.content like ?;";
		PreparedStatement statement = dbConnection.getConnection().prepareStatement(sql);
		search = "%" + search +"%";
		statement.setString(1, search);
		ResultSet result = statement.executeQuery();
		if(result.next()) {
			return result.getInt("count")/PAGE_SIZE + (result.getInt("count") % PAGE_SIZE == 0 ? 0 : 1);
		} else {
			throw new Exception();
		}
	}
	
	public List<Message> searchInMessages(String search, int page, int userId) throws Exception {
		String sql = "select * from messages left join users on messages.sender_id=users.id";
		sql += " where (messages.sender_id="+userId+" or messages.receiver_id="+userId +") ";
		sql += "and messages.content like ? ";
		sql += "order by messages.time desc ";
		sql += "limit " + PAGE_SIZE + " ";
		sql += "offset " + (page*PAGE_SIZE) + ";";
		PreparedStatement statement = dbConnection.getConnection().prepareStatement(sql);
		search = "%" + search +"%";
		statement.setString(1, search);
		ResultSet result = statement.executeQuery();
		List<Message> messages = new ArrayList<Message>();
		while(result.next()) {
			Message message = new Message();
			message.id = result.getInt("messages.id");
			message.sender_id = result.getInt("messages.sender_id");
			message.receiver_id = result.getInt("messages.receiver_id");
			message.content = result.getString("messages.content");
			message.time = result.getString("messages.time");
			message.before_how_many = util.Time.beforeFormat(message.time);
			message.readed = result.getBoolean("messages.readed");
			message.sender = new User();
			message.sender.id = result.getInt("users.id");
			message.sender.email = result.getString("users.email");
			message.sender.is_active = result.getBoolean("users.is_active");
			message.sender.name = result.getString("users.name");
			messages.add(message);
		}
		return messages;
	}
	
	public void setReaded(int currentUserId, int userId) {
		try {
			String sql = "update messages set readed=true where sender_id=" + userId + " and receiver_id=" + currentUserId + ";";
			dbConnection.executeUpdate(sql);
		} catch(Exception e) {}
	}
	
	public int getPagesOfMessagesBetween(int user1, int user2) throws Exception {
		String sql = "select count(id) as count from messages ";
		sql += "where (sender_id=" + user1 + " and receiver_id=" + user2 + ") or (sender_id=" + user2 + " and receiver_id=" + user1 + ");";
		PreparedStatement statement = dbConnection.getConnection().prepareStatement(sql);
		ResultSet result = statement.executeQuery();
		if(result.next()) {
			return result.getInt("count")/PAGE_SIZE + (result.getInt("count") % PAGE_SIZE == 0 ? 0 : 1);
		} else {
			throw new Exception();
		}
	}
	
	public List<Message> getMessagesBetween(int user1, int user2, int page) throws Exception {
		String sql = "select * from messages ";
		sql += "where (sender_id=" + user1 + " and receiver_id=" + user2 + ") or (sender_id=" + user2 + " and receiver_id=" + user1 + ") ";
		sql += "order by time desc ";
		sql += "limit " + PAGE_SIZE + " ";
		sql += "offset " + (page*PAGE_SIZE) + ";";
		ResultSet result = dbConnection.executeQuery(sql);
		List<Message> messages = new ArrayList<Message>();
		while(result.next()) {
			Message message = new Message();
			message.id = result.getInt("id");
			message.sender_id = result.getInt("sender_id");
			message.receiver_id = result.getInt("receiver_id");
			message.content = result.getString("content");
			message.time = result.getString("time");
			message.before_how_many = util.Time.beforeFormat(message.time);
			message.readed = result.getBoolean("readed");
			messages.add(message);
		}
		return messages;
	}
	
	public List<MessageWindow> getMessagesWindowOf(int userId) throws Exception {
		String sql = "select max(id) as id, max(time) as time, (sender_id+receiver_id-" + userId + ") as user_id from messages ";
		sql += "where (sender_id=" + userId + " or receiver_id=" + userId + ") group by user_id order by time desc;";
		PreparedStatement statement = dbConnection.getConnection().prepareStatement(sql);
		ResultSet result = statement.executeQuery();
		List<MessageWindow> messageWindows = new ArrayList<MessageWindow>();
		Map<Integer, Message> messagesMap = new HashMap<Integer, Message>();
		Map<Integer, MessageWindow> usersMap = new HashMap<Integer, MessageWindow>();
		List<Integer> userIds = new ArrayList<Integer>();
		List<Integer> messageIds = new ArrayList<Integer>();
		while(result.next()) {
			MessageWindow messageWindow = new MessageWindow();
			messageWindow.user = new User();
			messageWindow.user.id = result.getInt("user_id");
			messageWindow.last_message = new Message();
			messageWindow.last_message.id = result.getInt("id");
			userIds.add(messageWindow.user.id);
			messageIds.add(messageWindow.last_message.id);
			usersMap.put(messageWindow.user.id, messageWindow);
			messagesMap.put(messageWindow.last_message.id, messageWindow.last_message);
			messageWindows.add(messageWindow);
		}
		try {
			result.close();
		} catch(Exception e) {}
		if(messageWindows.isEmpty()) return messageWindows;
		sql = "select * from users where id in " + PostDao.createInValueFromArrayList(userIds) + ";";
		result = dbConnection.executeQuery(sql);
		while(result.next()) {
			User user = usersMap.get(result.getInt("id")).user;
			user.name = result.getString("name");
			user.is_active = (boolean)result.getBoolean("is_active");
		}
		try {
			result.close();
		} catch(Exception e) {}
		sql = "select * from messages where id in " + PostDao.createInValueFromArrayList(messageIds) + ";";
		result = dbConnection.executeQuery(sql);
		while(result.next()) {
			Message message = messagesMap.get(result.getInt("id"));
			message.sender_id = result.getInt("sender_id");
			message.receiver_id = result.getInt("receiver_id");
			message.content = result.getString("content");
			message.readed = result.getBoolean("readed");
			message.time = result.getString("time");
			message.before_how_many = util.Time.beforeFormat(message.time);
		}
		try {
			result.close();
		} catch(Exception e) {}
		sql = "select sender_id, count(id) as count from messages where receiver_id=" + userId + " and readed=false group by sender_id;";
		result = dbConnection.executeQuery(sql);
		while(result.next()) {
			MessageWindow messageWindow = usersMap.get(result.getInt("sender_id"));
			if(messageWindow != null) {
				messageWindow.un_readed_messages_count = result.getInt("count");
			}
		}
		try {
			result.close();
		} catch(Exception e) {}
		return messageWindows;
	}
	
	public boolean createMessage(Message message) throws Exception {
		String sql = "insert into messages(sender_id, receiver_id, content) values(?, ?, ?);";
		PreparedStatement statement = dbConnection.getConnection().prepareStatement(sql);
		statement.setInt(1, message.sender_id);
		statement.setInt(2, message.receiver_id);
		statement.setString(3, message.content);
		return statement.executeUpdate() == 1;
	}
	
	public void close() {
		dbConnection.close();
	}
}
