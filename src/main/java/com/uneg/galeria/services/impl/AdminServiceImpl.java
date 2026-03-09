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
        if (userRepository.findByLogin(admin.getLogin()).isPresent()) {
            throw new RuntimeException("El nombre de usuario ya existe.");
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
    public Admin actualizarAdmin(Long id, Admin updatedAdmin) {
        return adminRepository.findById(id).map(existing -> {
            if (updatedAdmin.getNombre() != null) existing.setNombre(updatedAdmin.getNombre());
            if (updatedAdmin.getApellido() != null) existing.setApellido(updatedAdmin.getApellido());
            if (updatedAdmin.getCargo() != null) existing.setCargo(updatedAdmin.getCargo());
            if (updatedAdmin.getPassword() != null) existing.setPassword(updatedAdmin.getPassword());
            return adminRepository.save(existing);
        }).orElseThrow(() -> new RuntimeException("Admin no encontrado"));
    }

    @Override
    public void eliminarAdmin(Long id) {
        adminRepository.deleteById(id);
    }
}