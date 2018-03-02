
// Usage:
//        java Client user-username server-hostname
//
// After initializing and opening appropriate sockets, we start two
// client threads, one to send messages, and another one to get
// messages.
//
// A limitation of our implementation is that there is no provision
// for a client to end after we start it. However, we implemented
// things so that pressing ctrl-c will cause the client to end
// gracefully without causing the server to fail.
//
// Another limitation is that there is no provision to terminate when
// the server dies.

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

class Client {

	private static boolean run;
	public static boolean logout = false;

	public static void main(String[] args) {

		// Check correct usage:
		if (args.length != 1) {
			Report.errorAndGiveUp("Usage: java Client server-hostname");
		}

		// Initialize information:
		String hostname = args[0];

		// Open sockets:
		DataOutputStream toServer = null;
		DataInputStream fromServer = null;
		Socket server = null;

		try {
			server = new Socket(hostname, Port.number); // Matches AAAAA in
														// Server.java
			toServer = new DataOutputStream(server.getOutputStream());
			fromServer = new DataInputStream(server.getInputStream());
		} catch (UnknownHostException e) {
			Report.errorAndGiveUp("Unknown host: " + hostname);
		} catch (IOException e) {
			Report.errorAndGiveUp("The server doesn't seem to be running " + e.getMessage());
		}

		BufferedReader fromUser = null;
		fromUser = new BufferedReader(new InputStreamReader(System.in));

		run = true;

		while (run) {
			// if a user has just logged out, creates a new socket and data
			// input and output streams
			if (logout) {
				try {
					server = new Socket(hostname, Port.number);
					toServer = new DataOutputStream(server.getOutputStream());
					fromServer = new DataInputStream(server.getInputStream());
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				logout = false;
			}

			System.out.println("Login or register a new user");

			String command = null;
			try {
				// reads command (login/register)
				command = fromUser.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (Command.LOGIN.equals(command)) {
				String username = null;
				String password = null;
				try {
					System.out.print("username: ");
					username = fromUser.readLine(); // reads the username
					System.out.print("password: ");
					password = fromUser.readLine(); // reads the password
				} catch (IOException e) {
					e.printStackTrace();
				}
				startThreads(command, username, password, toServer, fromServer, server);

			} else if (Command.REGISTER.equals(command)) {
				String username = null;
				String password = null;
				try {

					System.out.print("username: "); // reads the username
					username = fromUser.readLine();

					// does not allow to use words 'register' or 'login' as a
					// username to avoid confusion
					while (username.equals("register") || username.equals("login")) {
						System.out
								.println("Do not use " + username + " as your username. Please type a new username: ");
						System.out.print("username: ");
						username = fromUser.readLine();
					}

					System.out.print("password: ");
					password = fromUser.readLine(); // reads the password

					// makes sure that the password includes at least one of
					// each: upper-case, lowercase letters, digit, special
					// character and is at least 7 symbols long
					while (!Password.validate(password)) {
						System.out.println(
								"The password has to include at least one of each: upper-case, lowercase letters, digit, special character. The password must be at least 7 symbols long");
						System.out.print("password: ");
						password = fromUser.readLine();
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
				startThreads(command, username, password, toServer, fromServer, server);
			} else {
				System.out.println("Please login or register before sending any messages. Please try again.");
			}

		}

	}

	private static void startThreads(String command, String username, String password, DataOutputStream toServer,
			DataInputStream fromServer, Socket server) {

		boolean stop = false;

		try {
			toServer.writeUTF(username); // sends username to the server
			toServer.flush();

			toServer.writeUTF(password); // sends password to the server
			toServer.flush();

			toServer.writeUTF(command); // sends command (login / register) to the server
			toServer.flush();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		try {
			String response = fromServer.readUTF(); // reads server's response to the given data
			if (!response.equals("success")) { // meaning that command was executed successfully
				stop = true;
				System.out.println(response);
			} else {
				if (Command.REGISTER.equals(command)) {
					System.out.println("Successfuly registered a new user. Now logged in as " + username);
				} else {
					System.out.println("Successfuly logged in as " + username);
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		if (!stop) {
			ClientSender sender = new ClientSender(username, toServer); //starts ClientSender thread of this user
			ClientReceiver receiver = new ClientReceiver(fromServer); //starts ClientSender thread of this user

			sender.start();
			receiver.start();
			try {
				toServer.writeUTF(command);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			try {
				sender.join(); // Waits for ClientSender.java to end.
				Report.behaviour("Client sender ended");
				toServer.close(); // Will trigger SocketException
				fromServer.close();
				server.close();
				receiver.join();
				Report.behaviour("Client receiver ended");
			} catch (IOException e) {
				Report.errorAndGiveUp("Something wrong " + e.getMessage());
			} catch (InterruptedException e) {
				Report.errorAndGiveUp("Unexpected interruption " + e.getMessage());
			}

			// does not stop the while loop if a user has logged out
			if (!logout) { 
				run = false;
				Report.behaviour("Client ended. Goodbye.");
			}
		}
	}
}
