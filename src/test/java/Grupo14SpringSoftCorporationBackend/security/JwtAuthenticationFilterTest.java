package Grupo14SpringSoftCorporationBackend.security;

import Grupo14SpringSoftCorporationBackend.model.Usuario;
import Grupo14SpringSoftCorporationBackend.repository.UsuarioRepository;
import Grupo14SpringSoftCorporationBackend.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para JwtAuthenticationFilter.
 * doFilterInternal es 'protected', se invoca directamente porque
 * la clase de test esta en el mismo paquete.
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @AfterEach
    void limpiarContextoDeSeguridad() {
        SecurityContextHolder.clearContext();
    }

    private Usuario usuarioActivo(int idPerfil) {
        Usuario u = new Usuario();
        u.setIdUsuario(1);
        u.setCorreo("usuario@correo.com");
        u.setActivo(true);
        u.setIdPerfil(idPerfil);
        return u;
    }

    @Test
    void doFilterInternal_sinHeaderAuthorization_continuaSinAutenticarYSinTocarJwtUtil() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtUtil);
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void doFilterInternal_conHeaderQueNoEsBearer_continuaSinAutenticar() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic abc123");

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void doFilterInternal_tokenValidoYUsuarioActivo_autenticaYContinuaLaCadena() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token123");
        when(jwtUtil.extractEmail("token123")).thenReturn("usuario@correo.com");
        when(usuarioRepository.findByCorreo("usuario@correo.com"))
                .thenReturn(Optional.of(usuarioActivo(1)));
        when(jwtUtil.isTokenValid("token123")).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getAuthorities()).extracting("authority").containsExactly("ROLE_JEFE");
        verify(filterChain).doFilter(request, response);
    }

    @ParameterizedTest
    @CsvSource({
            "1, ROLE_JEFE",
            "2, ROLE_TECNICO",
            "3, ROLE_SISTEMAS",
            "4, ROLE_USUARIO",
            "0, ROLE_USUARIO"
    })
    void doFilterInternal_asignaElRolCorrectoSegunElIdPerfil(int idPerfil, String rolEsperado) throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token123");
        when(jwtUtil.extractEmail("token123")).thenReturn("usuario@correo.com");
        when(usuarioRepository.findByCorreo("usuario@correo.com"))
                .thenReturn(Optional.of(usuarioActivo(idPerfil)));
        when(jwtUtil.isTokenValid("token123")).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth.getAuthorities()).extracting("authority").containsExactly(rolEsperado);
    }

    @Test
    void doFilterInternal_usuarioInactivo_noAutenticaPeroContinuaLaCadena() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token123");
        when(jwtUtil.extractEmail("token123")).thenReturn("usuario@correo.com");
        Usuario inactivo = usuarioActivo(1);
        inactivo.setActivo(false);
        when(usuarioRepository.findByCorreo("usuario@correo.com")).thenReturn(Optional.of(inactivo));

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_usuarioNoEncontradoPorCorreo_noAutenticaPeroContinuaLaCadena() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token123");
        when(jwtUtil.extractEmail("token123")).thenReturn("inexistente@correo.com");
        when(usuarioRepository.findByCorreo("inexistente@correo.com")).thenReturn(Optional.empty());

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).isTokenValid(any());
    }

    @Test
    void doFilterInternal_tokenInvalido_noAutenticaPeroContinuaLaCadena() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token123");
        when(jwtUtil.extractEmail("token123")).thenReturn("usuario@correo.com");
        when(usuarioRepository.findByCorreo("usuario@correo.com"))
                .thenReturn(Optional.of(usuarioActivo(1)));
        when(jwtUtil.isTokenValid("token123")).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_emailNuloExtraidoDelToken_noAutenticaPeroContinuaLaCadena() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token123");
        when(jwtUtil.extractEmail("token123")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void doFilterInternal_siYaExisteUnaAutenticacionPrevia_noLaSobrescribe() throws Exception {
        Authentication autenticacionPrevia = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(autenticacionPrevia);

        when(request.getHeader("Authorization")).thenReturn("Bearer token123");
        when(jwtUtil.extractEmail("token123")).thenReturn("usuario@correo.com");

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(autenticacionPrevia);
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(usuarioRepository);
    }
}
