package Grupo14SpringSoftCorporationBackend.service;

import Grupo14SpringSoftCorporationBackend.model.RefreshToken;
import Grupo14SpringSoftCorporationBackend.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository repo;

    @InjectMocks
    private RefreshTokenService service;

    @BeforeEach
    void setUp() {
        // 7 dias en milisegundos, igual que el valor por defecto de @Value
        ReflectionTestUtils.setField(service, "refreshExpiration", 604800000L);
    }

    @Test
    void crear_eliminaLosTokensAnterioresDelUsuarioAntesDeGuardarUnoNuevo() {
        when(repo.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        RefreshToken resultado = service.crear(1);

        InOrder orden = inOrder(repo);
        orden.verify(repo).deleteByIdUsuario(1);
        orden.verify(repo).save(any(RefreshToken.class));

        assertThat(resultado.getToken()).isNotBlank();
        assertThat(resultado.getIdUsuario()).isEqualTo(1);
        assertThat(resultado.getRevocado()).isFalse();
        assertThat(resultado.getExpiryDate()).isAfter(Instant.now());
    }

    @Test
    void crear_generaTokensUnicosEnLlamadasSucesivas() {
        when(repo.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        RefreshToken primero = service.crear(1);
        RefreshToken segundo = service.crear(1);

        assertThat(primero.getToken()).isNotEqualTo(segundo.getToken());
    }

    @Test
    void crear_calculaLaFechaDeExpiracionSegunLaConfiguracion() {
        ReflectionTestUtils.setField(service, "refreshExpiration", 1000L); // 1 segundo
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        when(repo.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        Instant antes = Instant.now();
        service.crear(5);
        verify(repo).save(captor.capture());

        Instant expiracion = captor.getValue().getExpiryDate();
        assertThat(expiracion).isAfter(antes);
        assertThat(expiracion).isBefore(antes.plusMillis(5000));
    }

    @Test
    void validar_lanzaUnauthorizedCuandoElTokenNoExiste() {
        when(repo.findByToken("token-inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.validar("token-inexistente"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("no encontrado o inv");
    }

    @Test
    void validar_lanzaUnauthorizedCuandoElTokenEstaRevocado() {
        RefreshToken rt = new RefreshToken();
        rt.setToken("abc");
        rt.setRevocado(true);
        rt.setExpiryDate(Instant.now().plusSeconds(60));
        when(repo.findByToken("abc")).thenReturn(Optional.of(rt));

        assertThatThrownBy(() -> service.validar("abc"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("revocado");
    }

    @Test
    void validar_lanzaUnauthorizedYBorraElTokenCuandoEstaExpirado() {
        RefreshToken rt = new RefreshToken();
        rt.setToken("abc");
        rt.setRevocado(false);
        rt.setExpiryDate(Instant.now().minusSeconds(60));
        when(repo.findByToken("abc")).thenReturn(Optional.of(rt));

        assertThatThrownBy(() -> service.validar("abc"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("expirado");

        verify(repo).delete(rt);
    }

    @Test
    void validar_devuelveElTokenCuandoEsValidoNoRevocadoYNoExpirado() {
        RefreshToken rt = new RefreshToken();
        rt.setToken("abc");
        rt.setRevocado(false);
        rt.setExpiryDate(Instant.now().plusSeconds(60));
        when(repo.findByToken("abc")).thenReturn(Optional.of(rt));

        RefreshToken resultado = service.validar("abc");

        assertThat(resultado).isSameAs(rt);
        verify(repo, never()).delete(any());
    }

    @Test
    void revocarPorUsuario_delegaEnElRepositorio() {
        service.revocarPorUsuario(8);

        verify(repo).deleteByIdUsuario(8);
    }
}
