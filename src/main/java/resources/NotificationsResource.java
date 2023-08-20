package resources;

import java.util.List;

import beans.Notification;

public class NotificationsResource {
	int pages;
	String prev_page;
	String current_page;
	String next_page;
	Integer new_notifications;
	List<Notification> notifications;
	public NotificationsResource(List<Notification> notifications, int pages, String prev_page, String current_page, String next_page, Integer new_notifications) {
		this.notifications = notifications;
		this.pages = pages;
		this.prev_page = prev_page;
		this.current_page = current_page;
		this.next_page = next_page;
		this.new_notifications = new_notifications;
	}
}
