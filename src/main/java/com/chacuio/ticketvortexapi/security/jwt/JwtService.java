package com.chacuio.ticketvortexapi.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtProperties jwtProperties;

    @SuppressWarnings("java:S2143") // JWT requires java.util.date
    public String generateToken(Map<String, Object> claims, String subject) {
        Instant now = Instant.now();

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(
                        now.plusMillis(jwtProperties.expiration())
                ))
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = Map.of("authorities", userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList()
        );
        return generateToken(claims, userDetails.getUsername());
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.secret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    private <T> T getClaim(String token, Function<Claims, T> claimsMapper) {
        Claims allClaims = getAllClaimsFromToken(token);
        return claimsMapper.apply(allClaims);
    }

    public String getUsername(String token) {
        return getClaim(token, Claims::getSubject);
    }

    public List<String> getAuthorities(String token) {
        return getClaim(token, claims -> claims.get("authorities", List.class));
    }

    public Date getExpirationDate(String token) {
        return getClaim(token, Claims::getExpiration);
    }

    public boolean isTokenExpired(String token) {
        return getExpirationDate(token).before(new Date());
    }

    public boolean canBeTokenRenewed(String token) {
        return getExpirationDate(token).before(
                new Date(System.currentTimeMillis() + jwtProperties.renewTime())
        );
    }

    public String renewToken(String token, UserDetails userDetails) {
        if (!canBeTokenRenewed(token)) {
            throw new RuntimeException("Token cannot be renewed");
        }
        return generateToken(userDetails);
    }

    public boolean isValidToken(String token, UserDetails userDetails) {
        String username = getUsername(token);
        return username.equals(userDetails.getUsername());
    }
}
