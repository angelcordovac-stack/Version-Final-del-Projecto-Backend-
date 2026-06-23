package Grupo14SpringSoftCorporationBackend.controller;

import Grupo14SpringSoftCorporationBackend.model.Incidencia;
import Grupo14SpringSoftCorporationBackend.repository.UsuarioRepository;
import Grupo14SpringSoftCorporationBackend.security.JwtAuthenticationFilter;
import Grupo14SpringSoftCorporationBackend.security.SecurityConfig;
import Grupo14SpringSoftCorporationBackend.service.IncidenciaService;
import Grupo14SpringSoftCorporationBackend.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * - /asignar requiere rol JEFE.
 * - /solucionar requiere rol TECNICO o JEFE.
 * Los endpoints de solo lectura no tienen @PreAuthorize propio, asi que no
 * se repiten aqui (ver IncidenciaControllerTest para el comportamiento funcional).
 */
@WebMvcTest(IncidenciaController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class IncidenciaControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IncidenciaService service;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    @WithMockUser(roles = "JEFE")
    void asignarTecnico_permitidoParaRolJefe() throws Exception {
        when(service.asignarTecnico(any(), any())).thenReturn(new Incidencia());

        mockMvc.perform(put("/api/incidencias/1/asignar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idTecnico\": 7}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "TECNICO")
    void asignarTecnico_prohibidoParaRolTecnico() throws Exception {
        mockMvc.perform(put("/api/incidencias/1/asignar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idTecnico\": 7}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void asignarTecnico_rechazadoSinAutenticar() throws Exception {
        mockMvc.perform(put("/api/incidencias/1/asignar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idTecnico\": 7}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(roles = "TECNICO")
    void solucionar_permitidoParaRolTecnico() throws Exception {
        when(service.solucionar(any(), any())).thenReturn(new Incidencia());

        mockMvc.perform(put("/api/incidencias/1/solucionar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipoSolucion\": \"Reparacion\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "JEFE")
    void solucionar_permitidoParaRolJefe() throws Exception {
        when(service.solucionar(any(), any())).thenReturn(new Incidencia());

        mockMvc.perform(put("/api/incidencias/1/solucionar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipoSolucion\": \"Reparacion\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SISTEMAS")
    void solucionar_prohibidoParaRolSistemas() throws Exception {
        mockMvc.perform(put("/api/incidencias/1/solucionar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipoSolucion\": \"Reparacion\"}"))
                .andExpect(status().isForbidden());
    }
}
