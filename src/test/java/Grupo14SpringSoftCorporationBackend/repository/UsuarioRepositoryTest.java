package Grupo14SpringSoftCorporationBackend.repository;

import Grupo14SpringSoftCorporationBackend.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UsuarioRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UsuarioRepository repository;

    private Usuario nuevoUsuario(String correo, String nombre) {
        Usuario u = new Usuario();
        u.setNombreCompleto(nombre);
        u.setCorreo(correo);
        u.setPasswordHash("$2a$10$hashDePrueba");
        u.setIdPerfil(1);
        u.setActivo(true);
        return u;
    }

    @Test
    void findByCorreo_devuelveElUsuarioCuandoElCorreoExiste() {
        entityManager.persist(nuevoUsuario("ana@correo.com", "Ana Torres"));
        entityManager.flush();

        Optional<Usuario> resultado = repository.findByCorreo("ana@correo.com");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNombreCompleto()).isEqualTo("Ana Torres");
    }

    @Test
    void findByCorreo_devuelveVacioCuandoElCorreoNoExiste() {
        Optional<Usuario> resultado = repository.findByCorreo("inexistente@correo.com");

        assertThat(resultado).isEmpty();
    }

    @Test
    void save_persisteYAutogeneraElId() {
        Usuario guardado = repository.save(nuevoUsuario("nuevo@correo.com", "Usuario Nuevo"));

        assertThat(guardado.getIdUsuario()).isNotNull();
    }

    @Test
    void existsById_devuelveTrueCuandoElUsuarioExiste() {
        Usuario guardado = entityManager.persistAndFlush(nuevoUsuario("existe@correo.com", "Existente"));

        assertThat(repository.existsById(guardado.getIdUsuario())).isTrue();
    }

    @Test
    void existsById_devuelveFalseCuandoElUsuarioNoExiste() {
        assertThat(repository.existsById(99999)).isFalse();
    }
}
