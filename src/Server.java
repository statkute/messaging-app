import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Server {
	/**
	 * Start the server listening for connections.
	 */
	public static void main(String[] args) {

		ArrayList<String> listOfUsers = new ArrayList<String>();
		Map<String, byte[]> userPasswords = new HashMap<String, byte[]>();
		ArrayList<User> backupInformation = new ArrayList <User>(); 

		// This table will be shared by the server threads:
		ClientMessages clientMessages = new ClientMessages();
		ServerSocket serverSocket = null;

		try {
			serverSocket = new ServerSocket(Port.number);
		} catch (IOException e) {
			Report.errorAndGiveUp("Couldn't listen on port " + Port.number);
		}

		//checks if a backup file exists, if so, gets the backup'ed information
		if(new File("backup.ser").isFile()) { 
			ArrayList<User> oldBackup = Backup.deserialize();
		    for (User u : oldBackup){
		    	listOfUsers.add(u.username);
		    	userPasswords.put(u.username, u.password);
		    	clientMessages.addUser(u.username);
		    	for (Message m : u.messageList){
		    		clientMessages.addMessage(u.username, m);
		    	}
		    }
		}

		try {
			// Loops for ever, as servers usually do, so it could accept new clients.
			while (true) {
				// Listen to the socket, accepting connections from new clients:
				Socket socket = serverSocket.accept();
				DataInputStream fromClient = new DataInputStream(socket.getInputStream());
				DataOutputStream toClient = new DataOutputStream(socket.getOutputStream());

				(new Authentication(userPasswords, listOfUsers, fromClient, toClient, clientMessages, backupInformation)).start();
				
			}
		} catch (IOException e) {
			Report.errorAndGiveUp("Couldn't listen on port " + Port.number);
		}
	}
}
