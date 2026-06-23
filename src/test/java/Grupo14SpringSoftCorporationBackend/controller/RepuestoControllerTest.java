package Grupo14SpringSoftCorporationBackend.controller;

import Grupo14SpringSoftCorporationBackend.repository.UsuarioRepository;
import Grupo14SpringSoftCorporationBackend.util.JwtUtil;
import Grupo14SpringSoftCorporationBackend.model.Repuesto;
import Grupo14SpringSoftCorporationBackend.service.RepuestoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RepuestoController.class)
@AutoConfigureMockMvc(addFilters = false)
class RepuestoControllerTest {

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
    private RepuestoService service;

    @Test
    void solicitar_devuelveElRepuestoCreado() throws Exception {
        Repuesto entrada = new Repuesto();
        entrada.setDescripcion("Bateria laptop Dell");

        Repuesto guardado = new Repuesto();
        guardado.setIdRepuesto(1);
        guardado.setDescripcion("Bateria laptop Dell");
        guardado.setEstado("Solicitado");

        when(service.solicitar(any(Repuesto.class))).thenReturn(guardado);

        mockMvc.perform(post("/api/repuestos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entrada)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idRepuesto").value(1))
                .andExpect(jsonPath("$.estado").value("Solicitado"));
    }

    @Test
    void listar_devuelveTodosLosRepuestos() throws Exception {
        when(service.listar()).thenReturn(List.of(new Repuesto(), new Repuesto()));

        mockMvc.perform(get("/api/repuestos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void solicitados_devuelveLosRepuestosSolicitados() throws Exception {
        when(service.solicitados()).thenReturn(List.of(new Repuesto()));

        mockMvc.perform(get("/api/repuestos/solicitados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void entregados_devuelveLosRepuestosEntregados() throws Exception {
        when(service.entregados()).thenReturn(List.of(new Repuesto()));

        mockMvc.perform(get("/api/repuestos/entregados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void marcarEntregado_devuelveElRepuestoActualizado() throws Exception {
        Repuesto entregado = new Repuesto();
        entregado.setIdRepuesto(1);
        entregado.setEstado("Entregado");
        when(service.marcarEntregado(1)).thenReturn(entregado);

        mockMvc.perform(put("/api/repuestos/1/entregar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("Entregado"));
    }

    @Test
    void marcarEntregado_devuelve404CuandoNoExiste() throws Exception {
        when(service.marcarEntregado(99)).thenThrow(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Repuesto no encontrado con id: 99"));

        mockMvc.perform(put("/api/repuestos/99/entregar"))
                .andExpect(status().isNotFound());
    }
}
