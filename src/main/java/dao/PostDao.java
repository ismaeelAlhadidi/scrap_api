package dao;

import beans.Post;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;

import util.ImageHandler;
import util.Time;

import java.util.List;
import java.util.ArrayList;
import beans.User;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import beans.Comment;

public class PostDao {
	
	//private String storage = "/opt/tomcat/static_files";
	private String storage = "C:\\Users\\esmae\\Desktop";
	public DBConnection dbConnection;
	public static final int PAGE_SIZE = 20;
	
	public List<String> addedImages = new ArrayList<String>();
	
	public PostDao() throws Exception {
		dbConnection = new DBConnection();
	}
	
	public PostDao(DBConnection dbConnection) throws Exception {
		this.dbConnection = dbConnection;
	}
	
	public void setFavorites(List<Post> posts, User user) throws Exception {
		List<Integer> ids = new ArrayList<Integer>();
		for(Post post : posts) ids.add(post.id);
		String sql = "select post_id from favorites where user_id=" + user.getId() + " and post_id in " + createInValueFromArrayList(ids);
		ResultSet result = dbConnection.executeQuery(sql);
		Set<Integer> favorites = new HashSet<Integer>();
		while(result.next()) {
			favorites.add(result.getInt("post_id"));
		}
		for(Post post : posts) {
			if(favorites.contains(post.id)) {
				post.is_favorite = true;
			} else {
				post.is_favorite = false;
			}
		}
	}
	
	public boolean createReport(int userId, int postId) throws Exception {
		String sql = "select id from reports where user_id=" + userId + " and post_id=" + postId + ";";
		ResultSet result = dbConnection.executeQuery(sql);
		if(result.next()) {
			return true;
		}
		sql = "insert into reports(user_id, post_id) values(" + userId + "," + postId + ");";
		return dbConnection.executeUpdate(sql) == 1;
	}
	
