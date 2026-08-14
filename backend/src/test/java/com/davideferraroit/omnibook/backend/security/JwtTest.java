package com.davideferraroit.omnibook.backend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import java.util.Date;
import java.security.Key;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class JwtTest {

    @Test
    void testTokenGeneration() {
        String secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
        Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        String token = Jwts.builder()
                .subject("admin@omnibook.it")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 24))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
        assertNotNull(token);
    }
}
