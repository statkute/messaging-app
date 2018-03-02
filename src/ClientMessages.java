import java.util.HashMap;
import java.util.LinkedList;

public class ClientMessages {
	private HashMap<String, LinkedList<Message>> allUsersWithMessages = new HashMap<String, LinkedList<Message>>();

	public void addUser(String username) {
		allUsersWithMessages.put(username, new LinkedList<Message>());
	}

	public boolean userExists(String username) {
		if (allUsersWithMessages.containsKey(username))
			return true;
		else
			return false;
	}

	public LinkedList<Message> getAllUserMessages(String username) {
		return allUsersWithMessages.get(username);
	}

	public void addMessage(String username, Message message) {
		LinkedList<Message> listOfMessages = getAllUserMessages(username);
		listOfMessages.add(message);

		allUsersWithMessages.replace(username, listOfMessages);
	}
	
	
}
