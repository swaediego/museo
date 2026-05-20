package com.uneg.galeria.controllers;

import com.uneg.galeria.models.Cargo;
import com.uneg.galeria.services.CargoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cargos")
@CrossOrigin(origins = "http://localhost:3000")
public class CargoController {

    @Autowired
    private CargoService cargoService;

    @GetMapping
    public List<Cargo> listarTodos() {
        return cargoService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cargo> obtenerPorId(@PathVariable Long id) {
        return cargoService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}