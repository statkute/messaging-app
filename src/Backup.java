import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;

public class Backup {
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
}
