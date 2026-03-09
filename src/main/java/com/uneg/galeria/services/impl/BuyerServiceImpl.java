package com.uneg.galeria.services.impl;



import com.uneg.galeria.models.Buyer;
import com.uneg.galeria.models.MembershipPayment;
import com.uneg.galeria.models.UserAnswers;
import com.uneg.galeria.repositories.BuyerRepository;
import com.uneg.galeria.repositories.MembershipPaymentRepository;
import com.uneg.galeria.repositories.UserAnswersRepository;
import com.uneg.galeria.services.BuyerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class BuyerServiceImpl implements BuyerService {

    @Autowired
    private BuyerRepository buyerRepository;

    @Autowired
    private UserAnswersRepository userAnswersRepository;

    @Autowired
    private MembershipPaymentRepository membershipPaymentRepository;

    @Override
    @Transactional
    public Buyer registrarComprador(Buyer buyer) {
        // Validación: El login y el email deben ser únicos
        if (buyerRepository.existsByLogin(buyer.getLogin())) {
            throw new RuntimeException("Error: El nombre de usuario ya está en uso.");
        }
        if (buyerRepository.existsByEmail(buyer.getEmail())) {
            throw new RuntimeException("Error: El correo electrónico ya está registrado.");
        }

        // El estatus por defecto es activo y membresía en false
        return buyerRepository.save(buyer);
    }

    @Override
    @Transactional
    public boolean procesarPagoMembresia(Long buyerId, String metodoPago) {
        return buyerRepository.findById(buyerId).map(buyer -> {

            // 1. Crear el registro histórico del pago (Auditoría)
            MembershipPayment pago = new MembershipPayment();
            pago.setComprador(buyer);
            pago.setMetodoPago(metodoPago);
            pago.setMonto(10.0); // Monto fijo
            pago.setFechaPago(LocalDateTime.now());

            // Guardamos el registro en la tabla membership_payment
            membershipPaymentRepository.save(pago);

            // 2. Marcar membresía como paga en el perfil del comprador
            buyer.setMembresiaPaga(true);

            // 3. Generar código de seguridad aleatorio de 10 caracteres
            String nuevoCodigo = generarCodigoAleatorio(10);
            buyer.setCodigoSeguridad(nuevoCodigo);

            // Guardamos los cambios en el comprador
            buyerRepository.save(buyer);

            // Simulación de envío de correo (Requerimiento del PDF)
            System.out.println("SISTEMA: Registro de pago #" + pago.getId() + " creado exitosamente.");
            System.out.println("SISTEMA: El comprador " + buyer.getNombre() + " ahora está activo.");
            System.out.println("SISTEMA: Enviando código [" + nuevoCodigo + "] al correo " + buyer.getEmail());

            return true;
        }).orElseThrow(() -> new RuntimeException("Comprador no encontrado con ID: " + buyerId));
    }

    @Override
    public String recuperarCodigoSeguridad(String email, List<String> respuestasUsuario) {
        // 1. Buscar al comprador
        Buyer buyer = buyerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No existe un usuario asociado a ese correo."));

        // 2. Obtener las respuestas de seguridad guardadas
        List<UserAnswers> respuestasCorrectas = userAnswersRepository.findByUsuarioId(buyer.getId());

        // 3. Validar que tenga las 3 respuestas configuradas
        if (respuestasCorrectas.size() < 3) {
            throw new RuntimeException("El usuario no ha configurado sus preguntas de seguridad.");
        }

        // 4. Comparar respuestas
        for (int i = 0; i < respuestasCorrectas.size(); i++) {
            String guardada = respuestasCorrectas.get(i).getRespuesta().trim().toLowerCase();
            String ingresada = respuestasUsuario.get(i).trim().toLowerCase();

            if (!guardada.equals(ingresada)) {
                throw new RuntimeException("Validación fallida: Una o más respuestas no coinciden.");
            }
        }

        // 5. Si pasa la validación, devuelve el código
        return buyer.getCodigoSeguridad();
    }

    @Override
    public Optional<Buyer> buscarPorLogin(String login) {
        return buyerRepository.findByLogin(login);
    }


     //Método utilitario para generar el código alfanumérico
    private String generarCodigoAleatorio(int longitud) {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random rnd = new Random();
        while (sb.length() < longitud) {
            int index = (int) (rnd.nextFloat() * caracteres.length());
            sb.append(caracteres.charAt(index));
        }
        return sb.toString();
    }

    @Override
    public List<Buyer> listarTodos() {
        return buyerRepository.findAll();
    }

    //Metodo para la logica del login
    @Override
    public Buyer login(String login, String password) {
        // Buscamos al comprador por su login de usuario
        return buyerRepository.findByLogin(login)
                .filter(buyer -> buyer.getPassword().equals(password))
                .orElseThrow(() -> new RuntimeException("Credenciales incorrectas o usuario no encontrado"));
    }

    @Override
    public Buyer guardarComprador(Buyer buyer) {
        // Lógica importante: Si la contraseña ha cambiado, deberías encriptarla aquí
        // antes de llamar a buyerRepository.save(buyer);
        return buyerRepository.save(buyer);
    }

    @Override
    public Optional<Buyer> obtenerPorId(Long id) {
        return buyerRepository.findById(id);
    }

    @Override
    public void eliminarComprador(Long id) {
        buyerRepository.deleteById(id);
    }
}