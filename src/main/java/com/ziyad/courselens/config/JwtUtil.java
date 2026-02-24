package com.ziyad.courselens.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

// tells spring to create instance of this class and use it wherever needed
@Component
public class JwtUtil { // this will be a helper class Utility class we will call it whenever we need it

    @Value("${jwt.secret}") // tells spring to look in application.properties for that value of this String
    private String secret; // or secret key

    @Value("${jwt.expiration}")
    private long expirationMs; // token exp time

    // private helper method to take or string key and turn it into bytes the library can use
    private Key getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes()); //hmacShaKeyFor will create a key object so we use it in other methods
    }

    // to create a token given the email and role
    public String generateToken(String email, String role) {
        return Jwts.builder()  // jwts utill class
                .setSubject(email) // we set the sub to the email
                .claim("role", role) // claim so we add data in the payload
                .setIssuedAt(new Date()) // the time we created this token
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs)) // calculating exp time
                .signWith(getKey(), SignatureAlgorithm.HS256) // creating the token signature
                .compact(); // put everything together and return the JWT String
    }

    // this will only be called after isTokenValid return true means we don't need to catch Exp
    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // read and verify the token
    private Claims getClaims(String token) {
        return Jwts.parserBuilder() // parser to read not build
                .setSigningKey(getKey())// take the key so it can verify the signature to check if someone tamper with it
                .build()// finish setting the parser
                .parseClaimsJws(token) // verify the signature and check if expired if anything wrong throws Exp
                .getBody(); // return the token payload
    }
}
