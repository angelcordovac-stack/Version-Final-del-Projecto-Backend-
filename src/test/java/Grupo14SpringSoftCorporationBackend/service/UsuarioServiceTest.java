package Grupo14SpringSoftCorporationBackend.service;

import Grupo14SpringSoftCorporationBackend.model.RefreshToken;
import Grupo14SpringSoftCorporationBackend.model.Usuario;
import Grupo14SpringSoftCorporationBackend.repository.TecnicoRepository;
import Grupo14SpringSoftCorporationBackend.repository.UsuarioRepository;
import Grupo14SpringSoftCorporationBackend.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repo;

    @Mock
    private TecnicoRepository tecnicoRepo;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private UsuarioService service;

    private Usuario usuarioActivo() {
        Usuario u = new Usuario();
        u.setIdUsuario(1);
        u.setCorreo("usuario@correo.com");
        u.setPasswordHash("$2a$10$hashFalsoDePrueba");
        u.setNombreCompleto("Ana Torres");
        u.setActivo(true);
        u.setIdPerfil(1);
        return u;
    }

    // ---------- login ----------

    @Test
    void login_devuelveNullCuandoElCorreoNoExiste() {
        when(repo.findByCorreo("no-existe@correo.com")).thenReturn(Optional.empty());

        Map<String, Object> resultado = service.login("no-existe@correo.com", "123456");

        assertThat(resultado).isNull();
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void login_devuelveNullCuandoElUsuarioEstaInactivo() {
        Usuario inactivo = usuarioActivo();
        inactivo.setActivo(false);
        when(repo.findByCorreo(inactivo.getCorreo())).thenReturn(Optional.of(inactivo));

        Map<String, Object> resultado = service.login(inactivo.getCorreo(), "123456");

        assertThat(resultado).isNull();
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void login_devuelveNullCuandoLaPasswordNoCoincide() {
        Usuario usuario = usuarioActivo();
        when(repo.findByCorreo(usuario.getCorreo())).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("incorrecta", usuario.getPasswordHash())).thenReturn(false);

        Map<String, Object> resultado = service.login(usuario.getCorreo(), "incorrecta");

        assertThat(resultado).isNull();
        verify(jwtUtil, never()).generateToken(any(), any(), any());
    }

    @Test
    void login_devuelveTokenYDatosDelUsuarioCuandoLasCredencialesSonCorrectas() {
        Usuario usuario = usuarioActivo();
        when(repo.findByCorreo(usuario.getCorreo())).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("123456", usuario.getPasswordHash())).thenReturn(true);
        when(jwtUtil.generateToken(usuario.getCorreo(), usuario.getIdUsuario(), usuario.getIdPerfil()))
                .thenReturn("access-token-123");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token-456");
        when(refreshTokenService.crear(usuario.getIdUsuario())).thenReturn(refreshToken);

        Map<String, Object> resultado = service.login(usuario.getCorreo(), "123456");

        assertThat(resultado).isNotNull();
        assertThat(resultado.get("token")).isEqualTo("access-token-123");
        assertThat(resultado.get("refreshToken")).isEqualTo("refresh-token-456");
        assertThat(resultado.get("idUsuario")).isEqualTo(1);
        assertThat(resultado.get("nombreCompleto")).isEqualTo("Ana Torres");
        assertThat(resultado.get("correo")).isEqualTo("usuario@correo.com");
        assertThat(resultado.get("activo")).isEqualTo(true);
        assertThat(resultado.get("perfil")).isEqualTo("Jefe");
    }

    @ParameterizedTest
    @CsvSource({
            "1, Jefe",
            "2, Tecnico",
            "3, Sistemas",
            "4, Usuario",
            "99, Usuario"
    })
    void login_traduceElIdPerfilAlNombreDePerfilCorrespondiente(int idPerfil, String nombrePerfilEsperado) {
        Usuario usuario = usuarioActivo();
        usuario.setIdPerfil(idPerfil);
        when(repo.findByCorreo(usuario.getCorreo())).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(any(), any(), any())).thenReturn("token");
        when(refreshTokenService.crear(any())).thenReturn(new RefreshToken());

        Map<String, Object> resultado = service.login(usuario.getCorreo(), "123456");

        assertThat(resultado.get("perfil")).isEqualTo(nombrePerfilEsperado);
    }

    // ---------- listar / buscar ----------

    @Test
    void listar_delegaEnElRepositorio() {
        when(repo.findAll()).thenReturn(List.of(new Usuario(), new Usuario()));

        List<Usuario> resultado = service.listar();

        assertThat(resultado).hasSize(2);
    }

    @Test
    void buscar_lanzaNotFoundCuandoNoExisteElId() {
        when(repo.findById(50)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscar(50))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Usuario no encontrado con id");
    }

    @Test
    void buscar_devuelveElUsuarioCuandoExiste() {
        Usuario usuario = usuarioActivo();
        when(repo.findById(1)).thenReturn(Optional.of(usuario));

        Usuario resultado = service.buscar(1);

        assertThat(resultado).isSameAs(usuario);
    }

    @Test
    void buscarPorCorreo_lanzaNotFoundCuandoNoExiste() {
        when(repo.findByCorreo("ausente@correo.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorCorreo("ausente@correo.com"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Usuario no encontrado con correo");
    }

    @Test
    void buscarPorCorreo_devuelveElUsuarioCuandoExiste() {
        Usuario usuario = usuarioActivo();
        when(repo.findByCorreo(usuario.getCorreo())).thenReturn(Optional.of(usuario));

        Usuario resultado = service.buscarPorCorreo(usuario.getCorreo());

        assertThat(resultado).isSameAs(usuario);
    }

    // ---------- guardar ----------

    @Test
    void guardar_lanzaBadRequestCuandoElCorreoYaExisteAlCrear() {
        Usuario nuevo = new Usuario();
        nuevo.setCorreo("repetido@correo.com");
        nuevo.setPasswordHash("123456");

        Usuario existente = new Usuario();
        existente.setIdUsuario(99);
        when(repo.findByCorreo("repetido@correo.com")).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.guardar(nuevo))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Ya existe un usuario con el correo");

        verify(repo, never()).save(any());
    }

    @Test
    void guardar_lanzaBadRequestCuandoElCorreoPerteneceAOtroUsuarioAlActualizar() {
        Usuario aActualizar = new Usuario();
        aActualizar.setIdUsuario(1);
        aActualizar.setCorreo("repetido@correo.com");

        Usuario otroConEseCorreo = new Usuario();
        otroConEseCorreo.setIdUsuario(2);
        when(repo.findByCorreo("repetido@correo.com")).thenReturn(Optional.of(otroConEseCorreo));

        assertThatThrownBy(() -> service.guardar(aActualizar))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Ya existe otro usuario con el correo");
    }

    @Test
    void guardar_permiteActualizarCuandoElCorreoPerteneceAlMismoUsuario() {
        Usuario aActualizar = new Usuario();
        aActualizar.setIdUsuario(1);
        aActualizar.setCorreo("mismo@correo.com");
        aActualizar.setPasswordHash(null); // no cambia password
        aActualizar.setIdPerfil(1);

        Usuario existenteEnBd = usuarioActivo();
        existenteEnBd.setIdUsuario(1);
        existenteEnBd.setPasswordHash("$2a$10$hashOriginal");

        when(repo.findByCorreo("mismo@correo.com")).thenReturn(Optional.of(aActualizar));
        when(repo.findById(1)).thenReturn(Optional.of(existenteEnBd));
        when(repo.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario resultado = service.guardar(aActualizar);

        assertThat(resultado.getPasswordHash()).isEqualTo("$2a$10$hashOriginal");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void guardar_hasheaLaPasswordCuandoVieneEnTextoPlanoAlCrear() {
        Usuario nuevo = new Usuario();
        nuevo.setCorreo("nuevo@correo.com");
        nuevo.setPasswordHash("miPasswordPlano");
        nuevo.setIdPerfil(1);

        when(repo.findByCorreo("nuevo@correo.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("miPasswordPlano")).thenReturn("$2a$10$hasheada");
        when(repo.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario resultado = service.guardar(nuevo);

        assertThat(resultado.getPasswordHash()).isEqualTo("$2a$10$hasheada");
        verify(passwordEncoder).encode("miPasswordPlano");
    }

    @Test
    void guardar_noVuelveAHashearUnaPasswordQueYaEsUnHashBCrypt() {
        Usuario nuevo = new Usuario();
        nuevo.setCorreo("nuevo@correo.com");
        nuevo.setPasswordHash("$2b$10$yaEstaHasheada");
        nuevo.setIdPerfil(1);

        when(repo.findByCorreo("nuevo@correo.com")).thenReturn(Optional.empty());
        when(repo.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario resultado = service.guardar(nuevo);

        assertThat(resultado.getPasswordHash()).isEqualTo("$2b$10$yaEstaHasheada");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void guardar_lanzaBadRequestCuandoNoSeEnviaPasswordAlCrear() {
        Usuario nuevo = new Usuario();
        nuevo.setCorreo("nuevo@correo.com");
        nuevo.setPasswordHash(null);

        when(repo.findByCorreo("nuevo@correo.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.guardar(nuevo))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("La contrasena es obligatoria al crear un usuario");

        verify(repo, never()).save(any());
    }

    @Test
    void guardar_mantieneLaPasswordExistenteAlActualizarSinEnviarUnaNueva() {
        Usuario aActualizar = new Usuario();
        aActualizar.setIdUsuario(1);
        aActualizar.setCorreo("usuario@correo.com");
        aActualizar.setPasswordHash("");
        aActualizar.setIdPerfil(1);

        Usuario existenteEnBd = usuarioActivo();
        existenteEnBd.setPasswordHash("$2a$10$hashOriginalGuardado");

        when(repo.findByCorreo("usuario@correo.com")).thenReturn(Optional.empty());
        when(repo.findById(1)).thenReturn(Optional.of(existenteEnBd));
        when(repo.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario resultado = service.guardar(aActualizar);

        assertThat(resultado.getPasswordHash()).isEqualTo("$2a$10$hashOriginalGuardado");
    }

    // ---------- eliminar ----------

    @Test
    void eliminar_lanzaNotFoundCuandoElUsuarioNoExiste() {
        when(repo.existsById(50)).thenReturn(false);

        assertThatThrownBy(() -> service.eliminar(50))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("No existe el usuario con id");

        verify(repo, never()).deleteById(any());
    }

    @Test
    void eliminar_borraElUsuarioCuandoExiste() {
        when(repo.existsById(1)).thenReturn(true);

        service.eliminar(1);

        verify(repo).deleteById(1);
    }
}
