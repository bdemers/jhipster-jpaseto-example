package io.bdemers.example.security.paseto;

import dev.paseto.jpaseto.Pasetos;
import dev.paseto.jpaseto.Version;
import dev.paseto.jpaseto.lang.Keys;
import io.bdemers.example.security.AuthoritiesConstants;
import io.github.jhipster.config.JHipsterProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

public class PasetoTokenProviderTest {

    private static final long ONE_MINUTE = 60;

    private SecretKey key;
    private PasetoTokenProvider tokenProvider;

    @BeforeEach
    public void setup() {
        JHipsterProperties jHipsterProperties = new JHipsterProperties();
        jHipsterProperties.getSecurity().getAuthentication().getJwt().setBase64Secret("Sc8tG9pTuHvlk08KjYAYBJ4PphQXhfLKG9QUfr+Kess=");
        jHipsterProperties.getSecurity().getAuthentication().getJwt().setTokenValidityInSeconds(ONE_MINUTE);
        tokenProvider = new PasetoTokenProvider(jHipsterProperties);
        tokenProvider.init();
    }

    @Test
    public void testReturnFalseWhenPasetoHasInvalidSignature() {
        boolean isTokenValid = tokenProvider.validateToken(createTokenWithDifferentSignature());

        assertThat(isTokenValid).isEqualTo(false);
    }

    @Test
    public void testReturnFalseWhenPasetoIsMalformed() {
        Authentication authentication = createAuthentication();
        String token = tokenProvider.createToken(authentication, false);
        String invalidToken = token.substring(1);
        boolean isTokenValid = tokenProvider.validateToken(invalidToken);

        assertThat(isTokenValid).isEqualTo(false);
    }

    @Test
    public void testReturnFalseWhenPasetoIsExpired() {
        ReflectionTestUtils.setField(tokenProvider, "tokenValidityInSeconds", -ONE_MINUTE);

        Authentication authentication = createAuthentication();
        String token = tokenProvider.createToken(authentication, false);

        boolean isTokenValid = tokenProvider.validateToken(token);

        assertThat(isTokenValid).isEqualTo(false);
    }

    @Test
    public void testReturnFalseWhenPasetoIsUnsupported() {
        String unsupportedToken = createUnsupportedToken();

        boolean isTokenValid = tokenProvider.validateToken(unsupportedToken);

        assertThat(isTokenValid).isEqualTo(false);
    }

    @Test
    public void testReturnFalseWhenPasetoIsInvalid() {
        boolean isTokenValid = tokenProvider.validateToken("");

        assertThat(isTokenValid).isEqualTo(false);
    }

    private Authentication createAuthentication() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(AuthoritiesConstants.ANONYMOUS));
        return new UsernamePasswordAuthenticationToken("anonymous", "anonymous", authorities);
    }

    private String createUnsupportedToken() {
        KeyPair keyPair = Keys.keyPairFor(Version.V1);
        return Pasetos.V1.PUBLIC.builder() // public keys are not encrypted
            .setSubject("someone")
            .setPrivateKey(keyPair.getPrivate())
            .compact();
    }

    private String createTokenWithDifferentSignature() {
        SecretKey otherKey = Keys.secretKey(Base64.getDecoder()
            .decode("Q4D0XrdE9mIIYPtW45x7l/JZxQJ+HZ8pzk2K4xqYQXw="));

        return Pasetos.V1.LOCAL.builder()
            .setSubject("anonymous")
            .setSharedSecret(otherKey)
            .setExpiration(Instant.now().plus(1, ChronoUnit.MINUTES))
            .compact();
    }
}
