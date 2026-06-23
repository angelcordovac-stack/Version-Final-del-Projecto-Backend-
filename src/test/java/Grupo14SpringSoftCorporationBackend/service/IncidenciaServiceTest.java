package Grupo14SpringSoftCorporationBackend.service;

import Grupo14SpringSoftCorporationBackend.model.Incidencia;
import Grupo14SpringSoftCorporationBackend.model.Tecnico;
import Grupo14SpringSoftCorporationBackend.repository.IncidenciaRepository;
import Grupo14SpringSoftCorporationBackend.repository.TecnicoRepository;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncidenciaServiceTest {

    @Mock
    private IncidenciaRepository repo;

    @Mock
    private TecnicoRepository tecnicoRepo;

    @InjectMocks
    private IncidenciaService service;

    @Test
    void registrar_fijaEstadoPendienteYFechaDeRegistro() {
        Incidencia incidencia = new Incidencia();
        when(repo.save(any(Incidencia.class))).thenAnswer(inv -> inv.getArgument(0));

        Incidencia resultado = service.registrar(incidencia);

        assertThat(resultado.getEstado()).isEqualTo("Pendiente");
        assertThat(resultado.getFechaRegistro()).isNotNull();
        verify(repo).save(incidencia);
    }

    @Test
    void listar_delegaEnElRepositorio() {
        when(repo.findAll()).thenReturn(List.of(new Incidencia(), new Incidencia()));

        List<Incidencia> resultado = service.listar();

        assertThat(resultado).hasSize(2);
        verify(repo).findAll();
    }

    @Test
    void buscar_lanzaNotFoundCuandoNoExiste() {
        when(repo.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscar(99))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Incidencia no encontrada");
    }

    @Test
    void buscar_devuelveLaIncidenciaCuandoExiste() {
        Incidencia incidencia = new Incidencia();
        incidencia.setIdIncidencia(5);
        when(repo.findById(5)).thenReturn(Optional.of(incidencia));

        Incidencia resultado = service.buscar(5);

        assertThat(resultado.getIdIncidencia()).isEqualTo(5);
    }

    @Test
    void tareasDeTecnico_delegaEnElRepositorio() {
        when(repo.findByIdTecnicoAsignado(3)).thenReturn(List.of(new Incidencia()));

        List<Incidencia> resultado = service.tareasDeTecnico(3);

        assertThat(resultado).hasSize(1);
        verify(repo).findByIdTecnicoAsignado(3);
    }

    @Test
    void historialEquipo_delegaEnElRepositorioOrdenandoPorFecha() {
        when(repo.findByCodigoEquipoOrderByFechaRegistroDesc("EQ-01")).thenReturn(List.of(new Incidencia()));

        List<Incidencia> resultado = service.historialEquipo("EQ-01");

        assertThat(resultado).hasSize(1);
        verify(repo).findByCodigoEquipoOrderByFechaRegistroDesc("EQ-01");
    }

    @Test
    void asignarTecnico_lanzaNotFoundCuandoLaIncidenciaNoExiste() {
        when(repo.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.asignarTecnico(1, 2))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Incidencia no encontrada");

        verify(tecnicoRepo, never()).findById(any());
    }

    @Test
    void asignarTecnico_lanzaNotFoundCuandoElTecnicoNoExiste() {
        when(repo.findById(1)).thenReturn(Optional.of(new Incidencia()));
        when(tecnicoRepo.findById(2)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.asignarTecnico(1, 2))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Tecnico no encontrado");
    }

    @Test
    void asignarTecnico_lanzaBadRequestCuandoElTecnicoNoEstaDisponible() {
        when(repo.findById(1)).thenReturn(Optional.of(new Incidencia()));
        Tecnico tecnico = new Tecnico();
        tecnico.setIdUsuario(2);
        tecnico.setDisponibilidad(false);
        when(tecnicoRepo.findById(2)).thenReturn(Optional.of(tecnico));

        assertThatThrownBy(() -> service.asignarTecnico(1, 2))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("no esta disponible");

        verify(repo, never()).save(any());
    }

    @Test
    void asignarTecnico_lanzaBadRequestCuandoElTecnicoAlcanzoSuLimiteConfigurado() {
        Incidencia incidencia = new Incidencia();
        incidencia.setIdIncidencia(1);
        when(repo.findById(1)).thenReturn(Optional.of(incidencia));

        Tecnico tecnico = new Tecnico();
        tecnico.setIdUsuario(2);
        tecnico.setDisponibilidad(true);
        tecnico.setMaxIncidencias(2);
        when(tecnicoRepo.findById(2)).thenReturn(Optional.of(tecnico));

        Incidencia pendiente1 = new Incidencia();
        pendiente1.setEstado("Pendiente");
        Incidencia pendiente2 = new Incidencia();
        pendiente2.setEstado("pendiente"); // verifica comparacion sin distinguir mayusculas/minusculas
        when(repo.findByIdTecnicoAsignado(2)).thenReturn(List.of(pendiente1, pendiente2));

        assertThatThrownBy(() -> service.asignarTecnico(1, 2))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("limite de 2 incidencias pendientes");

        verify(repo, never()).save(any());
    }

    @Test
    void asignarTecnico_usaLimiteDePorDefectoDeCincoCuandoMaxIncidenciasEsNulo() {
        Incidencia incidencia = new Incidencia();
        incidencia.setIdIncidencia(1);
        when(repo.findById(1)).thenReturn(Optional.of(incidencia));

        Tecnico tecnico = new Tecnico();
        tecnico.setIdUsuario(2);
        tecnico.setDisponibilidad(true);
        tecnico.setMaxIncidencias(null);
        when(tecnicoRepo.findById(2)).thenReturn(Optional.of(tecnico));

        when(repo.findByIdTecnicoAsignado(2)).thenReturn(List.of());
        when(repo.save(any(Incidencia.class))).thenAnswer(inv -> inv.getArgument(0));

        Incidencia resultado = service.asignarTecnico(1, 2);

        assertThat(resultado.getIdTecnicoAsignado()).isEqualTo(2);
        assertThat(resultado.getFechaAsignacion()).isNotNull();
    }

    @Test
    void asignarTecnico_asignaCorrectamenteCuandoElTecnicoTieneCupoDisponible() {
        Incidencia incidencia = new Incidencia();
        incidencia.setIdIncidencia(10);
        when(repo.findById(10)).thenReturn(Optional.of(incidencia));

        Tecnico tecnico = new Tecnico();
        tecnico.setIdUsuario(4);
        tecnico.setDisponibilidad(true);
        tecnico.setMaxIncidencias(5);
        when(tecnicoRepo.findById(4)).thenReturn(Optional.of(tecnico));
        when(repo.findByIdTecnicoAsignado(4)).thenReturn(List.of());
        when(repo.save(any(Incidencia.class))).thenAnswer(inv -> inv.getArgument(0));

        Incidencia resultado = service.asignarTecnico(10, 4);

        assertThat(resultado.getIdTecnicoAsignado()).isEqualTo(4);
        assertThat(resultado.getFechaAsignacion()).isNotNull();
        verify(repo).save(incidencia);
    }

    @Test
    void solucionar_lanzaNotFoundCuandoLaIncidenciaNoExiste() {
        when(repo.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.solucionar(1, "Reparacion"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Incidencia no encontrada");
    }

    @Test
    void solucionar_actualizaEstadoTipoSolucionYFecha() {
        Incidencia incidencia = new Incidencia();
        incidencia.setIdIncidencia(1);
        when(repo.findById(1)).thenReturn(Optional.of(incidencia));
        when(repo.save(any(Incidencia.class))).thenAnswer(inv -> inv.getArgument(0));

        Incidencia resultado = service.solucionar(1, "Cambio de pieza");

        assertThat(resultado.getEstado()).isEqualTo("Solucionado");
        assertThat(resultado.getTipoSolucion()).isEqualTo("Cambio de pieza");
        assertThat(resultado.getFechaSolucion()).isNotNull();
    }

    @Test
    void pendientes_delegaEnElRepositorioConElEstadoPendiente() {
        when(repo.findByEstado("Pendiente")).thenReturn(List.of(new Incidencia()));

        List<Incidencia> resultado = service.pendientes();

        assertThat(resultado).hasSize(1);
        verify(repo).findByEstado(eq("Pendiente"));
    }

    @Test
    void solucionadas_delegaEnElRepositorioConElEstadoSolucionado() {
        when(repo.findByEstado("Solucionado")).thenReturn(List.of(new Incidencia()));

        List<Incidencia> resultado = service.solucionadas();

        assertThat(resultado).hasSize(1);
        verify(repo).findByEstado(eq("Solucionado"));
    }
}
