package com.uneg.galeria.controllers;

import com.uneg.galeria.models.Admin;
import com.uneg.galeria.models.User;
import com.uneg.galeria.repositories.UserRepository; // Necesitarás este repo base
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth") // Ruta dedicada para temas de sesión
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return userRepository.findByLogin(request.getLogin())
                .filter(user -> user.getPassword().equals(request.getPassword()))
                .map(user -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("user", user);
                    // Aquí usamos tu lógica de identificar el cargo
                    response.put("tipo", (user instanceof Admin) ? "ADMIN" : "BUYER");
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