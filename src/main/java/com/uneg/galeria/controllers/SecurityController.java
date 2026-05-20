package com.uneg.galeria.controllers;

import com.uneg.galeria.models.SecurityQuestion;
import com.uneg.galeria.models.UserAnswers;
import com.uneg.galeria.repositories.SecurityQuestionRepository;
import com.uneg.galeria.repositories.UserAnswersRepository;
import com.uneg.galeria.repositories.BuyerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/security")
@CrossOrigin(origins = "http://localhost:3000")
public class SecurityController {

    @Autowired private SecurityQuestionRepository questionRepository;
    @Autowired private UserAnswersRepository answersRepository;
    @Autowired private BuyerRepository buyerRepository;

    // Obtener las preguntas para el formulario de registro
    @GetMapping("/questions")
    public List<SecurityQuestion> getAllQuestions() {
        return questionRepository.findAll();
    }

    // Guardar las respuestas del usuario (se llama justo después del registro)
    @PostMapping("/answers")
    public void saveAnswers(@RequestBody List<UserAnswers> answers) {
        answersRepository.saveAll(answers);
    }

    // Obtener las preguntas configuradas por un usuario según su correo
    @GetMapping("/questions/by-email")
    public ResponseEntity<?> getQuestionsByEmail(@RequestParam String email) {
        return buyerRepository.findByEmail(email)
                .map(buyer -> {
                    List<UserAnswers> answers = answersRepository.findByUsuarioId(buyer.getId());
                    List<SecurityQuestion> questions = answers.stream()
                            .map(UserAnswers::getPregunta)
                            .toList();
                    return ResponseEntity.ok(questions);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}