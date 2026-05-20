package com.uneg.galeria.controllers;

import com.uneg.galeria.models.Admin;
import com.uneg.galeria.services.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admins")
@CrossOrigin(origins = "http://localhost:3000")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @PostMapping("/register")
    public ResponseEntity<Admin> register(@RequestBody Admin admin) {
        return new ResponseEntity<>(adminService.registrarAdmin(admin), HttpStatus.CREATED);
    }

    @GetMapping
    public List<Admin> getAllAdmins() {
        return adminService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Admin> getAdminById(@PathVariable Long id) {
        return adminService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Admin> updateAdmin(@PathVariable Long id, @RequestParam Long requesterId, @RequestBody Admin admin) {
        try {
            return ResponseEntity.ok(adminService.actualizarAdmin(id, admin, requesterId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(@PathVariable Long id, @RequestBody java.util.Map<String, String> passwords) {
        try {
            String actual = passwords.get("passwordActual");
            String nueva = passwords.get("passwordNueva");
            adminService.cambiarPassword(id, actual, nueva);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/login-exists")
    public ResponseEntity<Boolean> loginExists(@RequestParam String login, @RequestParam(required = false) Long excludeId) {
        return ResponseEntity.ok(adminService.loginExists(login, excludeId));
    }

    @DeleteMapping("/{adminId}")
    public ResponseEntity<Void> deleteAdmin(@PathVariable Long adminId, @RequestParam Long requesterId) {
        try {
            adminService.eliminarAdmin(adminId, requesterId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/{id}/resign")
    public ResponseEntity<Void> resignAdmin(@PathVariable Long id) {
        try {
            adminService.renunciarAdmin(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}