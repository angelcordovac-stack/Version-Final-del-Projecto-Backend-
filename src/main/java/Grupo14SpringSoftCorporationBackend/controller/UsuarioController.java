package Grupo14SpringSoftCorporationBackend.controller;

import Grupo14SpringSoftCorporationBackend.model.Usuario;
import Grupo14SpringSoftCorporationBackend.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService service;

    // LOGIN - publico
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Usuario user) {
        
        Map<String, Object> resultado = service.login(user.getCorreo(), user.getPasswordHash());

        if (resultado != null) {
            return ResponseEntity.ok(resultado);
        }
        return ResponseEntity.status(401).body(Map.of("error", "Credenciales invalidas"));
    }
}