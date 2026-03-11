package com.application.stockfela.JWT;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
public class JWTUtilities {

    private static final Logger logger= LoggerFactory.getLogger(JWTUtilities.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpirationMs;

    //Getting the JWT token from HTTP header
    public String getJwtForHeader(HttpServletRequest request){
        String bearerToken = request.getHeader("Authorization");
        logger.debug("Authorization Header: {}",bearerToken);

        if(bearerToken !=null && bearerToken.startsWith("Bearer")){
            return bearerToken.substring(7);
        }
        return null;
    }
    //Generate Token using UserDetails
    public String generateTokenFromUsername(UserDetails userDetails){
        String username = userDetails.getUsername();
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key()).compact();

    }

    public String getUserNameFromJwtToken(String token){

        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();

    }


    private SecretKey key() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }


    public boolean validateJwtToken(String authToken){
        try{
            System.out.println("Validate");
            Jwts.parserBuilder()
                    .setSigningKey((SecretKey) key())
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        }
        catch(MalformedJwtException e){
            logger.error("Invalid JWT token: {}",e.getMessage());

        } catch(ExpiredJwtException e){
            logger.error("JWT token is expired: {}", e.getMessage());

        }catch (UnsupportedJwtException e){
            logger.error("JWT token is unsupported {}",e.getMessage());

        }catch(IllegalArgumentException e){
            logger.error("JWT claims string is empty {}",e.getMessage());

        }
        return false;
    }



}
