package Grupo14SpringSoftCorporationBackend.controller;

import Grupo14SpringSoftCorporationBackend.model.Usuario;
import Grupo14SpringSoftCorporationBackend.repository.UsuarioRepository;
import Grupo14SpringSoftCorporationBackend.security.JwtAuthenticationFilter;
import Grupo14SpringSoftCorporationBackend.security.SecurityConfig;
import Grupo14SpringSoftCorporationBackend.service.UsuarioService;
import Grupo14SpringSoftCorporationBackend.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Todo MantenimientoController esta anotado con @PreAuthorize("hasRole('JEFE')") a nivel de clase. */
@WebMvcTest(MantenimientoController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class MantenimientoControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService service;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    @WithMockUser(roles = "JEFE")
    void listarUsuarios_permitidoParaRolJefe() throws Exception {
        when(service.listar()).thenReturn(List.of(new Usuario()));

        mockMvc.perform(get("/mantenimiento/usuarios"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "TECNICO")
    void listarUsuarios_prohibidoParaRolTecnico() throws Exception {
        mockMvc.perform(get("/mantenimiento/usuarios"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SISTEMAS")
    void listarUsuarios_prohibidoParaRolSistemas() throws Exception {
        mockMvc.perform(get("/mantenimiento/usuarios"))
                .andExpect(status().isForbidden());
    }

    @Test
    void listarUsuarios_rechazadoSinAutenticar() throws Exception {
        mockMvc.perform(get("/mantenimiento/usuarios"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(roles = "JEFE")
    void obtenerUsuario_permitidoParaRolJefe() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1);
        when(service.buscar(1)).thenReturn(usuario);

        mockMvc.perform(get("/mantenimiento/usuarios/1"))
                .andExpect(status().isOk());
    }
}
