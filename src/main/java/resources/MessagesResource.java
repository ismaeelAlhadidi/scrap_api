package resources;

import java.util.List;

import beans.Message;

public class MessagesResource {
	int pages;
	String prev_page;
	String current_page;
	String next_page;
	List<Message> messages;
	public MessagesResource(List<Message> messages, int pages, String prev_page, String current_page, String next_page) {
		this.messages = messages;
		this.pages = pages;
		this.prev_page = prev_page;
		this.current_page = current_page;
		this.next_page = next_page;
	}
}
