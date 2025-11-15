package com.Smtd.GestionPerteDoc.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.Smtd.GestionPerteDoc.security.jwt.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConf {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(authz -> authz
                // Routes publiques
                .requestMatchers(HttpMethod.PATCH, "/api/declarations/*/restaurer").hasAnyRole("ADMIN", "SUPERVISEUR", "AGENT")
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/roles/**").permitAll()
                .requestMatchers("/api/postes-police/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/api/declarations/actives/poste").hasAnyRole("ADMIN","SUPERVISEUR")
                .requestMatchers("/api/declarations/rechercher-declarant").hasAnyRole("ADMIN","SUPERVISEUR", "AGENT")
                .requestMatchers("/api/declarations/**").authenticated()
                .requestMatchers("/api/utilisateurs/reset-password").permitAll() 
                .requestMatchers("/api/utilisateurs/forgot-password").permitAll()
                .requestMatchers("/api/utilisateurs/*/restaurer").hasAnyRole("ADMIN","SUPERVISEUR")
                .requestMatchers("/api/utilisateurs/*/restaurer").hasAnyRole("ADMIN","SUPERVISEUR")
                .requestMatchers("/api/dashboard/stats/poste").hasAnyRole("ADMIN")
                .requestMatchers("/api/dashboard/stats/user/**").hasAnyRole("SUPERVISEUR", "ADMIN", "AGENT")
                .requestMatchers("/api/utilisateurs/existe-admin").permitAll() 

                // Routes types-document
                .requestMatchers(HttpMethod.GET, "/api/types-document/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/types-document/**").hasAnyRole("ADMIN", "SUPERVISEUR", "AGENT")
                .requestMatchers(HttpMethod.PUT, "/api/types-document/**").hasAnyRole("ADMIN", "SUPERVISEUR", "AGENT")
                .requestMatchers(HttpMethod.DELETE, "/api/types-document/**").hasAnyRole("ADMIN")

                // Routes declarations
                .requestMatchers(HttpMethod.GET, "/api/declarations/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/declarations/**").hasAnyRole("ADMIN", "SUPERVISEUR", "AGENT")
                .requestMatchers(HttpMethod.PUT, "/api/declarations/**").hasAnyRole("ADMIN", "SUPERVISEUR", "AGENT")
                .requestMatchers(HttpMethod.DELETE, "/api/declarations/**").hasAnyRole("ADMIN", "SUPERVISEUR")
                
                // Routes utilisateurs
                .requestMatchers("/api/utilisateurs/attente/**").hasAnyRole("ADMIN", "SUPERVISEUR")
                .requestMatchers("/api/utilisateurs/**").hasAnyRole("ADMIN", "SUPERVISEUR")
                .requestMatchers("/api/utilisateurs/*/activer").hasAnyRole("ADMIN", "SUPERVISEUR")
                .requestMatchers("/api/utilisateurs/*/desactiver").hasAnyRole("ADMIN", "SUPERVISEUR")
                .requestMatchers("/api/utilisateurs/*/transferer-admin/*").hasAnyRole("ADMIN")
                
                // Toutes les autres requêtes nécessitent authentification
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization", "Content-Disposition"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
