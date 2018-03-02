import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Encryption {
	
	// encryption and decryption code found here:
	// https://stackoverflow.com/a/29419826
	
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
}
