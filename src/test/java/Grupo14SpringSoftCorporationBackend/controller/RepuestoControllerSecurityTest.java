package Grupo14SpringSoftCorporationBackend.controller;

import Grupo14SpringSoftCorporationBackend.model.Repuesto;
import Grupo14SpringSoftCorporationBackend.repository.UsuarioRepository;
import Grupo14SpringSoftCorporationBackend.security.JwtAuthenticationFilter;
import Grupo14SpringSoftCorporationBackend.security.SecurityConfig;
import Grupo14SpringSoftCorporationBackend.service.RepuestoService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * - solicitar() requiere rol TECNICO o JEFE.
 * - marcarEntregado() requiere rol SISTEMAS o JEFE.
 */
@WebMvcTest(RepuestoController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class RepuestoControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RepuestoService service;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    @WithMockUser(roles = "TECNICO")
    void solicitar_permitidoParaRolTecnico() throws Exception {
        when(service.solicitar(any(Repuesto.class))).thenReturn(new Repuesto());

        mockMvc.perform(post("/api/repuestos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SISTEMAS")
    void solicitar_prohibidoParaRolSistemas() throws Exception {
        mockMvc.perform(post("/api/repuestos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SISTEMAS")
    void marcarEntregado_permitidoParaRolSistemas() throws Exception {
        when(service.marcarEntregado(any())).thenReturn(new Repuesto());

        mockMvc.perform(put("/api/repuestos/1/entregar"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "JEFE")
    void marcarEntregado_permitidoParaRolJefe() throws Exception {
        when(service.marcarEntregado(any())).thenReturn(new Repuesto());

        mockMvc.perform(put("/api/repuestos/1/entregar"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "TECNICO")
    void marcarEntregado_prohibidoParaRolTecnico() throws Exception {
        mockMvc.perform(put("/api/repuestos/1/entregar"))
                .andExpect(status().isForbidden());
    }
}
