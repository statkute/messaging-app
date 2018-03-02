
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.SocketException;

// Gets messages from other clients via the server (by the
// ServerSender thread).

public class ClientReceiver extends Thread {

  private DataInputStream server;

  ClientReceiver(DataInputStream fromServer) {
    this.server = fromServer;
  }

  /**
   * Run the client receiver thread.
   */
  public void run() {

    try {
      while (true) {
        String s = server.readUTF(); // reads addition text ServerSender.java
        int length = server.readInt();
        byte[] encryptedMessage = null;
        
        if(length>0) {
            encryptedMessage = new byte[length]; // read length of incoming message
            server.readFully(encryptedMessage, 0, encryptedMessage.length); // reads the encrypted message
            byte[] decryptedMessage = null;
            try {
				decryptedMessage = Encryption.decrypt(encryptedMessage);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            if (s == null) {
                throw new NullPointerException();
              }
            System.out.println(s + (new String (decryptedMessage)));
            System.out.flush();
        }
        // prints the message given in case no message is being passed
        else if (length == 0){
        	System.out.println(s);
        	System.out.flush();
        }

      }
    } catch (SocketException e) { // Matches HHHHH in Client.java
      Report.behaviour("Client receiver ending");
    } catch (NullPointerException | IOException e) {
      Report.errorAndGiveUp("Server seems to have died "
              + (e.getMessage() == null ? "" : e.getMessage()));
    }
  }
}


/*

 * The method readLine returns null at the end of the stream

 * It may throw IoException if an I/O error occurs

 * See https://docs.oracle.com/javase/8/docs/api/java/io/BufferedReader.html#readLine--


 */
