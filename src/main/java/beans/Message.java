package beans;

public class Message {
	public int id;
	public int sender_id;
	public int receiver_id = -1;
	public String content;
	public String time;
	public String before_how_many;
	public boolean readed;
	public User sender;
}