	public int getPagesOfUserComments(int userId, String search) throws Exception {
		String sql = "select count(comments.id) as count from comments where comments.user_id=" + userId;
		if(search != null) {
			search = "%" + search+ "%";
			sql += " and comments.content like ?;";
		} else {
			sql += ";";
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
	
	public List<Comment> getUserComments(int userId, int page, String search) throws Exception {
		String sql = "select * from comments ";
		sql += "where comments.user_id=" + userId + " ";
		if(search != null) {
			search = "%" + search + "%";
			sql += "and comments.content like ? ";
		}
		sql += "order by time desc ";
		sql += "limit " + PAGE_SIZE + " ";
		sql += "offset " + (page*PAGE_SIZE) + ";";
		PreparedStatement statement = dbConnection.getConnection().prepareStatement(sql);
		if(search != null) {
			statement.setString(1, search);
		}
		ResultSet result = statement.executeQuery();
		List<Comment> comments = new ArrayList<Comment>();
		while(result.next()) {
			Comment comment = new Comment();
			comment.content = result.getString("comments.content");
			comment.id = result.getInt("comments.id");
			comment.time = result.getString("comments.time");
			comment.before_how_many = Time.beforeFormat(comment.time);
			comment.post_id = result.getInt("comments.post_id");
			comment.user_id = result.getInt("comments.user_id");
			comments.add(comment);
		}
		return comments;
	}
	
	public int getPagesOfFavoritesPost(int userId, String search) throws Exception {
		String sql = "select count(post_id) as count from favorites left join posts on favorites.post_id=posts.id ";
		sql += "where ";
		sql += "favorites.user_id=" + userId + " ";
		sql += "and (posts.is_visible=true or posts.user_id=favorites.user_id) ";
		if(search != null) {
			search = "%" + search + "%";
			sql += "and posts.title like ? ";
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
	
	public List<Integer> getFavoritesIds(int userId, int page, String search) throws Exception {
		String sql = "select post_id from favorites left join posts on favorites.post_id=posts.id ";
		sql += "where ";
		sql += "favorites.user_id=" + userId + " ";
		sql += "and (posts.is_visible=true or posts.user_id=favorites.user_id) ";
		if(search != null) {
			search = "%" + search + "%";
			sql += "and posts.title like ?";
		}
		sql += "order by time desc ";
		sql += "limit " + PAGE_SIZE + " ";
		sql += "offset " + (page*PAGE_SIZE) + ";";
		PreparedStatement statement = dbConnection.getConnection().prepareStatement(sql);
		if(search != null) {
			statement.setString(1, search);
		}
		ResultSet result = statement.executeQuery();
		List<Integer> postsId = new ArrayList<Integer>();
		while(result.next()) {
			postsId.add(result.getInt("favorites.post_id"));
		}
		return postsId;
	}
	public List<Post> getFavoritersPost(int userId, int page, String search) throws Exception {
		List<Integer> postsId = getFavoritesIds(userId, page, search);
		if(postsId.isEmpty()) {
			return new ArrayList<Post>();
		}
		String sql = getPostsQuery();
		sql += "where posts.id in " + createInValueFromArrayList(postsId);
		sql += "order by time desc;";
		PreparedStatement statement = dbConnection.getConnection().prepareStatement(sql);
		ResultSet result = statement.executeQuery();
		return getPostsFromResult(result);
	}
	
	public boolean deleteFromFavorites(int postId, int userId) throws Exception {
		String sql = "delete from favorites where user_id=" + userId + " and post_id=" + postId + ";";
		return dbConnection.executeUpdate(sql) == 1;
	}
	
	public boolean isInFavorites(int postId, int userId) throws Exception {
		String sql = "select * from favorites where user_id=" + userId + " and post_id=" + postId + ";";
		ResultSet result = dbConnection.executeQuery(sql);
		return result.next();
	}
	public boolean addToFavorite(int postId, int userId) throws Exception {
		String sql = "insert into favorites(post_id, user_id) values(?, ?);";
		PreparedStatement statement = dbConnection.getConnection().prepareStatement(sql);
		statement.setInt(1, postId);
		statement.setInt(2, userId);
		return statement.executeUpdate() == 1;
	}
	
	public int getPagesOfUserPosts(int userId, boolean isCurrentUser, String search) throws Exception {
		String sql = "select count(id) as count from posts ";
		sql += "where ";
		if(! isCurrentUser) {
			sql += "is_visible=true and ";
		}
		sql += "posts.user_id=" + userId;
		if(search != null) {
			search = "%"+search+"%";
			sql += " and posts.title like ?;";
		} else {
			sql += ";";
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
	
	private List<Integer> getUserPostsId(int userId, boolean isCurrentUser, int page, String search) throws Exception {
		String sql = "select id from posts ";
		sql += "where ";
		if(! isCurrentUser) {
			sql += "is_visible=true and ";
		}
		sql += "posts.user_id=" + userId + " ";
		if(search != null) {
			search = "%"+search+"%";
			sql += "and posts.title like ?";
		}
		sql += "order by time desc ";
		sql += "limit " + PAGE_SIZE + " ";
		sql += "offset " + (page*PAGE_SIZE) + ";";
		PreparedStatement statement = dbConnection.getConnection().prepareStatement(sql);
		if(search != null) {
			statement.setString(1, search);
		}
		ResultSet result = statement.executeQuery();
		List<Integer> postsId = new ArrayList<Integer>();
		while(result.next()) {
			postsId.add(result.getInt("id"));
		}
		return postsId;
		
	}
	public List<Post> getUserPosts(int userId, boolean isCurrentUser, int page, String search) throws Exception {
		List<Integer> postsId = getUserPostsId(userId, isCurrentUser, page, search);
		if(postsId.isEmpty()) {
			return new ArrayList<Post>();
		}
		String sql = getPostsQuery();
		sql += "where posts.id in " + createInValueFromArrayList(postsId);
		
		sql += "order by time desc;";
		PreparedStatement statement = dbConnection.getConnection().prepareStatement(sql);
		ResultSet result = statement.executeQuery();
		return getPostsFromResult(result);
	}
	
	public List<Comment> getComments(int postId) throws Exception {
		List<Comment> comments = new ArrayList<Comment>();
		String sql = "select * from comments left join users on comments.user_id=users.id where comments.post_id=" + postId +" order by time desc;";
		ResultSet result = dbConnection.executeQuery(sql);
		while(result.next()) {
			Comment comment = new Comment();
			comment.id = result.getInt("comments.id");
			comment.content = result.getString("comments.content");
			comment.post_id = result.getInt("comments.post_id");
			comment.user_id = result.getInt("comments.user_id");
			comment.time = result.getString("comments.time");
			comment.before_how_many = Time.beforeFormat(comment.time);
			comment.user = new User();
			comment.user.id = result.getInt("users.id");
			comment.user.email = result.getString("users.email");
			comment.user.is_phone_visible = result.getBoolean("users.is_phone_visible");
			if(comment.user.is_phone_visible) {
				comment.user.phone = result.getString("phone");
			}
			comment.user.governorateName = result.getString("users.governorate_name");
			comment.user.region = result.getString("users.region_name");
			comment.user.name = result.getString("name");
			comments.add(comment);
		}
		return comments;
	}
	
	public boolean addComment(Comment comment) throws Exception {
		String sql = "insert into comments(content, post_id, user_id) values(?, ?, ?);";
		PreparedStatement statement = dbConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		statement.setString(1, comment.content);
		statement.setInt(2, comment.post_id);
		statement.setInt(3, comment.user_id);
		boolean ok = statement.executeUpdate() == 1;
		int commentId = -1;
		ResultSet result = statement.getGeneratedKeys();
		if(result.next()) commentId = result.getInt(1);
		comment.id = commentId;
		return ok && commentId != -1;
	}
	
	public int getPages(int page, String type, String search, 
			String category_name, String governorateName, String date_after) throws Exception {
		String sql = "select count(id) as count from posts ";
		sql += "where is_visible=true ";
		if(type != null) {
			sql += "and type=\"" + type + "\" ";
		}
		if(category_name != null) {
			sql += "and category_name=\"" + category_name + "\" ";
		}
		if(governorateName != null) {
			sql += "and governorate_name=\"" + governorateName + "\" ";
		}
		if(date_after != null) {
			sql += "and time>\"" + date_after + " 23:59:59.99\"";
		}
		if(search != null) {
			search = "%" + search + "%";
			sql += "and title like ? ";
		}
		sql +=  ";";
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
	
	private List<Integer> getPostsId(int page, String type, String search, 
			String category_name, String governorateName, String date_after) throws Exception {
		String sql = "select id from posts ";
		sql += "where is_visible=true ";
		if(type != null) {
			sql += "and type=\"" + type + "\" ";
		}
		if(category_name != null) {
			sql += "and category_name=\"" + category_name + "\" ";
		}
		if(governorateName != null) {
			sql += "and governorate_name=\"" + governorateName + "\" ";
		}
		if(date_after != null) {
			sql += "and time>\"" + date_after + " 23:59:59.99\"";
		}
		if(search != null) {
			search = "%" + search + "%";
			sql += "and title like ? ";
		}
		sql += "order by time desc ";
		sql += "limit " + PAGE_SIZE + " ";
		sql += "offset " + (page*PAGE_SIZE) + ";";
		PreparedStatement statement = dbConnection.getConnection().prepareStatement(sql);
		if(search != null) {
			statement.setString(1, search);
		}
		ResultSet result = statement.executeQuery();
		List<Integer> postsId = new ArrayList<Integer>();
		while(result.next()) {
			postsId.add(result.getInt("id"));
		}
		return postsId;
	}
	public static String createInValueFromArrayList(List<Integer> list) {
		String result = "(";
		for(int i = 0; i < list.size()-1; ++i) {
			result += (int)list.get(i);
			result += ",";
		}
		result += (int)list.get(list.size()-1);
		result += ")";
		return result;
	}
	public List<Post> getPosts(int page, String type, String search, 
			String category_name, String governorateName, String date_after) throws Exception {
		List<Integer> postsId = getPostsId(page, type, search, category_name, governorateName, date_after);
		if(postsId.isEmpty()) {
			return new ArrayList<Post>();
		}
		String sql = getPostsQuery();
		sql += "where posts.id in " + createInValueFromArrayList(postsId);
		sql += "order by time desc;";
		PreparedStatement statement = dbConnection.getConnection().prepareStatement(sql);
		ResultSet result = statement.executeQuery();
		return getPostsFromResult(result);
	}
	public Post getPost(int postId) throws Exception {
		Post post = null;
		String sql = getPostsQuery();
		sql += "where posts.id=? and is_visible=true;";
		PreparedStatement statement = dbConnection.getConnection().prepareStatement(sql);
		statement.setInt(1, postId);
		ResultSet result = statement.executeQuery();
		List<Post> posts = getPostsFromResult(result);
		if(posts.size() > 0) {
			post = posts.get(0);
		}
		return post;
	}
	
	public static String getPostsQuery() {
		return "select * from posts left join users on posts.user_id=users.id left join images on posts.id=images.post_id ";
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
			String sql = "select post_id, count(id) as count from comments where post_id in " + createInValueFromArrayList(ids) + " ";
			sql += "group by post_id;";
			ResultSet r = dbConnection.executeQuery(sql);
			while(r.next()) {
				Post post = map.get(r.getInt("post_id"));
				post.comments_count = r.getInt("count");
			}
		}
		return posts;
	}
	
	public boolean createPost(Post post) {
		Connection connection = dbConnection.getConnection();
		PreparedStatement addPostStatement = null, addImageStatement = null;
		ImageHandler imageHandler = new ImageHandler();
		boolean postCreated = false;
		try {
			connection.setAutoCommit(false);
			String sql = "insert into posts(category_name, governorate_name, region_name, "
					+ "title, description,price,quantity_or_time_of_use,user_id,"
					+ "is_phone_visible,type,negotiation,selling,exchange_title,exchange_description,with_comments) "
					+ "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
			addPostStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			addPostStatement.setString(1, post.categoryName);
			addPostStatement.setString(2, post.governorateName);
			addPostStatement.setString(3, post.region);
			addPostStatement.setString(4, post.title);
			addPostStatement.setString(5, post.description);
			addPostStatement.setDouble(6, post.price);
			addPostStatement.setString(7, post.quantityOrTimeOfUse);
			addPostStatement.setInt(8, post.user_id);
			addPostStatement.setBoolean(9, post.is_phone_visible);
			addPostStatement.setString(10, post.type);
			addPostStatement.setString(11, post.negotiation);
			addPostStatement.setString(12, post.selling);
			addPostStatement.setString(13, post.exchangeTitle);
			addPostStatement.setString(14, post.exchangeDescription);
			addPostStatement.setBoolean(15, post.with_comments);
			boolean ok = addPostStatement.executeUpdate() == 1;
			int postId = -1;
			ResultSet result = addPostStatement.getGeneratedKeys();
			if(result.next()) postId = result.getInt(1);
			if(ok && postId != -1) {
				post.id = postId;
				addImages(addImageStatement, post, imageHandler);
				connection.commit();
				postCreated = true;
			} else {
				try {
					connection.rollback();
				} catch(Exception ex) {}
				imageHandler.delete(addedImages);
			}
		} catch(Exception e) {
			try {
				connection.rollback();
			} catch(Exception ex) {}
			imageHandler.delete(addedImages);
		} finally {
			try {
				if(addPostStatement != null) addPostStatement.close();
			} catch(Exception ex) {}
			try {
				if(addImageStatement != null) addImageStatement.close();
			} catch(Exception ex) {}
			try {
				connection.close();
			} catch(Exception ex) {}
		}
		return postCreated;
	}
	
	private void addImages(PreparedStatement statement, Post post, ImageHandler imageHandler) throws Exception {
		String sql = "insert into images(post_id, is_exchange, src) values(?, ?, ?);";
		statement = dbConnection.getConnection().prepareStatement(sql);
		String src;
		for(String image : post.images) {
			src = imageHandler.store(storage, image);
			addedImages.add(src);
			statement.setInt(1, post.id);
			statement.setBoolean(2, false);
			statement.setString(3, src);
			if(statement.executeUpdate() != 1) {
				throw new Exception();
			}
		}
		if(post.exchangeImages != null) {
			for(String image : post.exchangeImages) {
				src = imageHandler.store(storage, image);
				addedImages.add(src);
				statement.setInt(1, post.id);
				statement.setBoolean(2, true);
				statement.setString(3, src);
				if(statement.executeUpdate() != 1) {
					throw new Exception();
				}
			}
		}
	}
	public void close() {
		dbConnection.close();
	}
}
