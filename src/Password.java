import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Password {
	private static final String PATTERN = "^(?=.*?[a-z])(?=.*?[A-Z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-+]).{7,}$";

	public static boolean validate(String password) {
		Pattern pattern = Pattern.compile(PATTERN);
		Matcher matcher = pattern.matcher(password);
		
		return matcher.matches();
	}
	
	public static void addPassword (String username, String password, Map <String, String> userPasswords){
		userPasswords.put (username, password);
	}
	
	public static String getPassword (String username, Map <String, String> userPasswords){
		return userPasswords.get(username);
	}
	
	public static boolean userExists (String username, Map <String, String> userPasswords){
		return userPasswords.containsKey(username);
	}
	
}
