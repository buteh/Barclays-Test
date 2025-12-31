package com.eaglebank.user.api.auth.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;


@Service
public class JwtService
{
	private final Key key;
	private final long ttlSeconds;

	public JwtService(@Value("${security.jwt.secret}") String secret, @Value("${security.jwt.ttl-seconds}") long ttlSeconds)
	{
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.ttlSeconds = ttlSeconds;
	}

	public String issueToken(String userId)
	{
		Instant now = Instant.now();
		return Jwts.builder().setSubject(userId).setIssuedAt(Date.from(now)).setExpiration(Date.from(now.plusSeconds(ttlSeconds)))
				.signWith(key, SignatureAlgorithm.HS256).compact();
	}

	public String parseUserId(String token)
	{
		return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
	}

}
