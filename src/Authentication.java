import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;

public class Authentication extends Thread {

	private Map<String, byte[]> userPasswords;
	private ArrayList<String> listOfUsers;
	private DataInputStream fromClient;
	private DataOutputStream toClient;
	private ClientMessages clientMessages;
	private ArrayList<User> backupInformation;
	private User user;

	Authentication(Map<String, byte[]> userPasswords, ArrayList<String> listOfUsers, DataInputStream fromClient, DataOutputStream toClient, ClientMessages clientMessages, ArrayList<User> backupInformation) {
		this.userPasswords = userPasswords;
		this.listOfUsers = listOfUsers;
		this.fromClient = fromClient;
		this.toClient = toClient;
		this.clientMessages = clientMessages;
		this.backupInformation = backupInformation;
	}

	public void run() {
		
		String clientName = null;
		String clientPassword = null;
		String command = null;
		
		boolean keep_running = true;

		while (keep_running) {
			// We ask the client what its name is:
			try {
				clientName = fromClient.readUTF();
				clientPassword = fromClient.readUTF();
				command = fromClient.readUTF();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 

			if (Command.REGISTER.equals(command)) {
				if (!userExists(clientName, listOfUsers)) {
					 clientMessages.addUser(clientName);
					 listOfUsers.add(clientName);
					 byte [] encryptedPassword = null;
					 try {
					 // saves and encrypted version of the user's password
				     encryptedPassword = Encryption.encrypt(clientPassword.getBytes(StandardCharsets.UTF_8));
					 userPasswords.put(clientName,encryptedPassword);
					 } catch (Exception e) {
					 e.printStackTrace();
					 }
					 try {
						toClient.writeUTF("success");
						toClient.flush();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					 
					System.out.println("Successfully registered " + clientName);
					System.out.flush();
					System.out.println("Logged in as " + clientName);
					System.out.flush();
					
					user = new User(clientName, encryptedPassword, new LinkedList <Message>());
					backupInformation.add(user);
					
					keep_running = false;
				} else {		
					try {
						toClient.writeUTF("A user with this nickname already exists. Please try again.");
						toClient.flush();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} else {
				if (userExists(clientName, listOfUsers)) {
					byte [] encryptedPassword = null;
					try {
						if (clientPassword.equals(new String(Encryption.decrypt(userPasswords.get(clientName))))) {
							toClient.writeUTF("success");
							toClient.flush();
							System.out.println("Logged in as " + clientName);
							encryptedPassword = Encryption.encrypt(clientPassword.getBytes(StandardCharsets.UTF_8));
							keep_running = false;
						} else {
							toClient.writeUTF("Incorrect password.");
							toClient.flush();
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					user = new User(clientName, encryptedPassword, new LinkedList <Message>());
					backupInformation.add(user);
					
				} else {
					try {
						toClient.writeUTF("This user does not exist.");
						toClient.flush();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
		Backup.serialize(backupInformation);
		
		// creates and starts a new ServerSender thread for this user
		ServerSender serverSender = new ServerSender(clientMessages.getAllUserMessages(clientName), toClient, user, backupInformation);
		serverSender.start();

		// creates and starts a new thread ServerReceiver for this user:
		(new ServerReceiver(clientName, fromClient, clientMessages, serverSender)).start();
		
	}

	private static boolean userExists(String username, ArrayList<String> listOfUsers) {
		boolean exists = false;

		if (listOfUsers.contains(username)) {
			exists = true;
		}
		return exists;
	}
}
