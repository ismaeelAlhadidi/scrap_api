package util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.JWTCreator.Builder;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;

public class JwtGenerator {
	public static String secret = "secret";
	public static long tokenTimeInSeconds = 60 * 60 * 24 * 60;
	
	public static String generateJwt(HashMap<String, String> payload) throws Exception {
		Builder tokenBuilder = JWT.create()
				.withExpiresAt(Date.from(Instant.now().plusSeconds(tokenTimeInSeconds)))
                .withIssuedAt(Date.from(Instant.now()));
		
		payload.entrySet().forEach(action -> tokenBuilder.withClaim(action.getKey(), action.getValue()));
		
		return tokenBuilder.sign(Algorithm.HMAC512(secret));
	}
}
