package util;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.util.UUID;
import java.util.Random;
import java.io.FileOutputStream;
import java.util.Base64;
import java.util.List;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;

public class ImageHandler {
	public String Dir = "storage/d831";
	public Random r;
	
	public ImageHandler() {
		r = new Random();
	}
	
	public String store(String storage, String src, String type) throws Exception {
		String location = generateUniqueFileLocation() + "." + type;
		FileOutputStream imgOutFile = null;
		try {
			String tempSrc = src.split(",")[1];
			byte[] imgByteArray = Base64.getDecoder().decode(tempSrc);
			imgOutFile = new FileOutputStream(storage + "/" + location);
			imgOutFile.write(imgByteArray);
			imgOutFile.close();
		} catch(Exception e) {
			if(imgOutFile != null) {
				try {
					imgOutFile.close();
				} catch(Exception ex) {}
			}
			throw e;
		}
		return location;
	}
	
	public String store(String storage, String src) throws Exception {
		String type = src.split(";")[0].split("/")[1];
		return store(storage, src, type);
	}
	
	public static String getFile(String location) throws Exception {
		String result = "";
		try {
			FileReader fileReader = new FileReader(new File(location));
		    int character;
		    StringBuilder file = new StringBuilder();
	        while ((character = fileReader.read()) != -1) {
	        	file.append((char)(character));
	        }
	        result = new String(file);
		} catch(Exception e) {
			throw e;
		}
		return result;
	}
	
	public static int writeImageToOutputSteam(String location, OutputStream out) throws Exception {
		try {
			File file = new File(location);
			FileInputStream in = new FileInputStream(file);
		    byte[] buf = new byte[1024];
		    int count = 0;
		    while ((count = in.read(buf)) >= 0) {
		    	out.write(buf, 0, count);
		    }
		    return (int)file.length();
		} catch(Exception e) {
			throw e;
		}
	}
	
	public String getDataURL(String storage, String location) throws Exception {
		String result = "";
		try {
			FileReader fileReader = new FileReader(new File(storage + "/" + location));
		    int character;
		    StringBuilder imageString = new StringBuilder();
	        while ((character = fileReader.read()) != -1) {
	        	imageString.append(character);
	        }
	        String image = Base64.getEncoder().withoutPadding().encodeToString(new String(imageString).getBytes());
	        StringBuilder dataUrl = new StringBuilder();
	        dataUrl.append("data:image/webp;base64,");
	        dataUrl.append(image);
	        result = new String(dataUrl);
		} catch(Exception e) {
			throw e;
		}
		return result;
	}
	
	public boolean delete(List<String> files) {
		boolean allDeleted = true;
		for(String file : files) {
			allDeleted = allDeleted && delete(file);
		}
		return allDeleted;
	}
	
	public boolean delete(String file) {
		boolean result = false;
		try {
			result = (new File(file)).delete();
		} catch(Exception e) {}
		return result;
	}
	
	public InputStream convertToInputStream(String data) {
		return (InputStream)new ByteArrayInputStream(data.getBytes());
	}
	
	public String generateUniqueFileLocation() {
		String random32String = UUID.randomUUID().toString();
		random32String = replaceAllWithRandom(random32String, '_');
		random32String = replaceAllWithRandom(random32String, '-');
		long time = System.currentTimeMillis();
		StringBuilder result = new StringBuilder();
		int pointer = 0;
		int digit;
		int randomNumber;
		while(time > 0) {
			digit = (int)(time%10);
			if(pointer >= random32String.length()) {
				result.append(digit);
				time /= (long)10;
			} else {
				randomNumber = r.nextInt(2);
				if(randomNumber == 0) {
					result.append(notDigit(random32String.charAt(pointer++)));
				} else {
					result.append(digit);
					time /= (long)10;
				}
			}
		}
		while(pointer < random32String.length()) {
			result.append(notDigit(random32String.charAt(pointer++)));
		}
		return Dir + "/" + (new String(result));
	}
	public String replaceAllWithRandom(String temp, char character) {
		char[] s = temp.toCharArray();
		for(int i = 0; i < s.length; ++i) {
			if(s[i] == character) {
				s[i] = (char)(r.nextInt(26)+'a');
			}
		}
		return new String(s);
	}
	public char notDigit(char character) {
		if((character-'0') >= 0 && (character-'0') <= 9) {
			return (char)((character-'0')+'a');
		}
		return character;
	}
}
