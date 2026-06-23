package Grupo14SpringSoftCorporationBackend.service;

import Grupo14SpringSoftCorporationBackend.model.Tecnico;
import Grupo14SpringSoftCorporationBackend.model.Usuario;
import Grupo14SpringSoftCorporationBackend.repository.TecnicoRepository;
import Grupo14SpringSoftCorporationBackend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TecnicoServiceTest {

    @Mock
    private TecnicoRepository repo;

    @Mock
    private UsuarioRepository usuarioRepo;

    @InjectMocks
    private TecnicoService service;

    @Test
    void listarDisponibles_delegaEnElRepositorioConDisponibilidadTrue() {
        Tecnico t = new Tecnico();
        when(repo.findByDisponibilidad(true)).thenReturn(List.of(t));

        List<Tecnico> resultado = service.listarDisponibles();

        assertThat(resultado).containsExactly(t);
        verify(repo).findByDisponibilidad(true);
    }

    @Test
    void listarTodosConNombre_completaElNombreCuandoElUsuarioExiste() {
        Tecnico tecnico = new Tecnico();
        tecnico.setIdUsuario(1);
        tecnico.setEspecialidad("Redes");
        tecnico.setDisponibilidad(true);
        tecnico.setMaxIncidencias(5);

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1);
        usuario.setNombreCompleto("Pedro Gomez");

        when(repo.findAll()).thenReturn(List.of(tecnico));
        when(usuarioRepo.findById(1)).thenReturn(Optional.of(usuario));

        List<Map<String, Object>> resultado = service.listarTodosConNombre();

        assertThat(resultado).hasSize(1);
        Map<String, Object> item = resultado.get(0);
        assertThat(item.get("idUsuario")).isEqualTo(1);
        assertThat(item.get("nombre")).isEqualTo("Pedro Gomez");
        assertThat(item.get("especialidad")).isEqualTo("Redes");
        assertThat(item.get("disponibilidad")).isEqualTo(true);
        assertThat(item.get("maxIncidencias")).isEqualTo(5);
    }

    @Test
    void listarTodosConNombre_usaDesconocidoCuandoElUsuarioNoExiste() {
        Tecnico tecnico = new Tecnico();
        tecnico.setIdUsuario(2);
        tecnico.setEspecialidad("Hardware");
        tecnico.setDisponibilidad(false);
        tecnico.setMaxIncidencias(3);

        when(repo.findAll()).thenReturn(List.of(tecnico));
        when(usuarioRepo.findById(2)).thenReturn(Optional.empty());

        List<Map<String, Object>> resultado = service.listarTodosConNombre();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).get("nombre")).isEqualTo("Desconocido");
    }

    @Test
    void listarTodosConNombre_devuelveListaVaciaCuandoNoHayTecnicos() {
        when(repo.findAll()).thenReturn(List.of());

        List<Map<String, Object>> resultado = service.listarTodosConNombre();

        assertThat(resultado).isEmpty();
    }
}
