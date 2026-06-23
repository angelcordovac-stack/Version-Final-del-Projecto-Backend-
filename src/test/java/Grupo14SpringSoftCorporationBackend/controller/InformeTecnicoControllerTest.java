package Grupo14SpringSoftCorporationBackend.controller;

import Grupo14SpringSoftCorporationBackend.repository.UsuarioRepository;
import Grupo14SpringSoftCorporationBackend.util.JwtUtil;
import Grupo14SpringSoftCorporationBackend.model.InformeTecnico;
import Grupo14SpringSoftCorporationBackend.service.InformeTecnicoService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InformeTecnicoController.class)
@AutoConfigureMockMvc(addFilters = false)
class InformeTecnicoControllerTest {

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
    private InformeTecnicoService service;

    @Test
    void registrar_devuelveElInformeCreado() throws Exception {
        InformeTecnico informe = new InformeTecnico();
        informe.setIdIncidencia(1);
        informe.setDiagnostico("Falla en la placa madre");

        InformeTecnico guardado = new InformeTecnico();
        guardado.setIdInforme(10);
        guardado.setIdIncidencia(1);
        guardado.setDiagnostico("Falla en la placa madre");

        when(service.registrar(any(InformeTecnico.class))).thenReturn(guardado);

        mockMvc.perform(post("/api/informes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(informe)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idInforme").value(10))
                .andExpect(jsonPath("$.diagnostico").value("Falla en la placa madre"));
    }

    @Test
    void listar_devuelveTodosLosInformes() throws Exception {
        when(service.listar()).thenReturn(List.of(new InformeTecnico(), new InformeTecnico()));

        mockMvc.perform(get("/api/informes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void buscarPorIncidencia_devuelveElInformeCuandoExiste() throws Exception {
        InformeTecnico informe = new InformeTecnico();
        informe.setIdIncidencia(5);
        when(service.buscarPorIncidencia(5)).thenReturn(informe);

        mockMvc.perform(get("/api/informes/incidencia/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idIncidencia").value(5));
    }

    @Test
    void buscarPorIncidencia_devuelve404CuandoNoExiste() throws Exception {
        when(service.buscarPorIncidencia(99)).thenThrow(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Informe no encontrado para la incidencia: 99"));

        mockMvc.perform(get("/api/informes/incidencia/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Informe no encontrado para la incidencia: 99"));
    }
}
