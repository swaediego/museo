package com.uneg.galeria.controllers;

import com.uneg.galeria.models.Buyer;
import com.uneg.galeria.services.BuyerService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/buyers")
@CrossOrigin(origins = "http://localhost:3000")
public class BuyerController {

    @Autowired
    private BuyerService buyerService;

    // 1. Registro de Comprador
    @PostMapping("/register")
    public ResponseEntity<Buyer> register(@RequestBody Buyer buyer) {
        try {
            Buyer nuevoComprador = buyerService.registrarComprador(buyer);
            return new ResponseEntity<>(nuevoComprador, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 2. Procesar Pago de Membresía ($10.00)
    // Recibimos un mapa con el metodoPago para mayor flexibilidad
    // Cambia el tipo de retorno de ResponseEntity<String> a ResponseEntity<Buyer>
    @PostMapping("/{id}/pay-membership")
    public ResponseEntity<Buyer> payMembership(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String metodoPago = request.get("metodoPago");
        boolean exito = buyerService.procesarPagoMembresia(id, metodoPago);

        if (exito) {
            // Recuperamos al comprador actualizado desde el servicio
            return buyerService.obtenerPorId(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 3. Recuperar Código de Seguridad (Validando las 3 respuestas)
    @PostMapping("/recover-code")
    public ResponseEntity<?> recoverCode(@RequestBody RecoveryRequest request) {
        try {
            String codigo = buyerService.recuperarCodigoSeguridad(request.getEmail(), request.getRespuestas());
            return ResponseEntity.ok("Validación exitosa. Su código es: " + codigo);
        } catch (RuntimeException e) {
            // Si las respuestas no coinciden o el usuario no existe, devolvemos el error
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // clase complementaria para recovery
    public static class RecoveryRequest {
        private String email;
        private List<String> respuestas;

        // Constructor vacío explícito
        public RecoveryRequest() {
        }

        // Getters y Setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public List<String> getRespuestas() { return respuestas; }
        public void setRespuestas(List<String> respuestas) { this.respuestas = respuestas; }
    }

    //6. DTO para no dar todo el objeto User
    public static class LoginRequest {
        private String login;
        private String password;

        public LoginRequest() {}
        // Getters y Setters
        public String getLogin() { return login; }
        public void setLogin(String login) { this.login = login; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    // 7. Actualizar datos del perfil
    @PatchMapping("/{id}")
    public ResponseEntity<Buyer> updateProfile(@PathVariable Long id, @RequestBody Buyer updatedBuyer) {
        return buyerService.obtenerPorId(id)
                .map(existingBuyer -> {
                    // Actualizamos solo los campos necesarios
                    if (updatedBuyer.getNombre() != null) existingBuyer.setNombre(updatedBuyer.getNombre());
                    if (updatedBuyer.getApellido() != null) existingBuyer.setApellido(updatedBuyer.getApellido());
                    if (updatedBuyer.getEmail() != null) existingBuyer.setEmail(updatedBuyer.getEmail());
                    if (updatedBuyer.getTelefono() != null) existingBuyer.setTelefono(updatedBuyer.getTelefono());
                    if (updatedBuyer.getDireccionEnvio() != null) existingBuyer.setDireccionEnvio(updatedBuyer.getDireccionEnvio());
                    if (updatedBuyer.getPassword() != null) existingBuyer.setPassword(updatedBuyer.getPassword());

                    buyerService.guardarComprador(existingBuyer); // Asegúrate de tener este método en tu Service
                    return ResponseEntity.ok(existingBuyer);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // 8. Eliminar comprador (para uso exclusivo del Admin)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBuyer(@PathVariable Long id) {
        buyerService.desactivarComprador(id);
        return ResponseEntity.noContent().build();
    }


    //9. hace un soft Delete y solo desactiva el buyer
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivarBuyer(@PathVariable Long id) {
        buyerService.desactivarComprador(id);
        return ResponseEntity.noContent().build();
    }

    //10. nos da todos los Compradores ya sea que este o no activos, dependiendo del filtro del front
    @GetMapping
    public List<Buyer> getAllBuyers(@RequestParam(defaultValue = "true") boolean soloActivos) {
        System.out.println("DEBUG: El filtro soloActivos recibido es: " + soloActivos);
        return buyerService.listarCompradores(soloActivos);
    }
}