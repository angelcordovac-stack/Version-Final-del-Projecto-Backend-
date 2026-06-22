package Grupo14SpringSoftCorporationBackend.controller;

import Grupo14SpringSoftCorporationBackend.model.RefreshToken;
import Grupo14SpringSoftCorporationBackend.model.Usuario;
import Grupo14SpringSoftCorporationBackend.repository.UsuarioRepository;
import Grupo14SpringSoftCorporationBackend.service.RefreshTokenService;
import Grupo14SpringSoftCorporationBackend.service.UsuarioService;
import Grupo14SpringSoftCorporationBackend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService service;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // LOGIN - publico
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Usuario user) {
        Map<String, Object> resultado = service.login(user.getCorreo(), user.getPasswordHash());

        if (resultado != null) {
            return ResponseEntity.ok(resultado);
        }
        return ResponseEntity.status(401).body(Map.of("error", "Credenciales invalidas"));
    }

    /**
     * REFRESH TOKEN - publico.
     * Recibe el refreshToken, valida que sea válido y no esté expirado,
     * y devuelve un nuevo accessToken (+ rota el refreshToken).
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> body) {
        String refreshTokenStr = body.get("refreshToken");

        if (refreshTokenStr == null || refreshTokenStr.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El refreshToken es requerido"));
        }

        // Valida el token (lanza 401 si es inválido o expirado)
        RefreshToken rt = refreshTokenService.validar(refreshTokenStr);

        // Buscar el usuario para generar un nuevo access token con datos actualizados
        Usuario usuario = usuarioRepository.findById(rt.getIdUsuario())
                .orElse(null);

        if (usuario == null || Boolean.FALSE.equals(usuario.getActivo())) {
            return ResponseEntity.status(401).body(Map.of("error", "Usuario no encontrado o inactivo"));
        }

        // Generar nuevo access token
        String nuevoAccessToken = jwtUtil.generateToken(
                usuario.getCorreo(), usuario.getIdUsuario(), usuario.getIdPerfil());

        // Rotar el refresh token (el servicio elimina el anterior y crea uno nuevo)
        RefreshToken nuevoRefreshToken = refreshTokenService.crear(usuario.getIdUsuario());

        return ResponseEntity.ok(Map.of(
                "token", nuevoAccessToken,
                "refreshToken", nuevoRefreshToken.getToken()
        ));
    }

    /**
     * LOGOUT - revoca el refresh token del usuario.
     * Requiere autenticación (JWT válido en Authorization header).
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> body) {
        String refreshTokenStr = body.get("refreshToken");
        if (refreshTokenStr != null && !refreshTokenStr.isBlank()) {
            try {
                RefreshToken rt = refreshTokenService.validar(refreshTokenStr);
                refreshTokenService.revocarPorUsuario(rt.getIdUsuario());
            } catch (Exception ignored) {
                // Si el token ya está inválido, no es un error del cliente
            }
        }
        return ResponseEntity.ok(Map.of("mensaje", "Sesión cerrada correctamente"));
    }
}
