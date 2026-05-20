package com.uneg.galeria.services.impl;

import com.uneg.galeria.models.Admin;
import com.uneg.galeria.repositories.AdminRepository;
import com.uneg.galeria.repositories.UserRepository;
import com.uneg.galeria.services.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Admin registrarAdmin(Admin admin) {
        if (admin.getLogin() == null || !admin.getLogin().matches("^[a-zA-Z0-9_]{3,50}$")) {
            throw new RuntimeException("El nombre de usuario debe tener entre 3 y 50 caracteres, solo letras, números y guiones bajos, sin espacios.");
        }
        if (userRepository.findByLogin(admin.getLogin()).isPresent()) {
            throw new RuntimeException("El nombre de usuario ya existe.");
        }
        if (admin.getRol() == null) {
            admin.setRol("SECUNDARIO");
        }
        return adminRepository.save(admin);
    }

    @Override
    public List<Admin> listarTodos() {
        return adminRepository.findAll();
    }

    @Override
    public Optional<Admin> obtenerPorId(Long id) {
        return adminRepository.findById(id);
    }

    @Override
    public Admin actualizarAdmin(Long id, Admin updatedAdmin, Long requesterId) {
        Admin target = adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin no encontrado"));

        Admin requester = adminRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("Solicitante no encontrado"));

        boolean isEditingSelf = requesterId.equals(id);

        if (isEditingSelf) {
            if (updatedAdmin.getLogin() != null) {
                if (userRepository.findByLogin(updatedAdmin.getLogin()).isPresent()
                        && !target.getLogin().equals(updatedAdmin.getLogin())) {
                    throw new RuntimeException("El nombre de usuario ya está en uso.");
                }
                target.setLogin(updatedAdmin.getLogin());
            }
            if (updatedAdmin.getNombre() != null) target.setNombre(updatedAdmin.getNombre());
            if (updatedAdmin.getApellido() != null) target.setApellido(updatedAdmin.getApellido());
            if (updatedAdmin.getEmail() != null) target.setEmail(updatedAdmin.getEmail());
            if (updatedAdmin.getTelefono() != null) target.setTelefono(updatedAdmin.getTelefono());
            if (updatedAdmin.getCargo() != null) target.setCargo(updatedAdmin.getCargo());
        } else {
            if (updatedAdmin.getRol() != null) {
                if (!"PRINCIPAL".equals(requester.getRol())) {
                    throw new RuntimeException("Solo un administrador principal puede cambiar el rol de otros.");
                }

                if ("PRINCIPAL".equals(target.getRol())) {
                    throw new RuntimeException("No se puede cambiar el rol de otro administrador principal.");
                }

                if ("PRINCIPAL".equals(updatedAdmin.getRol()) && "SECUNDARIO".equals(target.getRol())) {
                    target.setRol(updatedAdmin.getRol());
                } else if ("SECUNDARIO".equals(updatedAdmin.getRol())) {
                    if (contarPrincipales() <= 1) {
                        throw new RuntimeException("No se puede convertir a secundario si no hay otro administrador principal.");
                    }
                    target.setRol(updatedAdmin.getRol());
                }
            }
        }

        return adminRepository.save(target);
    }

    @Override
    public void eliminarAdmin(Long adminId, Long requesterId) {
        Admin target = adminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin no encontrado"));

        Admin requester = adminRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("Solicitante no encontrado"));

        if ("SECUNDARIO".equals(requester.getRol())) {
            throw new RuntimeException("Un admin secundario no puede eliminar a otros administradores.");
        }

        if ("PRINCIPAL".equals(target.getRol())) {
            throw new RuntimeException("No se puede eliminar a un administrador principal.");
        }

        adminRepository.deleteById(adminId);
    }

    @Override
    public void renunciarAdmin(Long adminId) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin no encontrado"));

        if (!"PRINCIPAL".equals(admin.getRol())) {
            throw new RuntimeException("Solo un admin principal puede renunciar.");
        }

        long principalesCount = contarPrincipales();
        if (principalesCount <= 1) {
            throw new RuntimeException("No se puede renunciar si no hay otro administrador principal.");
        }

        admin.setRol("SECUNDARIO");
        adminRepository.save(admin);
    }

    @Override
    public int contarPrincipales() {
        return adminRepository.findAll().stream()
                .filter(a -> "PRINCIPAL".equals(a.getRol()))
                .toList()
                .size();
    }

    @Override
    public void cambiarPassword(Long adminId, String passwordActual, String passwordNueva) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin no encontrado"));

        if (!admin.getPassword().equals(passwordActual)) {
            throw new RuntimeException("La contraseña actual es incorrecta.");
        }

        admin.setPassword(passwordNueva);
        adminRepository.save(admin);
    }

    @Override
    public boolean loginExists(String login, Long excludeId) {
        Optional<Admin> existing = adminRepository.findByLogin(login);
        if (existing.isEmpty()) return false;
        return !existing.get().getId().equals(excludeId);
    }
}