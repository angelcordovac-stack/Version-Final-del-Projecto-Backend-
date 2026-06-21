package Grupo14SpringSoftCorporationBackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Helper para generar hashes BCrypt de contrasenas en texto plano.
 *
 * Sirve para CREAR el primer usuario manualmente en la BD
 * o MIGRAR contrasenas viejas (que estaban en texto plano) a BCrypt.
 *
 * USO:
 *   GET http://localhost:8080/dev/hash?password=123456
 *
 * Devuelve algo como:
 *   { "hash": "$2a$10$abc..." }
 *
 * Despues hacer en Supabase:
 *   UPDATE usuarios SET password_hash='$2a$10$abc...' WHERE correo='admin@softcorp.com';
 *
 * IMPORTANTE: este endpoint debe DESACTIVARSE o BORRARSE en produccion.
 * Por ahora esta abierto sin autenticacion porque lo necesitas para
 * tener el primer usuario administrador.
 */
@RestController
@RequestMapping("/dev")
public class DevHelperController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/hash")
    public Map<String, String> hash(@RequestParam String password) {
        return Map.of("hash", passwordEncoder.encode(password));
    }
}
