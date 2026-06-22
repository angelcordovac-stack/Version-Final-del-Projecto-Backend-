package Grupo14SpringSoftCorporationBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Entidad que representa un Refresh Token persistido en BD.
 * Permite renovar el Access Token sin que el usuario vuelva a hacer login.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", nullable = false, unique = true)
    private String token;

    @Column(name = "id_usuario", nullable = false)
    private Integer idUsuario;

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    @Column(name = "revocado")
    private Boolean revocado = false;
}
