package resources;

import java.util.List;
import beans.Post;

public class PostsResource {
	int pages;
	String prev_page;
	String current_page;
	String next_page;
	List<Post> posts;
	public PostsResource(List<Post> posts, int pages, String prev_page, String current_page, String next_page) {
		this.posts = posts;
		this.pages = pages;
		this.prev_page = prev_page;
		this.current_page = current_page;
		this.next_page = next_page;
	}
}
