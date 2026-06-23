package Grupo14SpringSoftCorporationBackend.controller;

import Grupo14SpringSoftCorporationBackend.model.InformeTecnico;
import Grupo14SpringSoftCorporationBackend.repository.UsuarioRepository;
import Grupo14SpringSoftCorporationBackend.security.JwtAuthenticationFilter;
import Grupo14SpringSoftCorporationBackend.security.SecurityConfig;
import Grupo14SpringSoftCorporationBackend.service.InformeTecnicoService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** registrar() requiere rol TECNICO o JEFE. */
@WebMvcTest(InformeTecnicoController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class InformeTecnicoControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InformeTecnicoService service;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    @WithMockUser(roles = "TECNICO")
    void registrar_permitidoParaRolTecnico() throws Exception {
        when(service.registrar(any(InformeTecnico.class))).thenReturn(new InformeTecnico());

        mockMvc.perform(post("/api/informes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "JEFE")
    void registrar_permitidoParaRolJefe() throws Exception {
        when(service.registrar(any(InformeTecnico.class))).thenReturn(new InformeTecnico());

        mockMvc.perform(post("/api/informes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SISTEMAS")
    void registrar_prohibidoParaRolSistemas() throws Exception {
        mockMvc.perform(post("/api/informes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void registrar_rechazadoSinAutenticar() throws Exception {
        mockMvc.perform(post("/api/informes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().is4xxClientError());
    }
}
