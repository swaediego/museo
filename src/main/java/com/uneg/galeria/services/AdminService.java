package com.uneg.galeria.services;

import com.uneg.galeria.models.Admin;
import java.util.List;
import java.util.Optional;

public interface AdminService {
    Admin registrarAdmin(Admin admin);
    List<Admin> listarTodos();
    Optional<Admin> obtenerPorId(Long id);
    Admin actualizarAdmin(Long id, Admin admin);
    void eliminarAdmin(Long id);
}