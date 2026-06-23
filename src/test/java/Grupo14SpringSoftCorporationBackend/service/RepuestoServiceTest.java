package Grupo14SpringSoftCorporationBackend.service;

import Grupo14SpringSoftCorporationBackend.model.Repuesto;
import Grupo14SpringSoftCorporationBackend.repository.RepuestoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepuestoServiceTest {

    @Mock
    private RepuestoRepository repo;

    @InjectMocks
    private RepuestoService service;

    @Test
    void solicitar_fijaEstadoSolicitadoYFechaDeSolicitud() {
        Repuesto repuesto = new Repuesto();
        when(repo.save(any(Repuesto.class))).thenAnswer(inv -> inv.getArgument(0));

        Repuesto resultado = service.solicitar(repuesto);

        assertThat(resultado.getEstado()).isEqualTo("Solicitado");
        assertThat(resultado.getFechaSolicitud()).isNotNull();
        verify(repo).save(repuesto);
    }

    @Test
    void listar_delegaEnElRepositorio() {
        when(repo.findAll()).thenReturn(List.of(new Repuesto(), new Repuesto()));

        List<Repuesto> resultado = service.listar();

        assertThat(resultado).hasSize(2);
    }

    @Test
    void solicitados_delegaEnElRepositorioConElEstadoSolicitado() {
        when(repo.findByEstado("Solicitado")).thenReturn(List.of(new Repuesto()));

        List<Repuesto> resultado = service.solicitados();

        assertThat(resultado).hasSize(1);
        verify(repo).findByEstado("Solicitado");
    }

    @Test
    void entregados_delegaEnElRepositorioConElEstadoEntregado() {
        when(repo.findByEstado("Entregado")).thenReturn(List.of(new Repuesto()));

        List<Repuesto> resultado = service.entregados();

        assertThat(resultado).hasSize(1);
        verify(repo).findByEstado("Entregado");
    }

    @Test
    void marcarEntregado_lanzaNotFoundCuandoElRepuestoNoExiste() {
        when(repo.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.marcarEntregado(1))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Repuesto no encontrado");

        verify(repo, never()).save(any());
    }

    @Test
    void marcarEntregado_actualizaEstadoYFechaDeEntrega() {
        Repuesto repuesto = new Repuesto();
        repuesto.setIdRepuesto(1);
        repuesto.setEstado("Solicitado");
        when(repo.findById(1)).thenReturn(Optional.of(repuesto));
        when(repo.save(any(Repuesto.class))).thenAnswer(inv -> inv.getArgument(0));

        Repuesto resultado = service.marcarEntregado(1);

        assertThat(resultado.getEstado()).isEqualTo("Entregado");
        assertThat(resultado.getFechaEntrega()).isNotNull();
    }
}
