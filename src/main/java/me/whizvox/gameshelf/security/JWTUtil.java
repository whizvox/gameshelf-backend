package me.whizvox.gameshelf.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.List;

@Component
public class JWTUtil {

  private static final Logger LOG = LoggerFactory.getLogger(JWTUtil.class);

  private final SecretKey signingKey;

  public JWTUtil(@Value("${gameshelf.jwt.keyFile:jwt.key}") String keyFile,
                 @Value("${gameshelf.jwt.generateKeyFile:false}") boolean generateKeyFile) {
    Path keyPath = Paths.get(keyFile);
    if (!Files.exists(keyPath)) {
      if (!generateKeyFile) {
        throw new RuntimeException("JWT key file not found: " + keyFile);
      }
      signingKey = Jwts.SIG.HS512.key().build(); // HMAC SHA-512
      try {
        Files.write(keyPath, List.of(Base64.getEncoder().encodeToString(signingKey.getEncoded())));
      } catch (IOException e) {
        //noinspection StringConcatenationArgumentToLogCall
        LOG.warn("Could not write secret key to :" + keyPath, e);
      }
    } else {
      try {
        signingKey = Keys.hmacShaKeyFor(Files.readAllBytes(keyPath));
      } catch (IOException e) {
        throw new RuntimeException("Could not read JWT key file: " + keyPath, e);
      }
    }
  }

  public Claims extractClaims(String token) {
    return Jwts.parser()
        .verifyWith(signingKey)
        .clock(() -> Date.from(LocalDateTime.now().toInstant(ZoneOffset.UTC)))
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
