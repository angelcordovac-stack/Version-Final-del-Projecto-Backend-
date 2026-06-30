package Grupo14SpringSoftCorporationBackend.controller;

import Grupo14SpringSoftCorporationBackend.model.RefreshToken;
import Grupo14SpringSoftCorporationBackend.model.Usuario;
import Grupo14SpringSoftCorporationBackend.repository.UsuarioRepository;
import Grupo14SpringSoftCorporationBackend.service.RefreshTokenService;
import Grupo14SpringSoftCorporationBackend.service.UsuarioService;
import Grupo14SpringSoftCorporationBackend.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(UsuarioController.class)
@AutoConfigureMockMvc(addFilters = false)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService service;

    @MockitoBean
    private RefreshTokenService refreshTokenService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    // ---------- login ----------

    @Test
    void login_devuelve200ConElTokenCuandoLasCredencialesSonCorrectas() throws Exception {

        Map<String, Object> respuestaServicio = Map.of(
                "token", "access-token",
                "refreshToken", "refresh-token",
                "idUsuario", 1
        );
        when(service.login("usuario@correo.com", "123456")).thenReturn(respuestaServicio);

        mockMvc.perform(post("/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\": \"usuario@correo.com\", \"password\": \"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void login_devuelve401ConMensajeGenericoCuandoLasCredencialesSonIncorrectas() throws Exception {
        when(service.login("usuario@correo.com", "incorrecta")).thenReturn(null);

        mockMvc.perform(post("/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\": \"usuario@correo.com\", \"password\": \"incorrecta\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Credenciales invalidas"));
    }


    @Test
    void refresh_devuelve400CuandoNoSeEnviaElRefreshToken() throws Exception {
        mockMvc.perform(post("/usuarios/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El refreshToken es requerido"));

        verifyNoInteractions(refreshTokenService);
    }

    @Test
    void refresh_devuelve400CuandoElRefreshTokenEsEnBlanco() throws Exception {
        mockMvc.perform(post("/usuarios/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\": \"   \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refresh_propagaElErrorDelServicioCuandoElTokenEsInvalido() throws Exception {
        when(refreshTokenService.validar("token-invalido"))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token no encontrado o inválido"));

        mockMvc.perform(post("/usuarios/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\": \"token-invalido\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_devuelve401CuandoElUsuarioAsociadoNoExiste() throws Exception {
        RefreshToken rt = new RefreshToken();
        rt.setToken("token-valido");
        rt.setIdUsuario(1);
        when(refreshTokenService.validar("token-valido")).thenReturn(rt);
        when(usuarioRepository.findById(1)).thenReturn(Optional.empty());

        mockMvc.perform(post("/usuarios/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\": \"token-valido\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Usuario no encontrado o inactivo"));
    }

    @Test
    void refresh_devuelve401CuandoElUsuarioAsociadoEstaInactivo() throws Exception {
        RefreshToken rt = new RefreshToken();
        rt.setToken("token-valido");
        rt.setIdUsuario(1);
        Usuario inactivo = new Usuario();
        inactivo.setIdUsuario(1);
        inactivo.setActivo(false);

        when(refreshTokenService.validar("token-valido")).thenReturn(rt);
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(inactivo));

        mockMvc.perform(post("/usuarios/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\": \"token-valido\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_devuelveUnNuevoAccessTokenYRefreshTokenCuandoTodoEsValido() throws Exception {
        RefreshToken rt = new RefreshToken();
        rt.setToken("token-valido");
        rt.setIdUsuario(1);
        Usuario activo = new Usuario();
        activo.setIdUsuario(1);
        activo.setCorreo("usuario@correo.com");
        activo.setIdPerfil(1);
        activo.setActivo(true);

        RefreshToken nuevoRt = new RefreshToken();
        nuevoRt.setToken("nuevo-refresh-token");

        when(refreshTokenService.validar("token-valido")).thenReturn(rt);
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(activo));
        when(jwtUtil.generateToken("usuario@correo.com", 1, 1)).thenReturn("nuevo-access-token");
        when(refreshTokenService.crear(1)).thenReturn(nuevoRt);

        mockMvc.perform(post("/usuarios/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\": \"token-valido\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("nuevo-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("nuevo-refresh-token"));
    }



    @Test
    void logout_revocaElRefreshTokenCuandoEsValido() throws Exception {
        RefreshToken rt = new RefreshToken();
        rt.setToken("token-valido");
        rt.setIdUsuario(1);
        when(refreshTokenService.validar("token-valido")).thenReturn(rt);

        mockMvc.perform(post("/usuarios/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\": \"token-valido\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Sesión cerrada correctamente"));

        verify(refreshTokenService).revocarPorUsuario(1);
    }

    @Test
    void logout_noFallaCuandoElTokenYaEsInvalido() throws Exception {
        when(refreshTokenService.validar("token-invalido"))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalido"));

        mockMvc.perform(post("/usuarios/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\": \"token-invalido\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Sesión cerrada correctamente"));

        verify(refreshTokenService, never()).revocarPorUsuario(any());
    }

    @Test
    void logout_respondeOkAunSinEnviarRefreshToken() throws Exception {
        mockMvc.perform(post("/usuarios/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        verifyNoInteractions(refreshTokenService);
    }
}
