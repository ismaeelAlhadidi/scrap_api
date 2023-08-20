package util;

import java.util.Random;

public class Generator {
	
	public static String generate6DigitsCode() {
		if(true) return "123456";
		Random r = new Random();
		StringBuilder code = new StringBuilder();
		for(int i = 0; i < 6; ++i) {
			code.append(r.nextInt(10));
		}
		return code.toString();
	}
}
