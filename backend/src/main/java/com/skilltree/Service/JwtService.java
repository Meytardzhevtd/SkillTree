package com.skilltree.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.skilltree.model.Users;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
	@Value("${jwt.secret}")
	private String jwtSecret;

	@Value("${jwt.expiration-ms}")
	private long jwtExpirationMs;

	public String generateToken(String email, Long userId) {
		Instant now = Instant.now();

		return Jwts.builder().subject(email).claim("userId", userId).issuedAt(Date.from(now))
				.expiration(Date.from(now.plusMillis(jwtExpirationMs))).signWith(getSigningKey())
				.compact();
	}

	public Long extractUserId(String token) {
		return extractAllClaims(token).get("userId", Long.class);
	}

	public String extractEmail(String token) {
		return extractAllClaims(token).getSubject();
	}

	public boolean isTokenValid(String token, String email) {
		try {
			return email.equals(extractEmail(token)) && !isTokenExpired(token);
		} catch (JwtException | IllegalArgumentException ex) {
			return false;
		}
	}

	public boolean isTokenValidWithUserId(String token, String email, Long userId) {
		try {
			return email.equals(extractEmail(token)) && userId.equals(extractUserId(token))
					&& !isTokenExpired(token);
		} catch (JwtException | IllegalArgumentException ex) {
			return false;
		}
	}

	public Optional<Long> extractUserIdFromAuthHeader(String authHeader) {
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return Optional.empty();
		}

		String token = authHeader.substring(7);

		try {
			Claims claims = extractAllClaims(token);

			if (claims.getExpiration().before(new Date())) {
				return Optional.empty();
			}

			return Optional.of(claims.get("userId", Long.class));

		} catch (IllegalArgumentException e) {
			return Optional.empty();
		}
	}

	private boolean isTokenExpired(String token) {
		return extractAllClaims(token).getExpiration().before(new Date());
	}

	private Claims extractAllClaims(String token) {
		try {
			return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token)
					.getPayload();
		} catch (JwtException e) {
			throw new IllegalArgumentException("Неверный JWT токен", e);
		}
	}

	private SecretKey getSigningKey() {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] keyBytes = digest.digest(jwtSecret.getBytes(StandardCharsets.UTF_8));
			return Keys.hmacShaKeyFor(keyBytes);
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("Cannot initialize JWT signing key", ex);
		}
	}
}
