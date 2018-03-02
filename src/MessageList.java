import java.util.LinkedList;

public class MessageList {
	
	public static Message getCurrentMessage(LinkedList <Message> messagesList) {
		return messagesList.get(messagesList.size() - 1);
	}
	
	public static Message getMessage(LinkedList <Message> messagesList, int id) {
		return messagesList.get(id);
	}
	
	public static LinkedList<Message> deleteMessage(int id, LinkedList<Message> allMessages) {
		allMessages.remove(id);
		LinkedList<Message> allUsersWithMessages = allMessages;
		
		return allUsersWithMessages;
	}
}
