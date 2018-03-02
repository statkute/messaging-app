import java.io.Serializable;
import java.util.LinkedList;

public class User implements Serializable{
	private static final long serialVersionUID = 42L;
	public String username;
	public byte[] password;
	public LinkedList <Message> messageList;
	
	User (String username, byte[] password, LinkedList <Message> messageList){
		this.username = username;
		this.password = password;
		this.messageList = messageList;
	}
}
