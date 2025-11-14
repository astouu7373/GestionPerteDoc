//package com.Smtd.GestionPerteDoc.controllers;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.Smtd.GestionPerteDoc.security.services.CustomUserDetails;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/debug")
//public class DebugController {
//
//    @GetMapping("/auth")
//    public Map<String, Object> debugAuth(@AuthenticationPrincipal CustomUserDetails userDetails) {
//        Map<String, Object> response = new HashMap<>();
//        
//        if (userDetails == null) {
//            response.put("status", "NOT_AUTHENTICATED");
//            return response;
//        }
//        
//        response.put("status", "AUTHENTICATED");
//        response.put("username", userDetails.getUsername());
//        response.put("authorities", userDetails.getAuthorities());
//        response.put("roles", userDetails.getAuthorities().stream()
//                .map(auth -> auth.getAuthority())
//                .toList());
//        
//        // Vérification des rôles spécifiques
//        response.put("hasRole_ADMIN", userDetails.getAuthorities().stream()
//                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
//        response.put("hasRole_AGENT", userDetails.getAuthorities().stream()
//                .anyMatch(auth -> auth.getAuthority().equals("ROLE_AGENT")));
//        response.put("hasRole_SUPERVISEUR", userDetails.getAuthorities().stream()
//                .anyMatch(auth -> auth.getAuthority().equals("ROLE_SUPERVISEUR")));
//        
//        return response;
//    }
//
//    @PostMapping("/test-create")
//    public Map<String, Object> testCreate(@RequestBody Map<String, Object> requestData, 
//                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
//        Map<String, Object> response = new HashMap<>();
//        
//        System.out.println("=== 🧪 TEST CREATE DEBUG ===");
//        System.out.println("User: " + (userDetails != null ? userDetails.getUsername() : "null"));
//        System.out.println("Authorities: " + (userDetails != null ? userDetails.getAuthorities() : "null"));
//        System.out.println("Data: " + requestData);
//        
//        response.put("status", "success");
//        response.put("user", userDetails != null ? userDetails.getUsername() : "null");
//        response.put("authorities", userDetails != null ? userDetails.getAuthorities().toString() : "null");
//        response.put("data_received", requestData);
//        
//        return response;
//    }
//}