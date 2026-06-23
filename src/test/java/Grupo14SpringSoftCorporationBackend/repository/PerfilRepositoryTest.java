package Grupo14SpringSoftCorporationBackend.repository;

import Grupo14SpringSoftCorporationBackend.model.Perfil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PerfilRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PerfilRepository repository;

    @Test
    void save_persisteYAutogeneraElId() {
        Perfil perfil = new Perfil(null, "Jefe");

        Perfil guardado = repository.save(perfil);

        assertThat(guardado.getIdPerfil()).isNotNull();
        assertThat(guardado.getNombrePerfil()).isEqualTo("Jefe");
    }

    @Test
    void findAll_devuelveTodosLosPerfilesPersistidos() {
        entityManager.persist(new Perfil(null, "Jefe"));
        entityManager.persist(new Perfil(null, "Tecnico"));
        entityManager.flush();

        List<Perfil> resultado = repository.findAll();

        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting(Perfil::getNombrePerfil)
                .containsExactlyInAnyOrder("Jefe", "Tecnico");
    }
}
