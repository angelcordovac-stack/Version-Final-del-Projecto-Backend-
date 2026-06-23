package Grupo14SpringSoftCorporationBackend.controller;

import Grupo14SpringSoftCorporationBackend.repository.UsuarioRepository;
import Grupo14SpringSoftCorporationBackend.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DevHelperController.class)
@AutoConfigureMockMvc(addFilters = false)
class DevHelperControllerTest {

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
    private PasswordEncoder passwordEncoder;

    @Test
    void hash_devuelveElHashGeneradoPorElPasswordEncoder() throws Exception {
        when(passwordEncoder.encode("123456")).thenReturn("$2a$10$hashDePrueba");

        mockMvc.perform(get("/dev/hash").param("password", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hash").value("$2a$10$hashDePrueba"));

        verify(passwordEncoder).encode("123456");
    }

    @Test
    void hash_devuelve400CuandoFaltaElParametroPassword() throws Exception {
        mockMvc.perform(get("/dev/hash"))
                .andExpect(status().isBadRequest());
    }
}
