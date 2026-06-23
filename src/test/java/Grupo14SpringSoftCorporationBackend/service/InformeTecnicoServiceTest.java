package Grupo14SpringSoftCorporationBackend.service;

import Grupo14SpringSoftCorporationBackend.model.InformeTecnico;
import Grupo14SpringSoftCorporationBackend.repository.InformeTecnicoRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InformeTecnicoServiceTest {

    @Mock
    private InformeTecnicoRepository repo;

    @InjectMocks
    private InformeTecnicoService service;

    @Test
    void registrar_fijaLaFechaDelInformeAntesDeGuardar() {
        InformeTecnico informe = new InformeTecnico();
        when(repo.save(any(InformeTecnico.class))).thenAnswer(inv -> inv.getArgument(0));

        InformeTecnico resultado = service.registrar(informe);

        assertThat(resultado.getFechaInforme()).isNotNull();
        verify(repo).save(informe);
    }

    @Test
    void listar_delegaEnElRepositorio() {
        when(repo.findAll()).thenReturn(List.of(new InformeTecnico(), new InformeTecnico()));

        List<InformeTecnico> resultado = service.listar();

        assertThat(resultado).hasSize(2);
        verify(repo).findAll();
    }

    @Test
    void buscarPorIncidencia_lanzaNotFoundCuandoNoExisteInforme() {
        when(repo.findByIdIncidencia(50)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorIncidencia(50))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Informe no encontrado");
    }

    @Test
    void buscarPorIncidencia_devuelveElInformeCuandoExiste() {
        InformeTecnico informe = new InformeTecnico();
        informe.setIdIncidencia(50);
        when(repo.findByIdIncidencia(50)).thenReturn(Optional.of(informe));

        InformeTecnico resultado = service.buscarPorIncidencia(50);

        assertThat(resultado.getIdIncidencia()).isEqualTo(50);
    }
}
