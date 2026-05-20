package com.uneg.galeria.services.impl;

import com.uneg.galeria.models.Cargo;
import com.uneg.galeria.repositories.CargoRepository;
import com.uneg.galeria.services.CargoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CargoServiceImpl implements CargoService {

    @Autowired
    private CargoRepository cargoRepository;

    @Override
    public List<Cargo> listarTodos() {
        return cargoRepository.findAll();
    }

    @Override
    public Optional<Cargo> obtenerPorId(Long id) {
        return cargoRepository.findById(id);
    }
}