# Explanation of "Messaging system"

#### Registration functionality (user input = 'register')

Client.java asks for a username and password. Then the Server creates a new ```Authentication``` thread that is responsible for user login and registration. A new thread for authentication is used to enable simultaneous logins / registrations by many users.

In the ```Authentication``` class, two criteria are checked:
  - A user with this username has not been registered before.
  The username is checked against a list of all registered usernames. If no such username has been registered it writes this username to the list.
  - A password passes the following criteria: includes at least one of each: upper-case, lowercase letters, digit, special character and is at least 7 symbols long.

If both criteria are passed, it automatically registers the user and logs him/her in (two server threads are started in ```Server```, two client threads are started in ```Client```).

#### Login (user input = 'login')

Client.java asks for a username and password. Then writes it to the Server where two criteria are checked:
  - A user with this username has been registered before.
  The username is checked against a list of all registered usernames. If such username has been registered already continues, if not, the user is asked to login or register again.
  - If a user with the given username exists, checks whether the user has entered the correct password.

If both criteria are passed user is logged in (two server threads are started in ```Server```, two client threads are started in ```Client```).

The application allows simultaneous logins by the same account on different devices. This is because this functionality is a must for any messaging system as many users like to receive and send messages simultaneously on multiple devices, for instance: phone and computer, tablet and phone and etc.

#### Logout (user input = 'logout')

In most applications, when a user decides to log out, he/she is given an option to log-in or register again - the app does not close itself on logout. That happens with the logout function in my messaging app as well ('quit' functionality actually quits the app for that user).

To achieve that, when a user wants to logout, all four threads (```ServerSender```, ```ServerReceiver```, ```ClientSender```, ```ClientReciever```) are stopped, however, ```Client``` keeps running as a while loop inside of it keeps running. The user is asked to login or register again.
This is achieved using a variable in ```Client```:
```Java
public static boolean logout = false;
```
This username is set to true when the user wants to logout (in ClientSender.java): 
```Java
Client.logout = true;
```
When this happens, all threads, sockets and input/output streams are killed, but the while loop inside keeps running and new socket and input/output streams are created (unlike with 'quit').
```Java
while (run) {
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
            ...
```


#### Storing messages even if a user is offline

The messages are stored in the server even if the user is currently offline and in case someone sends a new message to that user in the meantime, new messages are added together with the old ones.
All messages are stored in ```ClientMessages```:
```Java
private HashMap<String, LinkedList<Message>> allUsersWithMessages = new HashMap<String, LinkedList<Message>>();
```
The keys are different usernames and the values are linked list of all the messages that user has. Even when the user is offline, a method for adding new messages in ClientMessages.java is called:
```Java
public void addMessage(String nickname, Message message) {
        LinkedList<Message> listOfMessages = getAllUserMessages(nickname);
        listOfMessages.add(message);

        allUsersWithMessages.replace(nickname, listOfMessages);
    }
```
When a user logs back in, he/her sees the current message (the one that was last added to the linked list) using a method in ```ClientMessages``` to get all messages:
```Java
public static void remove(String nickname) {
    public LinkedList<Message> getAllUserMessages(String nickname) {
        return allUsersWithMessages.get(nickname);
    }
}
```
and then method in ```MessageList``` to get the current message:
```Java
public static Message getCurrentMessage(LinkedList <Message> messagesList) {
        return messagesList.get(messagesList.size() - 1);
    }
```
#### New syntax and semantics to send (encrypted) messages

At first, in ```ClientSender``` the first user line is read. If it says "send", two more lines are read - first of which for the recipient and second for the message text.
```Java
String command = user.readLine();

...

String recipient = user.readLine();
String text = user.readLine();
byte[] encryptedText = null;
try {
        // encryption is explained later!
        encryptedText = Encryption.encrypt(text.getBytes(StandardCharsets.UTF_8));
} catch (Exception e) {
    e.printStackTrace();
}
server.writeUTF(recipient); // Matches CCCCC in ServerReceiver 
server.writeInt(encryptedText.length); // write length of the message
server.write(encryptedText); // Matches DDDDD in ServerReceiver
```

