package com.uneg.galeria.controllers;

import com.uneg.galeria.models.Admin;
import com.uneg.galeria.models.User;
import com.uneg.galeria.repositories.UserRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return userRepository.findByLogin(request.getLogin())
                .filter(user -> user.getPassword().equals(request.getPassword()))
                .map(user -> {
                    System.out.println("DEBUG: Clase del objeto encontrado: " + user.getClass().getName());
                    System.out.println("DEBUG: ¿Es instancia de Admin?: " + (user instanceof Admin));
                    Map<String, Object> response = new HashMap<>();
                    response.put("user", user);

                    // Si el ID del usuario existe en la tabla Admin, es Admin.
                    if (user instanceof Admin) {
                        response.put("tipo", "ADMIN");
                    } else {
                        response.put("tipo", "BUYER");
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