package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

public class PasswordHandler {
	
	public static boolean validate(String salt, String password, String hashedPassword) throws Exception {
		return generateHash(salt, password).equals(hashedPassword);
	}
	
	public static String generateHash(String salt, String password) throws Exception {
		MessageDigest md = MessageDigest.getInstance("SHA-512");
		md.update(salt.getBytes());
		byte[] hashedPassword = md.digest(password.getBytes(StandardCharsets.UTF_8));
		return new String(hashedPassword);
	}
	
	public static String generateSalt() {
		SecureRandom random = new SecureRandom();
		byte[] salt = new byte[16];
		random.nextBytes(salt);
		return new String(salt);
	}
}
