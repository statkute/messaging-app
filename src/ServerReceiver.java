
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;

// Gets messages from client and puts them in a queue, for another
// thread to forward to the appropriate client.

public class ServerReceiver extends Thread {
	private String myClientsName;
	private DataInputStream myClient;
	private ClientMessages clientMessages;
	private ServerSender companion;
	private int id = 0;

	/**
	 * Constructs a new server receiver.
	 * 
	 * @param n
	 *            the name of the client with which this server is communicating
	 * @param c
	 *            the reader with which this receiver will read data
	 * @param t
	 *            the table of known clients and connections
	 * @param s
	 *            the corresponding sender for this receiver
	 */
	public ServerReceiver(String n, DataInputStream c, ClientMessages m, ServerSender s) {
		myClientsName = n;
		myClient = c;
		clientMessages = m;
		companion = s;
	}

	/**
	 * Starts this server receiver.
	 */
	public void run() {
		try {
			while (true) {
				String userInput = myClient.readUTF(); // Matches CCCCC in ClientSender.java
				
				while (userInput.equals(Command.REGISTER) || (userInput.equals(Command.LOGIN))) {
					userInput = myClient.readUTF();
				}

				if (userInput == null || userInput.equals(Command.QUIT)) {
					// Either end of stream reached, just give up, or user wants to quit
					break;
				}
				
				if (userInput.equals(Command.CURRENT)) {
					companion.setCommand(Command.CURRENT);
				};
				
				if (userInput.equals(Command.PREVIOUS)) {
					companion.setCommand(Command.PREVIOUS);
				};
				
				if (userInput.equals(Command.NEXT)) {
					companion.setCommand(Command.NEXT);
				};
				
				if (userInput.equals(Command.DELETE)) {
					companion.setCommand(Command.DELETE);
				};
				
				//meaning if a user is sending a message
				if (!((userInput.equals(Command.CURRENT)) || (userInput.equals(Command.PREVIOUS)) || (userInput.equals(Command.NEXT)) || (userInput.equals(Command.DELETE)))) {
					
					int byteLength = myClient.readInt(); // reads the length of incoming message

					if (byteLength > 0) {
						byte[] messageByte = new byte[byteLength];
						myClient.readFully(messageByte, 0, messageByte.length); // read the encrypted message
						
						Message msg = null;
						try {
							msg = new Message(myClientsName, messageByte);
						} catch (Exception e) {
							Report.behaviour("Failed message encryption");
							e.printStackTrace();
						}
						LinkedList<Message> recipientsMessages = clientMessages.getAllUserMessages(userInput); // Matches EEEEE in ServerSender.java

						if (recipientsMessages != null) {
							recipientsMessages.add(msg);
						} else {
							//Report.error("Message for unexistent client " + userInput + ": " + text);
						}
					} else {
						// No point in closing socket. Just give up.
						return;
					}
				}
				
			}
		} catch (IOException e) {
			Report.error("Something went wrong with the client " + myClientsName + " " + e.getMessage());
			// No point in trying to close sockets. Just give up.
			// We end this thread (we don't do System.exit(1)).
		}
		Report.behaviour("Server receiver ending");
		Report.behaviour("Server sender ending");
		companion.interrupt();
		Report.behaviour("Server sender ended");
		Report.behaviour("Server receiver ended");
	}
}
