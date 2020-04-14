package io.bdemers.example.security.paseto;

import dev.paseto.jpaseto.PasetoParser;
import io.github.jhipster.config.JHipsterProperties;
import dev.paseto.jpaseto.Claims;
import dev.paseto.jpaseto.PasetoException;
import dev.paseto.jpaseto.Pasetos;
import dev.paseto.jpaseto.lang.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.stream.Collectors;

@Component
public class PasetoTokenProvider {

    private final Logger log = LoggerFactory.getLogger(PasetoTokenProvider.class);

    private static final String AUTHORITIES_KEY = "auth";

    private SecretKey key;

    private long tokenValidityInSeconds;

    private long tokenValidityInSecondsForRememberMe;

    private final JHipsterProperties jHipsterProperties;

    private PasetoParser pasetoParser;

    public PasetoTokenProvider(JHipsterProperties jHipsterProperties) {
        this.jHipsterProperties = jHipsterProperties;
    }

    @PostConstruct
    public void init() {
        // TODO the JWT properties are used here as they _mostly_ align with PASETO
        byte[] keyBytes;
        String secret = jHipsterProperties.getSecurity().getAuthentication().getJwt().getSecret();
        if (!StringUtils.isEmpty(secret)) {
            log.warn("Warning: the PASETO key used is not Base64-encoded. " +
                "We recommend using the `jhipster.security.authentication.jwt.base64-secret` key for optimum security.");
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        } else {
            log.debug("Using a Base64-encoded PASETO secret key");
            keyBytes = Base64.getDecoder().decode(jHipsterProperties.getSecurity().getAuthentication().getJwt().getBase64Secret());
        }
        this.key = Keys.secretKey(keyBytes);
        this.tokenValidityInSeconds = jHipsterProperties.getSecurity().getAuthentication().getJwt().getTokenValidityInSeconds();
        this.tokenValidityInSecondsForRememberMe = jHipsterProperties.getSecurity().getAuthentication().getJwt()
                .getTokenValidityInSecondsForRememberMe();

        this.pasetoParser = Pasetos.parserBuilder()
            .setSharedSecret(key)
            .build();
    }

    public String createToken(Authentication authentication, boolean rememberMe) {
        String authorities = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(","));

        Instant now = Instant.now();
        Instant validity;
        if (rememberMe) {
            validity = now.plus(this.tokenValidityInSecondsForRememberMe, ChronoUnit.SECONDS);
        } else {
            validity = now.plus(this.tokenValidityInSeconds, ChronoUnit.SECONDS);
        }

        return Pasetos.V1.LOCAL.builder()
            .setSubject(authentication.getName())
            .claim(AUTHORITIES_KEY, authorities)
            .setSharedSecret(key)
            .setExpiration(validity)
            .compact();
    }

    public Authentication getAuthentication(String token) {

        Claims claims = Pasetos.parserBuilder()
            .setSharedSecret(key)
            .build()
            .parse(token)
            .getClaims();

        Collection<? extends GrantedAuthority> authorities =
            Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        User principal = new User(claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public boolean validateToken(String authToken) {
        try {
            pasetoParser.parse(authToken);
            return true;
        } catch (PasetoException | IllegalArgumentException e) {
            log.info("Invalid Paseto token.");
            log.trace("Invalid Paseto token trace.", e);
        }
        return false;
    }
}
