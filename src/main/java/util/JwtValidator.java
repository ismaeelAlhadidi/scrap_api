package util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

public class JwtValidator {
	
	public static DecodedJWT validate(String token) {
		try {
                        final DecodedJWT jwt = JWT.decode(token);

                        Algorithm algorithm = Algorithm.HMAC512(JwtGenerator.secret);
                        JWTVerifier verifier = JWT.require(algorithm)
                                .build();

                        verifier.verify(token);
                        
                        return jwt;
                } catch (Exception e) {
                        return null;
                }
	}
}
