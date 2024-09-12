package dev.nastiausenko.movies.security;

import dev.nastiausenko.movies.jwt.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/V1/login", "/api/V1/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/V1/reviews").hasAnyAuthority("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/V1/reviews/user-reviews").hasAnyAuthority("USER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/V1/reviews/{id}").hasAnyAuthority("USER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/V1/change-username").hasAnyAuthority("USER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/V1/change-password").hasAnyAuthority("USER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/V1/reviews/{id}").hasAnyAuthority("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/V1/admin/**").hasAuthority("ADMIN")
                        .anyRequest().permitAll()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
