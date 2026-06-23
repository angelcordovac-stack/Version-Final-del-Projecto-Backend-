package Grupo14SpringSoftCorporationBackend.repository;

import Grupo14SpringSoftCorporationBackend.model.Tecnico;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TecnicoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TecnicoRepository repository;

    @Test
    void findByDisponibilidad_devuelveSoloLosTecnicosDisponibles() {
        entityManager.persist(new Tecnico(1, "Redes", 5, true));
        entityManager.persist(new Tecnico(2, "Hardware", 3, false));
        entityManager.persist(new Tecnico(3, "Software", 4, true));
        entityManager.flush();

        List<Tecnico> resultado = repository.findByDisponibilidad(true);

        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting(Tecnico::getIdUsuario).containsExactlyInAnyOrder(1, 3);
    }

    @Test
    void findByDisponibilidad_devuelveSoloLosTecnicosNoDisponibles() {
        entityManager.persist(new Tecnico(1, "Redes", 5, true));
        entityManager.persist(new Tecnico(2, "Hardware", 3, false));
        entityManager.flush();

        List<Tecnico> resultado = repository.findByDisponibilidad(false);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getIdUsuario()).isEqualTo(2);
    }

    @Test
    void save_persisteUnTecnicoConSuClavePrimariaManual() {
        Tecnico tecnico = new Tecnico(10, "Electricidad", 6, true);

        Tecnico guardado = repository.save(tecnico);

        assertThat(repository.findById(10)).isPresent();
        assertThat(guardado.getEspecialidad()).isEqualTo("Electricidad");
    }
}
