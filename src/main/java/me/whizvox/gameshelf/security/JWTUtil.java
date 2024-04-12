package me.whizvox.gameshelf.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Date;

@Component
public class JWTUtil {

  private final SecretKey signingKey;

  public JWTUtil(@Value("${gameshelf.jwt.keyFile:jwt.key}") String keyFile) {
    Path keyPath = Paths.get(keyFile);
    if (!Files.exists(keyPath)) {
      throw new RuntimeException("JWT key file not found: " + keyFile);
    }
    try {
      signingKey = readSecretKey(keyPath);
    } catch (IOException e) {
      throw new RuntimeException("Could not read JWT key file: " + keyPath, e);
    }
  }

  private SecretKey readSecretKey(Path path) throws IOException {
    return Keys.hmacShaKeyFor(Base64.getDecoder().decode(Files.readAllBytes(path)));
  }

  public Claims extractClaims(String token) {
    return Jwts.parser()
        .verifyWith(signingKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public ObjectId extractUserId(Claims claims) {
    return new ObjectId(claims.getSubject());
  }

  public AccessToken generateToken(ObjectId userId, Duration lifespan) {
    AccessToken accessToken = new AccessToken();
    LocalDateTime now = LocalDateTime.now();
    accessToken.issued = now;
    accessToken.expires = now.plus(lifespan);
    accessToken.token = Jwts.builder()
        .subject(userId.toHexString())
        .issuedAt(Date.from(accessToken.issued.toInstant(ZoneOffset.UTC)))
        .expiration(Date.from(accessToken.expires.toInstant(ZoneOffset.UTC)))
        .signWith(signingKey)
        .compact();
    return accessToken;
  }

}
