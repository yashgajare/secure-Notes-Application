package com.notes.security.jwt;

import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.notes.security.services.UserDetailsImpl;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class JwtUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(JwtUtil.class);
	
	@Value("${spring.app.jwtSecret}")
	private String jwtSecret;
	
	@Value("${spring.app.jwtExpirationMs}")
	private Integer jwtExpirationMs;
	
	public String getJwtFromHeader(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");
		LOGGER.debug("Authorization Header: {}", bearerToken);
		if(bearerToken!=null && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		return null;
	}
	
	public String generateTokenFromUsername(UserDetailsImpl userDetails) {
		String username = userDetails.getUsername();
		String roles = userDetails.getAuthorities().stream().map(authority -> authority.getAuthority()).collect(Collectors.joining(","));
		return Jwts
				.builder()
				.subject(username)
				.claim("roles", roles)
				.claim("is2faEnabled", userDetails.getIs2faEnabled())
				.issuedAt(new Date())
				.expiration(new Date(new Date().getTime() + jwtExpirationMs))
				.signWith(key())
				.compact();
	}
	
	public String getUsernamefromJwtToken(String token) {
		return Jwts
				.parser()
				.verifyWith((SecretKey) key())
				.build()
				.parseSignedClaims(token)
				.getPayload()
				.getSubject();
	}
	
	private Key key() {
		return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
	}
	
	public boolean validateJwtToken(String token) {
		 try {
	            System.out.println("Validate");
	            Jwts.parser().verifyWith((SecretKey) key()).build().parseSignedClaims(token);
	            return true;
	        } catch (MalformedJwtException e) {
	        	LOGGER.error("Invalid JWT token: {}", e.getMessage());
	        } catch (ExpiredJwtException e) {
	        	LOGGER.error("JWT token is expired: {}", e.getMessage());
	        } catch (UnsupportedJwtException e) {
	        	LOGGER.error("JWT token is unsupported: {}", e.getMessage());
	        } catch (IllegalArgumentException e) {
	        	LOGGER.error("JWT claims string is empty: {}", e.getMessage());
	        }
	        return false;
	}
	
}
