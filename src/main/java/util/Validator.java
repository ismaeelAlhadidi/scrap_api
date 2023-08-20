package util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validator {
	public static boolean isValidPhone(String phone) {
		char[] s = phone.toCharArray();
		for(int i = 0; i < s.length; ++i) {
			if((int)s[i] < (int)'0' || (int)s[i] > (int)'9') {
				return false;
			}
		}
		return true;
	}
	
	public static boolean isValidMail(String email) {
		String regex = "^(.+)@(.+)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
		return matcher.matches();
	}
	
	public static boolean isDate(String test) {
		return true;
	}
}
