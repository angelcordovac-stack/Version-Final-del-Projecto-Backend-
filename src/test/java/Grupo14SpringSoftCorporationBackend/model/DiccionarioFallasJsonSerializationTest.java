package Grupo14SpringSoftCorporationBackend.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba la configuracion @JsonProperty(READ_ONLY) de DiccionarioFallas:
 * nombreAutor debe aparecer al serializar (salida hacia el frontend) pero
 * debe ser ignorado si llega como parte del JSON de entrada.
 */
class DiccionarioFallasJsonSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void serializar_incluyeElNombreDelAutorEnElJsonDeSalida() throws Exception {
        DiccionarioFallas falla = new DiccionarioFallas();
        falla.setProblemaComun("Pantalla negra");
        falla.setNombreAutor("Maria Lopez");

        String json = objectMapper.writeValueAsString(falla);

        assertThat(json).contains("\"nombreAutor\":\"Maria Lopez\"");
    }

    @Test
    void deserializar_ignoraElNombreAutorAunqueVengaEnElJsonDeEntrada() throws Exception {
        String json = "{\"problemaComun\":\"Pantalla negra\",\"nombreAutor\":\"Intento de manipulacion\"}";

        DiccionarioFallas falla = objectMapper.readValue(json, DiccionarioFallas.class);

        assertThat(falla.getProblemaComun()).isEqualTo("Pantalla negra");
        assertThat(falla.getNombreAutor()).isNull();
    }
}
