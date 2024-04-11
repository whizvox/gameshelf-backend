package me.whizvox.gameshelf.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    byte[] key = Base64.getDecoder().decode(Files.readAllBytes(path));
    return new SecretKeySpec(key, "AES");
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

  public String generateToken(ObjectId userId) {
    LocalDateTime now = LocalDateTime.now();
    return Jwts.builder()
        .subject(userId.toHexString())
        .issuedAt(Date.from(now.toInstant(ZoneOffset.UTC)))
        .expiration(Date.from(now.plusDays(7).toInstant(ZoneOffset.UTC)))
        .signWith(signingKey)
        .compact();
  }

}
