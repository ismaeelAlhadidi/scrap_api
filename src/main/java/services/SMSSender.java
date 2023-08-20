package services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class SMSSender {
	
    private static final String ACCOUNT_SID = "GJsadn823k12akdvplq2k3j92k3jk23kasdofl";
    private static final String AUTH_TOKEN = "14ksadn123k12312k3j12k0jk42623kasdofl";
    private static final String ourPhoneNumber = "+15007750104";

    public static String send(String to, String messageContent) {
    	System.out.println("FROM: " + ourPhoneNumber);
    	System.out.println("TO: " + to);
    	System.out.println("Message: " + messageContent);
    	if(true) return "";
    	Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        Message message = Message.creator(
                new PhoneNumber(ourPhoneNumber),
                new PhoneNumber(to),
                messageContent)
            .create();
        System.out.println(message.getSid());
        return message.getSid();
    }
}
