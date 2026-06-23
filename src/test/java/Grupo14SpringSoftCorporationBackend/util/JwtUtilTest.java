package Grupo14SpringSoftCorporationBackend.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas unitarias para JwtUtil.
 * Se inyectan manualmente los valores de @Value (secret/expiration)
 * con ReflectionTestUtils ya que no se levanta el contexto de Spring.
 */
class JwtUtilTest {

    // Clave de al menos 256 bits (32 caracteres) requerida por HS256
    private static final String SECRET = "claveSecretaDePruebaConLongitudSuficiente123456";

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secretKey", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", 3600000L); // 1 hora
    }

    @Test
    void generateToken_devuelveUnTokenNoNuloNiVacio() {
        String token = jwtUtil.generateToken("usuario@correo.com", 1, 2);

        assertThat(token).isNotNull();
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // header.payload.signature
    }

    @Test
    void extractEmail_devuelveElCorreoUsadoAlGenerarElToken() {
        String token = jwtUtil.generateToken("juan.perez@empresa.com", 10, 1);

        String email = jwtUtil.extractEmail(token);

        assertThat(email).isEqualTo("juan.perez@empresa.com");
    }

    @Test
    void extractIdUsuario_devuelveElIdUsuarioUsadoAlGenerarElToken() {
        String token = jwtUtil.generateToken("correo@dominio.com", 42, 3);

        Integer idUsuario = jwtUtil.extractIdUsuario(token);

        assertThat(idUsuario).isEqualTo(42);
    }

    @Test
    void extractIdPerfil_devuelveElIdPerfilUsadoAlGenerarElToken() {
        String token = jwtUtil.generateToken("correo@dominio.com", 42, 3);

        Integer idPerfil = jwtUtil.extractIdPerfil(token);

        assertThat(idPerfil).isEqualTo(3);
    }

    @Test
    void isTokenValid_devuelveTrueParaUnTokenValidoYNoExpirado() {
        String token = jwtUtil.generateToken("correo@dominio.com", 1, 1);

        assertThat(jwtUtil.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_devuelveFalseParaUnTokenManipuladoOMalFormado() {
        String token = jwtUtil.generateToken("correo@dominio.com", 1, 1);
        String tokenManipulado = token.substring(0, token.length() - 2) + "xx";

        assertThat(jwtUtil.isTokenValid(tokenManipulado)).isFalse();
    }

    @Test
    void isTokenValid_devuelveFalseParaUnTokenVacioONulo() {
        assertThat(jwtUtil.isTokenValid("")).isFalse();
    }

    @Test
    void isTokenValid_devuelveFalseCuandoElTokenYaExpiro() {
        // Expiracion negativa: el token nace ya vencido
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", -10000L);
        String tokenExpirado = jwtUtil.generateToken("correo@dominio.com", 1, 1);

        assertThat(jwtUtil.isTokenValid(tokenExpirado)).isFalse();
    }

    @Test
    void isTokenValid_devuelveFalseCuandoElTokenFueFirmadoConOtraClave() {
        String token = jwtUtil.generateToken("correo@dominio.com", 1, 1);

        JwtUtil otroJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(otroJwtUtil, "secretKey", "otraClaveSecretaCompletamenteDiferenteDe32Bytes");
        ReflectionTestUtils.setField(otroJwtUtil, "jwtExpiration", 3600000L);

        assertThat(otroJwtUtil.isTokenValid(token)).isFalse();
    }
}
