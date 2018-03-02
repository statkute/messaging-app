
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

// Repeatedly reads recipient's nickname and text from the user in two
// separate lines, sending them to the server (read by ServerReceiver
// thread).

public class ClientSender extends Thread {

	private String username;
	private DataOutputStream server;
	private boolean quit = false;
	private boolean notSending = false;

	ClientSender(String nickname, DataOutputStream server) {
		this.username = nickname;
		this.server = server;
	}

	/**
	 * Start ClientSender thread.
	 */
	public void run() {
		// So that we can use the method readLine:
		BufferedReader user = new BufferedReader(new InputStreamReader(System.in));

		try {
			// Then loop forever sending messages to recipients via the server:
			while (true) {
				String command = user.readLine();
				if (quit == true) {
					server.writeUTF("quit"); // Matches CCCCC in ClientSender.java
					server.flush();
					if (command.equals(Command.LOGOUT)){
						Client.logout = true;
					}
					break;
				}

				if (command.equals(Command.QUIT) || command.equals(Command.LOGOUT)) {
					server.writeUTF(command); // Matches CCCCC in ClientSender.java
					server.flush();
					
					if (command.equals(Command.LOGOUT)){
						Client.logout = true;
					}
					
					break;
				}

				while (!command.equals(Command.SEND)) {
					if (command.equals(Command.CURRENT)) {
						server.writeUTF(command); // Matches CCCCC in ClientSender.java
						server.flush();
						notSending = true;
						break;
					}
					
					else if (command.equals(Command.PREVIOUS)) {
						server.writeUTF(command); // Matches CCCCC in ClientSender.java
						server.flush();
						notSending = true;
						break;
					}
					
					else if (command.equals(Command.NEXT)) {
						server.writeUTF(command); // Matches CCCCC in ClientSender.java
						server.flush();
						notSending = true;
						break;
					}
					
					else if (command.equals(Command.DELETE)) {
						server.writeUTF(command); // Matches CCCCC in ClientSender.java
						server.flush();
						notSending = true;
						break;
					}
					
					else{	
						if (command.equals(Command.QUIT) || command.equals(Command.LOGOUT)) {
							quit = true;
							notSending = true;
							break;
						}
						
						System.out.println("The command " + command
								+ " does not exist. Type send to send a message followed by recipient and text.");

						command = user.readLine();

						if (command.equals(Command.QUIT) || command.equals(Command.LOGOUT) ) {
							quit = true;
							break;
						}
					}
				}
				
				if (!notSending) {
					
					String recipient = user.readLine();
					String text = user.readLine();
					byte[] encryptedText = null;
					try {
						encryptedText = Encryption.encrypt(text.getBytes(StandardCharsets.UTF_8));
					} catch (Exception e) {
						e.printStackTrace();
					}
					server.writeUTF(recipient); // Matches CCCCC in ServerReceiver 
					server.writeInt(encryptedText.length); // write length of the message
					server.write(encryptedText); // Matches DDDDD in ServerReceiver
					
					System.out.println("");
				}
				
				notSending = false;
			}
		} catch (IOException e) {
			Report.errorAndGiveUp("Communication broke in ClientSender" + e.getMessage());
		}

		Report.behaviour("Client sender thread ending"); // Matches GGGGG in Client.java
	}
}

/*
 * 
 * What happens if recipient is null? Then, according to the Java documentation,
 * println will send the string "null" (not the same as null!). So maye we
 * should check for that case! Paticularly in extensions of this system.
 * 
 */
