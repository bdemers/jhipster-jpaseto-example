package io.bdemers.example.security.paseto;

import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class PasetoConfigurer extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    private final PasetoTokenProvider pasetoTokenProvider;

    public PasetoConfigurer(PasetoTokenProvider pasetoTokenProvider) {
        this.pasetoTokenProvider = pasetoTokenProvider;
    }

    @Override
    public void configure(HttpSecurity http) {
        PasetoFilter customFilter = new PasetoFilter(pasetoTokenProvider);
        http.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
