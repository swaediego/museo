package com.uneg.galeria.controllers;

import com.uneg.galeria.models.Admin;
import com.uneg.galeria.models.Buyer;
import com.uneg.galeria.models.User;
import com.uneg.galeria.repositories.UserRepository;
import com.uneg.galeria.repositories.AdminRepository;
import com.uneg.galeria.repositories.BuyerRepository;
import com.uneg.galeria.security.JwtUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private BuyerRepository buyerRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return userRepository.findByLogin(request.getLogin())
                .filter(user -> user.getPassword().equals(request.getPassword()))
                .map(user -> {
                    Map<String, Object> response = new HashMap<>();

                    Optional<Admin> adminOpt = adminRepository.findById(user.getId());
                    if (adminOpt.isPresent()) {
                        String token = jwtUtil.generateToken("admin", List.of("ADMIN"));
                        response.put("token", token);
                        response.put("tipo", "ADMIN");
                        response.put("user", adminOpt.get());
                    } else {
                        String token = jwtUtil.generateToken("buyer", List.of("BUYER"));
                        response.put("token", token);
                        response.put("tipo", "BUYER");
                        Optional<Buyer> buyerOpt = buyerRepository.findById(user.getId());
                        if (buyerOpt.isPresent()) {
                            response.put("user", buyerOpt.get());
                        } else {
                            response.put("user", user);
                        }
                    }
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @Data
    public static class LoginRequest {
        private String login;
        private String password;
    }
}