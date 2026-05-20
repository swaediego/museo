package com.uneg.galeria.services;

import com.uneg.galeria.models.Cargo;
import java.util.List;
import java.util.Optional;

public interface CargoService {
    List<Cargo> listarTodos();
    Optional<Cargo> obtenerPorId(Long id);
}