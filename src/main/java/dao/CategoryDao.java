package dao;

import java.util.List;
import java.util.ArrayList;

import java.sql.ResultSet;

public class CategoryDao {
	
	public DBConnection dbConnection;
	
	public CategoryDao() throws Exception {
		dbConnection = new DBConnection();
	}
	
	public CategoryDao(DBConnection dbConnection) {
		this.dbConnection = dbConnection;
	}
	
	public List<String> getCategories() throws Exception {
		String sql = "select name from categories;";
		ResultSet result = dbConnection.executeQuery(sql);
		List<String> categories = new ArrayList<String>();
		while(result.next()) {
			categories.add(result.getString("name"));
		}
		return categories;
	}
	public void close() {
		dbConnection.close();
	}
}
