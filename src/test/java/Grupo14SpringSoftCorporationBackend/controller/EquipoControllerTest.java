package Grupo14SpringSoftCorporationBackend.controller;

import Grupo14SpringSoftCorporationBackend.repository.UsuarioRepository;
import Grupo14SpringSoftCorporationBackend.util.JwtUtil;
import Grupo14SpringSoftCorporationBackend.model.Equipo;
import Grupo14SpringSoftCorporationBackend.repository.EquipoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test de la capa web con MockMvc. Se desactivan los filtros de seguridad
 * (addFilters = false) porque este controlador no tiene reglas de
 * autorizacion propias (@PreAuthorize); solo se prueba el mapeo HTTP/JSON.
 */
@WebMvcTest(EquipoController.class)
@AutoConfigureMockMvc(addFilters = false)
class EquipoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // JwtAuthenticationFilter es recogido automaticamente por @WebMvcTest
    // (es un Filter), por lo que sus dependencias deben mockearse aunque
    // este controlador no tenga relacion directa con la seguridad.
    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private EquipoRepository repository;

    @Test
    void getAll_devuelveLaListaDeEquiposComoJson() throws Exception {
        Equipo e1 = new Equipo("EQ-01", "HP ProBook", "Piso 1", "Juan Perez");
        Equipo e2 = new Equipo("EQ-02", "Dell OptiPlex", "Piso 2", "Maria Lopez");
        when(repository.findAll()).thenReturn(List.of(e1, e2));

        mockMvc.perform(get("/api/equipos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].codigoEquipo").value("EQ-01"))
                .andExpect(jsonPath("$[0].marcaModelo").value("HP ProBook"))
                .andExpect(jsonPath("$[1].codigoEquipo").value("EQ-02"));
    }

    @Test
    void getAll_devuelveListaVaciaCuandoNoHayEquipos() throws Exception {
        when(repository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/equipos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
