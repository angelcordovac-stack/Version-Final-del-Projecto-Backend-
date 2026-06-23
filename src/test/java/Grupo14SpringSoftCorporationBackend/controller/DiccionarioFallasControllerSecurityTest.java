package Grupo14SpringSoftCorporationBackend.controller;

import Grupo14SpringSoftCorporationBackend.model.DiccionarioFallas;
import Grupo14SpringSoftCorporationBackend.repository.UsuarioRepository;
import Grupo14SpringSoftCorporationBackend.security.JwtAuthenticationFilter;
import Grupo14SpringSoftCorporationBackend.security.SecurityConfig;
import Grupo14SpringSoftCorporationBackend.service.DiccionarioFallasService;
import Grupo14SpringSoftCorporationBackend.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
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

/**
 * Verifica las reglas de autorizacion (@PreAuthorize) reales del controlador,
 * importando la configuracion de seguridad de la aplicacion.
 * Solo TECNICO y SISTEMAS pueden registrar una falla.
 */
@WebMvcTest(DiccionarioFallasController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class DiccionarioFallasControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private DiccionarioFallasService service;

    // Dependencias de JwtAuthenticationFilter, no se usan en estos tests
    // porque no se envia header Authorization (la autenticacion la simula @WithMockUser)
    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    private String body() throws Exception {
        DiccionarioFallas falla = new DiccionarioFallas();
        falla.setEstado("CRITICO");
        return objectMapper.writeValueAsString(falla);
    }

    @Test
    @WithMockUser(roles = "TECNICO")
    void registrar_permitidoParaRolTecnico() throws Exception {
        when(service.registrar(any(DiccionarioFallas.class))).thenReturn(new DiccionarioFallas());

        mockMvc.perform(post("/api/fallas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SISTEMAS")
    void registrar_permitidoParaRolSistemas() throws Exception {
        when(service.registrar(any(DiccionarioFallas.class))).thenReturn(new DiccionarioFallas());

        mockMvc.perform(post("/api/fallas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "JEFE")
    void registrar_prohibidoParaRolJefe() throws Exception {
        mockMvc.perform(post("/api/fallas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USUARIO")
    void registrar_prohibidoParaRolUsuarioGenerico() throws Exception {
        mockMvc.perform(post("/api/fallas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body()))
                .andExpect(status().isForbidden());
    }

    @Test
    void registrar_rechazadoCuandoNoHayUsuarioAutenticado() throws Exception {
        mockMvc.perform(post("/api/fallas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body()))
                .andExpect(status().is4xxClientError());
    }
}
