package com.uneg.galeria.controllers;

import com.uneg.galeria.models.Admin;
import com.uneg.galeria.models.Buyer;
import com.uneg.galeria.models.User;
import com.uneg.galeria.repositories.UserRepository;
import com.uneg.galeria.repositories.AdminRepository;
import com.uneg.galeria.repositories.BuyerRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return userRepository.findByLogin(request.getLogin())
                .filter(user -> user.getPassword().equals(request.getPassword()))
                .map(user -> {
                    Map<String, Object> response = new HashMap<>();

                    // Si el ID del usuario existe en la tabla Admin, es Admin.
                    Optional<Admin> adminOpt = adminRepository.findById(user.getId());
                    if (adminOpt.isPresent()) {
                        response.put("tipo", "ADMIN");
                        response.put("user", adminOpt.get());
                    } else {
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