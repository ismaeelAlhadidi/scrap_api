package dao;

import beans.Admin;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import beans.Post;
import beans.User;

public class AdminDao {
	public DBConnection dbConnection;
	
	public AdminDao() throws Exception {
		dbConnection = new DBConnection();
	}
	
	public AdminDao(DBConnection dbConnection) {
		this.dbConnection = dbConnection;
	}
	
	public Admin getAdmin(int id) throws Exception {
		String sql = "select * from admins where id=" + id;
		Admin admin = null;
		ResultSet result = dbConnection.executeQuery(sql);
		if(result.next()) {
			admin = new Admin();
			admin.id = result.getInt("id");
			admin.email = result.getString("email");
		}
		return admin;
	}
	
	public boolean login(Admin admin) throws Exception {
		String sql = "select * from admins where email=?";
		PreparedStatement statement = dbConnection.getConnection().prepareStatement(sql);
		statement.setString(1, admin.email);
		ResultSet result = statement.executeQuery();
		if(result.next()) {
			if(result.getString("password").equals(admin.password)) {
				admin.id = result.getInt("id");
				return true;
			} else return false;
		}
		return false;
	}
	
	public boolean setPostHidden(int postId) throws Exception {
		String sql = "select is_visible from posts where id=" + postId;
		ResultSet result = dbConnection.executeQuery(sql);
		if(result.next()) {
			if(! (boolean)result.getBoolean("is_visible")) {
				return true;
			}
		} else {
			throw new Exception();
		}
		sql = "update posts set is_visible=false where id=" + postId;
		return dbConnection.executeUpdate(sql) == 1;
	}
	
	public boolean setPostVisible(int postId) throws Exception {
		String sql = "select is_visible from posts where id=" + postId;
		ResultSet result = dbConnection.executeQuery(sql);
		if(result.next()) {
			if((boolean)result.getBoolean("is_visible")) {
				return true;
			}
		} else {
			throw new Exception();
		}
		sql = "update posts set is_visible=true where id=" + postId;
		return dbConnection.executeUpdate(sql) == 1;
	}
	
	public boolean isPostExist(int postId) throws Exception {
		String sql = "select id from posts where id=" + postId;
		ResultSet result = dbConnection.executeQuery(sql);
		if(result.next()) {
			return true;
		}
		return false;
	}
	
	public int getPages() throws Exception {
		String sql = "select count(id) as count from posts;";
		ResultSet result = dbConnection.executeQuery(sql);
		if(result.next()) {
			return result.getInt("count")/PostDao.PAGE_SIZE + (result.getInt("count") % PostDao.PAGE_SIZE == 0 ? 0 : 1);
		} else {
			throw new Exception();
		}
	}
	public List<Integer> getPostsIds(int page) throws Exception {
		String sql = "select id from posts order by time desc limit " + PostDao.PAGE_SIZE + " offset " + (page*PostDao.PAGE_SIZE) + ";";
		List<Integer> ids = new ArrayList<Integer>();
		ResultSet result = dbConnection.executeQuery(sql);
		while(result.next()) {
			ids.add(result.getInt("id"));
		}
		return ids;
	}
	
	public List<Post> getPosts(int page) throws Exception {
		List<Integer> ids = getPostsIds(page);
		if(ids.isEmpty()) {
			return new ArrayList<Post>();
		}
		String sql = PostDao.getPostsQuery();
		sql += "where posts.id in " + PostDao.createInValueFromArrayList(ids) + " ";
		sql += "order by posts.time desc ";
		sql += "limit " + PostDao.PAGE_SIZE + " ";
		sql += "offset " + (page*PostDao.PAGE_SIZE) + ";";
		ResultSet result = dbConnection.executeQuery(sql);
		return getPostsFromResult(result);
	}
	
	private List<Post> getPostsFromResult(ResultSet result) throws Exception {
		List<Post> posts = new ArrayList<Post>();
		Map<Integer, Post> map = new HashMap<Integer, Post>();
		Map<Integer, Set<String>> images = new HashMap<Integer, Set<String>>();
		Map<Integer, Set<String>> exchangeImages = new HashMap<Integer, Set<String>>();
		while(result.next()) {
			Post post = map.get(result.getInt("posts.id"));
			if(post == null) {
				post = new Post();
				post.id = result.getInt("posts.id");
				post.categoryName = result.getString("posts.category_name");
				post.governorateName = result.getString("posts.governorate_name");
				post.region = result.getString("posts.region_name");
				post.title = result.getString("posts.title");
				post.description = result.getString("posts.description");
				post.price = result.getDouble("posts.price");
				post.quantityOrTimeOfUse = result.getString("posts.quantity_or_time_of_use");
				post.is_phone_visible = result.getBoolean("posts.is_phone_visible");
				post.type = result.getString("posts.type");
				post.negotiation = result.getString("posts.negotiation");
				post.selling = result.getString("posts.selling");
				post.exchangeTitle = result.getString("posts.exchange_title");
				post.exchangeDescription = result.getString("posts.exchange_description");
				post.with_comments = result.getBoolean("posts.with_comments");
				post.time = result.getString("time");
				post.before_how_many = util.Time.beforeFormat(post.time);
				post.is_visible = result.getBoolean("is_visible");
				post.user_id = result.getInt("posts.user_id");
				post.user = new User();
				post.user.id = result.getInt("users.id");
				post.user.email = result.getString("users.email");	
				post.user.phone = result.getString("users.phone");
				post.user.is_phone_visible = result.getBoolean("users.is_phone_visible");
				post.user.name = result.getString("users.name");
				post.user.governorateName = result.getString("users.governorate_name");
				post.user.region = result.getString("users.region_name");
				map.put(post.id, post);
				images.put(post.id, new HashSet<String>());
				exchangeImages.put(post.id, new HashSet<String>());
				posts.add(post);
			}
			boolean is_exchange = result.getBoolean("images.is_exchange");
			String imageSrc = result.getString("images.src");
			Set<String> temp;
			if(is_exchange) {
				temp = exchangeImages.get(post.id);
				if(imageSrc != null) {
					temp.add(imageSrc);
				}
			} else {
				temp = images.get(post.id);
				if(imageSrc != null) {
					temp.add(imageSrc);
				}
			}
		}
		List<Integer> ids = new ArrayList<Integer>();
		for(Post post : posts) {
			Set<String> temp = images.get(post.id);
			post.images = new String[temp.size()];
			int i = 0;
			for(String image : temp) {
				post.images[i++] = image;
			}
			temp = exchangeImages.get(post.id);
			post.exchangeImages = new String[temp.size()];
			i = 0;
			for(String image : temp) {
				post.exchangeImages[i++] = image;
			}
			ids.add(post.id);
		}
		if(! ids.isEmpty()) {
			String sql = "select post_id, count(id) as count from comments where post_id in " + PostDao.createInValueFromArrayList(ids) + " ";
			sql += "group by post_id;";
			ResultSet r = dbConnection.executeQuery(sql);
			while(r.next()) {
				Post post = map.get(r.getInt("post_id"));
				post.comments_count = r.getInt("count");
			}
		}
		return posts;
	}
	
	public void close() {
		dbConnection.close();
	}
}
