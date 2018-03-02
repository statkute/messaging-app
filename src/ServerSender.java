
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;

// Continuously reads from message queue for a particular client,
// forwarding to the client.

public class ServerSender extends Thread {
	private LinkedList<Message> clientMessages;
	private DataOutputStream client;
	private int size = 0;
	protected String command = "continue";
	private int id = 0;
	boolean browsing = false;
	private User user;
	private ArrayList<User> backupInformation;
	private boolean changeInMessageList = false;
	
	/**
	 * Constructs a new server sender.
	 * 
	 * @param linkedList
	 *            messages from this queue will be sent to the client
	 * @param toClient
	 *            the stream used to send data to the client
	 * @param user 
	 * @param backupInformation 
	 */
	public ServerSender(LinkedList<Message> linkedList, DataOutputStream toClient, User user, ArrayList<User> backupInformation) {
		clientMessages = linkedList;
		client = toClient;
		this.user = user;
		this.backupInformation = backupInformation;
	}

	/**
	 * Starts this server sender.
	 */
	public void run() {
		while (true) {
			System.out.flush();

			switch (command) {
			case "continue":
				if (!clientMessages.isEmpty()) {
					if (clientMessages.size() > size) {
						Message msg = MessageList.getCurrentMessage(clientMessages); 

						sendMessage("NEW MESSAGE ---> ", msg);
						size = clientMessages.size();
						if (!browsing) {
							resetId();
						}
						changeInMessageList = true;
					}
				}
				break;
			case "current":
				if (!clientMessages.isEmpty()) {
					Message msg = MessageList.getCurrentMessage(clientMessages);

					sendMessage("CURRENT MESSAGE ---> ", msg);
					command = "continue";
					resetId();
					browsing = false;
				} else {
					try {
						client.writeUTF("You have no messages");
						client.writeInt(0); // meaning that no message is being
											// passed

					} catch (IOException e) {
						e.printStackTrace();
					}
					command = "continue";
				}
				break;
			case "previous":
				if (clientMessages.isEmpty()) {
					try {
						client.writeUTF("You have no messages");
						client.writeInt(0); // meaning that no message is passed
					} catch (IOException e) {
						e.printStackTrace();
					}
					command = "continue";
					break;
				}
				if ((id <= clientMessages.size() - 1) && (id >= 1)) {
					--id;
					Message msg = MessageList.getMessage(clientMessages, id);
					sendMessage("PREVIOUS MESSAGE ---> ", msg);
					command = "continue";
					browsing = true;
				} else {
					try {
						client.writeUTF("There are no previous messages");
						client.writeInt(0); // meaning that no message is passed
					} catch (IOException e) {
						e.printStackTrace();
					}
					command = "continue";
					break;
				}
				break;
			case "next":
				if (clientMessages.isEmpty()) {
					try {
						client.writeUTF("You have no messages");
						client.writeInt(0); // meaning that no message is passed
					} catch (IOException e) {
						e.printStackTrace();
					}
					command = "continue";
					break;
				}
				if ((id >= 0) && (id <= clientMessages.size() - 2)) {
					++id;
					Message msg = MessageList.getMessage(clientMessages, id);
					sendMessage("NEXT MESSAGE ---> ", msg);
					command = "continue";
					browsing = true;
				} else {
					try {
						client.writeUTF("There are no newer messages");
						client.writeInt(0); // meaning that no message is passed
					} catch (IOException e) {
						e.printStackTrace();
					}
					command = "continue";
					break;
				}
				break;
			case "delete":
				if (clientMessages.isEmpty()) {
					try {
						client.writeUTF("You have no messages");
						client.writeInt(0); // meaning that no message is passed
					} catch (IOException e) {
						e.printStackTrace();
					}
					command = "continue";
					break;
				}
				else{
					clientMessages = MessageList.deleteMessage(id, clientMessages);
					idAfterDeletion();
					changeInMessageList = true;
					if (clientMessages.size() > 0){
						Message msg = MessageList.getMessage(clientMessages, id);
						sendMessage("NEXT MESSAGE ---> ", msg);
					}
					else{
						try {
							client.writeUTF("You have no messages");
							client.writeInt(0); // meaning that no message is passed
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					command = "continue";
					browsing = true;
					break;
				}
			}
			
			if (changeInMessageList){
				user.messageList = clientMessages;
				Backup.serialize(backupInformation);
				changeInMessageList = false;
			}
		}
	}

	protected void setCommand(String command) {
		this.command = command;
	}

	private void resetId() {
		id = clientMessages.size() - 1;
	}
	
	private void idAfterDeletion() {
		if (id >= (clientMessages.size()-1)){
			if (clientMessages.size()-1 >= 0) {
				resetId();
			}
		}
	}

	private void sendMessage(String additionalText, Message msg) {
		try {
			// sends clientReceiver the new/previous/next message text and who
			// message was sent from
			client.writeUTF(additionalText + msg.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}

		byte[] encryptedText = msg.getEncryptedText();

		try {
			// sends clientReceiver the length of incoming message
			client.writeInt(encryptedText.length);
			// sends clientReceiver the encrypted message
			client.write(encryptedText);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
