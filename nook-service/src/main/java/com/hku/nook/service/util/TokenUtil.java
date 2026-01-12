package com.hku.nook.service.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.hku.nook.domain.exception.ConditionException;

import java.util.Calendar;
import java.util.Date;

public class TokenUtil {

    private static final String ISSUER = "hku-renliubo";

    public static String generateToken(Long userId) throws Exception {
        Algorithm algorithm = Algorithm.RSA256(
                RSAUtil.getPublicKey(), RSAUtil.getPrivateKey()
        );
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR, 3);
        return JWT.create().withKeyId(String.valueOf(userId))
                .withIssuer(ISSUER)
                .withExpiresAt(calendar.getTime())
                .sign(algorithm);
    }
    
    public static String generateRefreshToken(Long userId) throws Exception {
        Algorithm algorithm = Algorithm.RSA256(
                RSAUtil.getPublicKey(), RSAUtil.getPrivateKey()
        );
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, 7);
        return JWT.create().withKeyId(String.valueOf(userId))
                .withIssuer(ISSUER)
                .withExpiresAt(calendar.getTime())
                .sign(algorithm);
    }

    public static Long verifyToken(String token) {
        Algorithm algorithm = null;
        try {
            algorithm = Algorithm.RSA256(
                    RSAUtil.getPublicKey(), RSAUtil.getPrivateKey()
            );
        } catch (Exception e) {
            throw new ConditionException("RSAUtil get key failed!");
        }
        String userId = null;
        try {
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT jwt = verifier.verify(token);
            userId = jwt.getKeyId();
        } catch (TokenExpiredException e) {
            throw new ConditionException("555", "Token expired!");
        }catch (Exception e) {
            throw new ConditionException("Illegal user or invalid token!");
        }
        return Long.valueOf(userId);
    }

}
