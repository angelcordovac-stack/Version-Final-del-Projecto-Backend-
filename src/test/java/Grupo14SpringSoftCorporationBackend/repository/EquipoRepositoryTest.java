package Grupo14SpringSoftCorporationBackend.repository;

import Grupo14SpringSoftCorporationBackend.model.Equipo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class EquipoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EquipoRepository repository;

    @Test
    void save_persisteUnEquipoConSuClavePrimariaManual() {
        Equipo equipo = new Equipo("EQ-100", "Lenovo ThinkPad", "Piso 3", "Carlos Ruiz");

        Equipo guardado = repository.save(equipo);

        assertThat(guardado.getCodigoEquipo()).isEqualTo("EQ-100");
        Optional<Equipo> encontrado = repository.findById("EQ-100");
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getMarcaModelo()).isEqualTo("Lenovo ThinkPad");
        assertThat(encontrado.get().getAreaUbicacion()).isEqualTo("Piso 3");
        assertThat(encontrado.get().getResponsable()).isEqualTo("Carlos Ruiz");
    }

    @Test
    void findAll_devuelveTodosLosEquiposPersistidos() {
        entityManager.persist(new Equipo("EQ-01", "HP ProBook", "Piso 1", "Ana"));
        entityManager.persist(new Equipo("EQ-02", "Dell OptiPlex", "Piso 2", "Luis"));
        entityManager.flush();

        List<Equipo> resultado = repository.findAll();

        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting(Equipo::getCodigoEquipo)
                .containsExactlyInAnyOrder("EQ-01", "EQ-02");
    }

    @Test
    void findById_devuelveVacioCuandoElCodigoNoExiste() {
        Optional<Equipo> resultado = repository.findById("NO-EXISTE");

        assertThat(resultado).isEmpty();
    }

    @Test
    void deleteById_eliminaElEquipoCorrectamente() {
        entityManager.persist(new Equipo("EQ-05", "Asus VivoBook", "Piso 5", "Sofia"));
        entityManager.flush();

        repository.deleteById("EQ-05");

        assertThat(repository.findById("EQ-05")).isEmpty();
    }
}
