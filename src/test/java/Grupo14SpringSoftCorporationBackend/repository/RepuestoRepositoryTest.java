package Grupo14SpringSoftCorporationBackend.repository;

import Grupo14SpringSoftCorporationBackend.model.Repuesto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RepuestoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RepuestoRepository repository;

    private Repuesto nuevoRepuesto(Integer idIncidencia, String estado) {
        Repuesto r = new Repuesto();
        r.setIdIncidencia(idIncidencia);
        r.setDescripcion("Bateria de laptop");
        r.setEstado(estado);
        r.setFechaSolicitud(LocalDateTime.now());
        return r;
    }

    @Test
    void findByEstado_devuelveSoloLosRepuestosConEseEstado() {
        entityManager.persist(nuevoRepuesto(1, "Solicitado"));
        entityManager.persist(nuevoRepuesto(2, "Entregado"));
        entityManager.flush();

        List<Repuesto> resultado = repository.findByEstado("Solicitado");

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getIdIncidencia()).isEqualTo(1);
    }

    @Test
    void findByIdIncidencia_devuelveTodosLosRepuestosDeEsaIncidencia() {
        entityManager.persist(nuevoRepuesto(5, "Solicitado"));
        entityManager.persist(nuevoRepuesto(5, "Entregado"));
        entityManager.persist(nuevoRepuesto(6, "Solicitado"));
        entityManager.flush();

        List<Repuesto> resultado = repository.findByIdIncidencia(5);

        assertThat(resultado).hasSize(2);
        assertThat(resultado).allMatch(r -> r.getIdIncidencia().equals(5));
    }

    @Test
    void save_persisteYAutogeneraElId() {
        Repuesto guardado = repository.save(nuevoRepuesto(7, "Solicitado"));

        assertThat(guardado.getIdRepuesto()).isNotNull();
    }
}
