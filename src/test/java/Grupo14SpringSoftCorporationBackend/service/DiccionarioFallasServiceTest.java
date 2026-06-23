package Grupo14SpringSoftCorporationBackend.service;

import Grupo14SpringSoftCorporationBackend.model.DiccionarioFallas;
import Grupo14SpringSoftCorporationBackend.model.Usuario;
import Grupo14SpringSoftCorporationBackend.repository.DiccionarioFallasRepository;
import Grupo14SpringSoftCorporationBackend.repository.UsuarioRepository;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiccionarioFallasServiceTest {

    @Mock
    private DiccionarioFallasRepository repo;

    @Mock
    private UsuarioRepository usuarioRepo;

    @InjectMocks
    private DiccionarioFallasService service;

    @Test
    void registrar_lanzaExcepcionCuandoEstadoEsNulo() {
        DiccionarioFallas falla = new DiccionarioFallas();
        falla.setEstado(null);

        assertThatThrownBy(() -> service.registrar(falla))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("El estado es obligatorio");

        verify(repo, never()).save(any());
    }

    @Test
    void registrar_lanzaExcepcionCuandoEstadoEsEnBlanco() {
        DiccionarioFallas falla = new DiccionarioFallas();
        falla.setEstado("   ");

        assertThatThrownBy(() -> service.registrar(falla))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("El estado es obligatorio");

        verify(repo, never()).save(any());
    }

    @Test
    void registrar_lanzaExcepcionCuandoEstadoNoEsValido() {
        DiccionarioFallas falla = new DiccionarioFallas();
        falla.setEstado("INVENTADO");

        assertThatThrownBy(() -> service.registrar(falla))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Estado invalido");

        verify(repo, never()).save(any());
    }

    @Test
    void registrar_normalizaElEstadoAMayusculasYRecortaEspacios() {
        DiccionarioFallas falla = new DiccionarioFallas();
        falla.setEstado("  resuelto  ");
        falla.setIdAutor(null);

        when(repo.save(any(DiccionarioFallas.class))).thenAnswer(inv -> inv.getArgument(0));

        DiccionarioFallas guardada = service.registrar(falla);

        assertThat(guardada.getEstado()).isEqualTo("RESUELTO");
        assertThat(guardada.getFechaRegistro()).isNotNull();
        verify(repo).save(falla);
    }

    @Test
    void registrar_completaElNombreDelAutorCuandoExisteIdAutor() {
        DiccionarioFallas falla = new DiccionarioFallas();
        falla.setEstado("CRITICO");
        falla.setIdAutor(7);

        Usuario autor = new Usuario();
        autor.setIdUsuario(7);
        autor.setNombreCompleto("Maria Lopez");

        when(repo.save(any(DiccionarioFallas.class))).thenAnswer(inv -> inv.getArgument(0));
        when(usuarioRepo.findById(7)).thenReturn(Optional.of(autor));

        DiccionarioFallas resultado = service.registrar(falla);

        assertThat(resultado.getNombreAutor()).isEqualTo("Maria Lopez");
    }

    @Test
    void registrar_noFallaCuandoElAutorNoExiste() {
        DiccionarioFallas falla = new DiccionarioFallas();
        falla.setEstado("EN_CURSO");
        falla.setIdAutor(99);

        when(repo.save(any(DiccionarioFallas.class))).thenAnswer(inv -> inv.getArgument(0));
        when(usuarioRepo.findById(99)).thenReturn(Optional.empty());

        DiccionarioFallas resultado = service.registrar(falla);

        assertThat(resultado.getNombreAutor()).isNull();
    }

    @Test
    void listar_devuelveTodasLasFallasConNombreDeAutorCompletado() {
        DiccionarioFallas f1 = new DiccionarioFallas();
        f1.setIdAutor(1);
        DiccionarioFallas f2 = new DiccionarioFallas();
        f2.setIdAutor(null);

        Usuario u1 = new Usuario();
        u1.setIdUsuario(1);
        u1.setNombreCompleto("Carlos Ruiz");

        when(repo.findAll()).thenReturn(List.of(f1, f2));
        when(usuarioRepo.findById(1)).thenReturn(Optional.of(u1));

        List<DiccionarioFallas> resultado = service.listar();

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getNombreAutor()).isEqualTo("Carlos Ruiz");
        assertThat(resultado.get(1).getNombreAutor()).isNull();
        verify(usuarioRepo, times(1)).findById(any());
    }

    @Test
    void buscar_delegaEnElRepositorioConElMismoKeywordParaAmbosCampos() {
        when(repo.findByProblemaComunContainingIgnoreCaseOrSolucionSugeridaContainingIgnoreCase(
                anyString(), anyString())).thenReturn(List.of());

        service.buscar("pantalla");

        verify(repo).findByProblemaComunContainingIgnoreCaseOrSolucionSugeridaContainingIgnoreCase(
                "pantalla", "pantalla");
    }
}
