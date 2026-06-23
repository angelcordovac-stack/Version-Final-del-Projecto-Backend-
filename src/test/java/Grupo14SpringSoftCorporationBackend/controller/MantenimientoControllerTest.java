package Grupo14SpringSoftCorporationBackend.controller;

import Grupo14SpringSoftCorporationBackend.repository.UsuarioRepository;
import Grupo14SpringSoftCorporationBackend.util.JwtUtil;
import Grupo14SpringSoftCorporationBackend.model.Usuario;
import Grupo14SpringSoftCorporationBackend.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas funcionales (sin seguridad) del controlador.
 * Toda la clase requiere rol JEFE en produccion (@PreAuthorize a nivel de
 * clase); esa restriccion se prueba en MantenimientoControllerSecurityTest.
 */
@WebMvcTest(MantenimientoController.class)
@AutoConfigureMockMvc(addFilters = false)
class MantenimientoControllerTest {

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
    private UsuarioService service;

    @Test
    void listarUsuarios_devuelveTodosLosUsuarios() throws Exception {
        when(service.listar()).thenReturn(List.of(new Usuario(), new Usuario()));

        mockMvc.perform(get("/mantenimiento/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void obtenerUsuario_devuelveElUsuarioCorrespondiente() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(5);
        when(service.buscar(5)).thenReturn(usuario);

        mockMvc.perform(get("/mantenimiento/usuarios/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value(5));
    }

    @Test
    void crearUsuario_fuerzaIdUsuarioNuloAntesDeGuardar() throws Exception {
        Usuario entrada = new Usuario();
        entrada.setIdUsuario(999); // el controlador debe ignorar este valor
        entrada.setCorreo("nuevo@correo.com");
        entrada.setNombreCompleto("Usuario Nuevo");
        entrada.setPasswordHash("123456");

        Usuario guardado = new Usuario();
        guardado.setIdUsuario(1);
        guardado.setCorreo("nuevo@correo.com");
        when(service.guardar(any(Usuario.class))).thenReturn(guardado);

        mockMvc.perform(post("/mantenimiento/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entrada)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value(1));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(service).guardar(captor.capture());
        assertThat(captor.getValue().getIdUsuario()).isNull();
    }

    @Test
    void actualizarUsuario_fijaElIdDelPathEnElUsuarioAGuardar() throws Exception {
        Usuario entrada = new Usuario();
        entrada.setCorreo("actualizado@correo.com");

        Usuario actualizado = new Usuario();
        actualizado.setIdUsuario(7);
        actualizado.setCorreo("actualizado@correo.com");
        when(service.guardar(any(Usuario.class))).thenReturn(actualizado);

        mockMvc.perform(put("/mantenimiento/usuarios/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entrada)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value(7));
    }

    @Test
    void eliminarUsuario_devuelveMensajeDeConfirmacion() throws Exception {
        mockMvc.perform(delete("/mantenimiento/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Usuario eliminado correctamente"));

        verify(service).eliminar(1);
    }
}
