import java.io.Serializable;

public class Message implements Serializable{
	
	private static final long serialVersionUID = 42L;
	private final String sender;
	private final byte[] encryptedText;

	Message(String sender, byte[] encryptedText) {
		this.sender = sender;
		this.encryptedText = encryptedText;
	}

	public String getSender() {
		return sender;
	}

	public byte[] getEncryptedText() {
		return encryptedText;
	}

	public String toString() {
		return "From " + sender + ": ";
	}
}
