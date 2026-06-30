package Grupo14SpringSoftCorporationBackend.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioJsonSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void serializar_nuncaIncluyeLaPasswordHashEnElJsonDeSalida() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1);
        usuario.setNombreCompleto("Ana Torres");
        usuario.setCorreo("ana@correo.com");
        usuario.setPasswordHash("$2a$10$hashSecreto");

        String json = objectMapper.writeValueAsString(usuario);

        assertThat(json).doesNotContain("hashSecreto");
        assertThat(json).doesNotContain("passwordHash");
        assertThat(json).contains("ana@correo.com");
    }

    @Test
    void deserializar_aceptaLaClavePasswordComoAliasDePasswordHash() throws Exception {
        String json = "{\"correo\":\"ana@correo.com\",\"password\":\"123456\"}";

        Usuario usuario = objectMapper.readValue(json, Usuario.class);

        assertThat(usuario.getPasswordHash()).isEqualTo("123456");
    }

    @Test
    void deserializar_aceptaLaClavePasswordHashDirectamente() throws Exception {
        String json = "{\"correo\":\"ana@correo.com\",\"passwordHash\":\"abcdef\"}";

        Usuario usuario = objectMapper.readValue(json, Usuario.class);

        assertThat(usuario.getPasswordHash()).isEqualTo("abcdef");
    }
}
