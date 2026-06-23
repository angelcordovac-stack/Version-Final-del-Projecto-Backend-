package Grupo14SpringSoftCorporationBackend.controller;

import Grupo14SpringSoftCorporationBackend.repository.UsuarioRepository;
import Grupo14SpringSoftCorporationBackend.util.JwtUtil;
import Grupo14SpringSoftCorporationBackend.service.TecnicoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TecnicoController.class)
@AutoConfigureMockMvc(addFilters = false)
class TecnicoControllerTest {

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
    private TecnicoService service;

    @Test
    void listarDisponibles_devuelveLaListaDeTecnicosConSusDatos() throws Exception {
        Map<String, Object> tecnico = Map.of(
                "idUsuario", 1,
                "nombre", "Pedro Gomez",
                "especialidad", "Redes",
                "disponibilidad", true,
                "maxIncidencias", 5
        );
        when(service.listarTodosConNombre()).thenReturn(List.of(tecnico));

        mockMvc.perform(get("/api/tecnicos/disponibles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Pedro Gomez"))
                .andExpect(jsonPath("$[0].especialidad").value("Redes"))
                .andExpect(jsonPath("$[0].disponibilidad").value(true));
    }

    @Test
    void listarDisponibles_devuelveListaVaciaCuandoNoHayTecnicos() throws Exception {
        when(service.listarTodosConNombre()).thenReturn(List.of());

        mockMvc.perform(get("/api/tecnicos/disponibles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
