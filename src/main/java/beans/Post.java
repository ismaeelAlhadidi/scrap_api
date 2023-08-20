package beans;

public class Post {
	public static final int POST_MAX_IMAGES_COUNT = 10;
	public int id;
	public int user_id;
	public double price = -1;
	public String governorateName;
	public String region;
	public String categoryName;
	public String title;
	public String description;
	public String quantityOrTimeOfUse;
	public String type;
	public String time;
	public String before_how_many;
	public String negotiation; 
	public String selling; 
	public String exchangeTitle;  
	public String exchangeDescription;
	public boolean with_comments;
	public boolean is_phone_visible;
	public String[] images;
	public String[] exchangeImages;
	public Boolean is_visible;
	public Boolean is_favorite;
	public int comments_count;
	public User user;
}
