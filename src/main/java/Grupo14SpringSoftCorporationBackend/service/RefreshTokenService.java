package Grupo14SpringSoftCorporationBackend.service;

import Grupo14SpringSoftCorporationBackend.model.RefreshToken;
import Grupo14SpringSoftCorporationBackend.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

/**
 * Servicio para gestionar el ciclo de vida de los Refresh Tokens.
 * - Crea un token único con expiración configurable.
 * - Valida si el token es válido y no ha expirado.
 * - Revoca tokens anteriores del usuario al hacer login (rotación).
 */
@Service
public class RefreshTokenService {

    @Value("${jwt.refresh-expiration:604800000}") // 7 días por defecto
    private long refreshExpiration;

    @Autowired
    private RefreshTokenRepository repo;

    /**
     * Crea (o rota) el refresh token para un usuario.
     * Elimina los tokens anteriores del usuario antes de crear uno nuevo.
     */
    public RefreshToken crear(Integer idUsuario) {
        // Rotar: eliminar tokens previos del usuario
        repo.deleteByIdUsuario(idUsuario);

        RefreshToken rt = new RefreshToken();
        rt.setToken(UUID.randomUUID().toString());
        rt.setIdUsuario(idUsuario);
        rt.setExpiryDate(Instant.now().plusMillis(refreshExpiration));
        rt.setRevocado(false);

        return repo.save(rt);
    }

    /**
     * Valida que el token exista, no esté revocado y no haya expirado.
     * Lanza 401 si alguna condición falla.
     */
    public RefreshToken validar(String token) {
        RefreshToken rt = repo.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Refresh token no encontrado o inválido"));

        if (Boolean.TRUE.equals(rt.getRevocado())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token revocado");
        }

        if (rt.getExpiryDate().isBefore(Instant.now())) {
            repo.delete(rt);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expirado");
        }

        return rt;
    }

    /**
     * Revoca (invalida) todos los refresh tokens de un usuario.
     * Llamar al hacer logout.
     */
    public void revocarPorUsuario(Integer idUsuario) {
        repo.deleteByIdUsuario(idUsuario);
    }
}
