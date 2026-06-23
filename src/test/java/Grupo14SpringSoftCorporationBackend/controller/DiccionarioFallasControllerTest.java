package Grupo14SpringSoftCorporationBackend.controller;

import Grupo14SpringSoftCorporationBackend.repository.UsuarioRepository;
import Grupo14SpringSoftCorporationBackend.util.JwtUtil;
import Grupo14SpringSoftCorporationBackend.model.DiccionarioFallas;
import Grupo14SpringSoftCorporationBackend.service.DiccionarioFallasService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas funcionales (sin seguridad) del controlador.
 * La autorizacion por rol se prueba aparte en DiccionarioFallasControllerSecurityTest.
 */
@WebMvcTest(DiccionarioFallasController.class)
@AutoConfigureMockMvc(addFilters = false)
class DiccionarioFallasControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // JwtAuthenticationFilter es recogido automaticamente por @WebMvcTest
    // (es un Filter), por lo que sus dependencias deben mockearse aunque
    // este controlador no tenga relacion directa con la seguridad.
    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private DiccionarioFallasService service;

    @Test
    void registrar_devuelveLaFallaCreada() throws Exception {
        DiccionarioFallas entrada = new DiccionarioFallas();
        entrada.setProblemaComun("Pantalla negra");
        entrada.setSolucionSugerida("Cambiar cable VGA");
        entrada.setEstado("critico");

        DiccionarioFallas guardada = new DiccionarioFallas();
        guardada.setIdFalla(1);
        guardada.setProblemaComun("Pantalla negra");
        guardada.setSolucionSugerida("Cambiar cable VGA");
        guardada.setEstado("CRITICO");

        when(service.registrar(any(DiccionarioFallas.class))).thenReturn(guardada);

        mockMvc.perform(post("/api/fallas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entrada)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idFalla").value(1))
                .andExpect(jsonPath("$.estado").value("CRITICO"));
    }

    @Test
    void registrar_propagaElErrorDeValidacionDelServicioComo400() throws Exception {
        DiccionarioFallas entrada = new DiccionarioFallas();
        entrada.setEstado("INVALIDO");

        when(service.registrar(any(DiccionarioFallas.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado invalido"));

        mockMvc.perform(post("/api/fallas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entrada)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Estado invalido"));
    }

    @Test
    void listar_devuelveTodasLasFallas() throws Exception {
        DiccionarioFallas f1 = new DiccionarioFallas();
        f1.setIdFalla(1);
        when(service.listar()).thenReturn(List.of(f1));

        mockMvc.perform(get("/api/fallas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void buscar_delegaElKeywordAlServicio() throws Exception {
        when(service.buscar("pantalla")).thenReturn(List.of());

        mockMvc.perform(get("/api/fallas/buscar").param("keyword", "pantalla"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void buscar_devuelve400CuandoFaltaElParametroKeyword() throws Exception {
        mockMvc.perform(get("/api/fallas/buscar"))
                .andExpect(status().isBadRequest());
    }
}