All this data is received by the ```ServerReceiver```, passed along to ```ServerSender``` and then to ```ClientReceiver```.

#### Previous, next, current

```ServerSender``` receives the command. And gets the according message based on ID (if the command is not 'current', if it is 'current' it calls the previously seen method ```getCurrentMessage(LinkedList <Message> messagesList).```

```Java
public static Message getMessage(LinkedList <Message> messagesList, int id) {
        return messagesList.get(id);
    }
```
ID by default is the index of the last item in the linked list of that user messages. It is increased and decreased accordingly based on the commands. If the command is 'current', then the ID is reset again.

If there is no previous/current/next message, the user is informed that he/she does not have any previous messages / any messages / any newer messages.

#### Delete

```ServerSender``` receives the command. It deletes the current message (the message the user has last viewed) using a method in ```MessageList.java```:
```Java
public static LinkedList<Message> deleteMessage(int id, LinkedList<Message> allMessages) {
        allMessages.remove(id);
        LinkedList<Message> allUsersWithMessages = allMessages;
        
        return allUsersWithMessages;
    }
```
If the user has no messages, it does not do anything but warn the user that this is the case and there is nothing that could be deleted.

After a message is deleted, user is shown the message that was received after the one that was just deleted. If there are no such messages, it shows the latest one left. If there are no messages left after deletion, it says that to the user.

#### Password and Message encryption

```Java
public class Encryption {
    
    // encryption and decryption code found here: https://stackoverflow.com/a/29419826
    
    private final static byte[] KEY = "MZygpewJsCpRrfOr".getBytes(StandardCharsets.UTF_8);
    private static final String ALGORITHM = "AES";

    public static byte[] encrypt(byte[] password) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(KEY, ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        return (cipher.doFinal(password));
    }

    public static byte[] decrypt(byte[] cipherText) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(KEY, ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        return cipher.doFinal(cipherText);
    }
```

Message Encryption happens in ClientSender class so it would not ever reach the server (to keep the user information secure). Decryption happens in ClientReceiver class so it would not ever reach the server also. Server only gets the encrypted byte [] arrays of the passwords to make it more secure as the original password version never leaves the user.

When a user registers, the password is encrypted and saved in```public static Map<String, byte[]> userPasswords = new HashMap<String, byte[]>();``` so that the Server could check if it is correct. On login, the server checks if encrypted version of the stored password is the same as the one that was just entered. 

Because the encryption uses ```byte []``` arrays, I used DataInputStream and DataOutputStream instead off PrintStream and BufferedReader (so it would be easier to pass ```byte[]``` arrays between threads).

#### Information backup

Using serialization and deserialization all of the user information (usernames, passwords and all messages) are saved in a backup.ser file. All passwords and messages in those files are encrypted to increase the security of the information.

When a new user registers, a ```User``` object is created and it stores the previously mentioned user information that is being stored in a .ser backup file. All of the ```User``` objects are stored together in a ```ArrayList <User>```. These two variables are passed into the ```ServerSender``` class. After the message list (of all of that user's messages) is changed, inside of ```ServerSender``` is changed, a new .ser file is serialized with the new information. Therefore, it the backup file always contains the latest information. The method for that is in class ```Backup```:

```Java
public static void serialize(ArrayList<User> backupInformation) {
        try {
            FileOutputStream fileOut = new FileOutputStream("backup.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(backupInformation);
            out.close();
            fileOut.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
```

The ```Messsage``` class and the ```User``` class implement ```Serializable``` as there stored information is being serialized and later deserialized.

After the server is interrupted / stopped or crashes, that backup file has the information about all users.  When the server is started up again, it checks for a backup file. If one exists, it restores that old information (adds users to the user list, adds users and their passwords to the hashmap, client table is updated with every user's messages):

```Java
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
```

The deserialize method is found in the ```Backup``` class:

```Java
public static ArrayList<User> deserialize() {
        ArrayList<User> backupInformation = null;
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream("backup.ser"));
            backupInformation = (ArrayList<User>) in.readObject();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return backupInformation;
    }
```

In order to test the app functionality without the backup, delete the backup.ser file found in src folder after each time the server is shut down.
