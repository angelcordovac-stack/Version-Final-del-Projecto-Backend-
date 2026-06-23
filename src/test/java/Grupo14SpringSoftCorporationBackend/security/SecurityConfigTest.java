package Grupo14SpringSoftCorporationBackend.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas unitarias de los beans con logica propia definidos en SecurityConfig
 * (passwordEncoder y corsConfigurationSource). No se prueba filterChain()
 * porque requiere un HttpSecurity completo, propio de un test de integracion.
 */
class SecurityConfigTest {

    private final SecurityConfig securityConfig = new SecurityConfig();

    @Test
    void passwordEncoder_devuelveUnaInstanciaDeBCryptPasswordEncoder() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();

        assertThat(encoder).isInstanceOf(BCryptPasswordEncoder.class);
    }

    @Test
    void passwordEncoder_codificaYValidaCorrectamenteUnaPassword() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();

        String hash = encoder.encode("miPassword123");

        assertThat(hash).isNotEqualTo("miPassword123");
        assertThat(encoder.matches("miPassword123", hash)).isTrue();
        assertThat(encoder.matches("otraPassword", hash)).isFalse();
    }

    @Test
    void corsConfigurationSource_permiteLosMetodosHttpEsperados() {
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        CorsConfiguration config = obtenerConfigParaCualquierRuta(source);

        assertThat(config.getAllowedMethods())
                .containsExactlyInAnyOrder("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }

    @Test
    void corsConfigurationSource_permiteCualquierOrigenYCualquierHeader() {
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        CorsConfiguration config = obtenerConfigParaCualquierRuta(source);

        assertThat(config.getAllowedOrigins()).containsExactly("*");
        assertThat(config.getAllowedHeaders()).containsExactly("*");
    }

    private CorsConfiguration obtenerConfigParaCualquierRuta(CorsConfigurationSource source) {
        UrlBasedCorsConfigurationSource urlSource = (UrlBasedCorsConfigurationSource) source;
        return urlSource.getCorsConfigurations().get("/**");
    }
}
