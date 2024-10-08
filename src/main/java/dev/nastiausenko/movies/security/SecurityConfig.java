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
                        .requestMatchers(HttpMethod.POST, "/api/V1/user/login", "/api/V1/user/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/V1/user/reviews").hasAnyAuthority("USER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/V1/user/change-username").hasAnyAuthority("USER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/V1/user/change-password").hasAnyAuthority("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/V1/user/categories").hasAnyAuthority("USER", "ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/V1/reviews/user-reviews").hasAnyAuthority("USER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/V1/reviews/{id}").hasAnyAuthority("USER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/V1/reviews/{id}").hasAnyAuthority("USER", "ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/V1/admin/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/V1/admin/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/V1/admin/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/V1/admin/**").hasAuthority("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/V1/categories/**").hasAuthority("USER")
                        .requestMatchers(HttpMethod.DELETE, "/api/V1/categories/**").hasAuthority("USER")
                        .requestMatchers(HttpMethod.PUT, "/api/V1/categories/**").hasAuthority("USER")
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
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
