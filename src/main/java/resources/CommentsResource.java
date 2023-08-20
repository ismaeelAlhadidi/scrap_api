package resources;

import java.util.List;

import beans.Comment;

public class CommentsResource {
	int pages;
	String prev_page;
	String current_page;
	String next_page;
	List<Comment> comments;
	public CommentsResource(List<Comment> comments, int pages, String prev_page, String current_page, String next_page) {
		this.comments = comments;
		this.pages = pages;
		this.prev_page = prev_page;
		this.current_page = current_page;
		this.next_page = next_page;
	}
}
