package com.Smtd.GestionPerteDoc.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.Smtd.GestionPerteDoc.security.jwt.JwtUtil;
import com.Smtd.GestionPerteDoc.security.services.CustomUserDetailsService;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        //  NE PAS filtrer les routes d'authentification
        boolean isAuthRoute = path.startsWith("/api/auth/");
        if (isAuthRoute) {
            System.out.println(" JWT Filter skipped for auth route: " + path);
        }
        return isAuthRoute;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        System.out.println("=== JWT FILTER EXECUTED ===");
        System.out.println("URL: " + request.getRequestURI());
        
        final String authHeader = request.getHeader("Authorization");
        System.out.println("Authorization Header: " + authHeader);

        // ✅ VÉRIFICATION AMÉLIORÉE : Valider le format du header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("No Bearer token found, skipping JWT filter");
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        System.out.println("JWT Token length: " + jwt.length());
        
        //  VÉRIFICATION : Token non vide
        if (jwt.isEmpty() || "undefined".equals(jwt) || "null".equals(jwt)) {
            System.out.println("Invalid JWT token: empty or undefined");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String username = jwtUtil.extractUsername(jwt);
            System.out.println("Extracted username: " + username);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                System.out.println("Loading user details for: " + username);
                
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                System.out.println("UserDetails loaded: " + (userDetails != null ? userDetails.getUsername() : "NULL"));

                //  VALIDATION : Vérifier que le token est valide
                if (userDetails != null && jwtUtil.validateToken(jwt, userDetails)) {
                    System.out.println(" Token validated, creating authentication token");
                    
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, // Principal
                            null, // Credentials
                            userDetails.getAuthorities() // Authorities
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println(" Authentication set in SecurityContext");
                    
                    // Vérification
                    var auth = SecurityContextHolder.getContext().getAuthentication();
                    System.out.println("SecurityContext Principal: " + (auth != null ? auth.getPrincipal().getClass().getName() : "NULL"));
                } else {
                    System.out.println(" Token validation failed");
                    // Ne pas bloquer la requête, laisser passer pour d'autres traitements
                }
            }
        } catch (UsernameNotFoundException e) {
            System.out.println(" User not found: " + e.getMessage());
            // Ne pas envoyer de réponse d'erreur ici, laisser passer la requête
            // Le contrôleur gérera l'authentification
        } catch (Exception e) {
            System.out.println(" JWT Filter error: " + e.getMessage());
            e.printStackTrace();
            // Ne pas bloquer la requête en cas d'erreur JWT
        }

        filterChain.doFilter(request, response);
    }
}