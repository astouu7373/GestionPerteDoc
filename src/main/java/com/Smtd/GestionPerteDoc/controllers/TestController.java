//package com.smtd.GestionDoc.controllers;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.Smtd.GestionPerteDoc.security.services.CustomUserDetails;
//import com.Smtd.GestionPerteDoc.security.services.CustomUserDetailsService;
//
//import lombok.RequiredArgsConstructor;
//
//@RestController
//@RequestMapping("/api/test")
//@RequiredArgsConstructor
//public class TestController {
//
//    private final CustomUserDetailsService userDetailsService;
//
//    @GetMapping("/roles/{email}")
//    public ResponseEntity<?> getRoles(@PathVariable String email) {
//        try {
//            CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(email);
//            return ResponseEntity.ok(userDetails.getAuthorities());
//        } catch (UsernameNotFoundException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur non trouvé");
//        }
//    }
//}



