package Grupo14SpringSoftCorporationBackend.repository;

import Grupo14SpringSoftCorporationBackend.model.Incidencia;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class IncidenciaRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private IncidenciaRepository repository;

    private Incidencia nuevaIncidencia(String codigoEquipo, String estado, Integer idTecnico, LocalDateTime fechaRegistro) {
        Incidencia incidencia = new Incidencia();
        incidencia.setCodigoEquipo(codigoEquipo);
        incidencia.setDescripcionProblema("Descripcion de prueba con longitud suficiente");
        incidencia.setEstado(estado);
        incidencia.setIdTecnicoAsignado(idTecnico);
        incidencia.setFechaRegistro(fechaRegistro);
        return incidencia;
    }

    @Test
    void findByIdTecnicoAsignado_devuelveSoloLasIncidenciasDeEseTecnico() {
        entityManager.persist(nuevaIncidencia("EQ-01", "Pendiente", 1, LocalDateTime.now()));
        entityManager.persist(nuevaIncidencia("EQ-02", "Pendiente", 2, LocalDateTime.now()));
        entityManager.persist(nuevaIncidencia("EQ-03", "Solucionado", 1, LocalDateTime.now()));
        entityManager.flush();

        List<Incidencia> resultado = repository.findByIdTecnicoAsignado(1);

        assertThat(resultado).hasSize(2);
        assertThat(resultado).allMatch(i -> i.getIdTecnicoAsignado().equals(1));
    }

    @Test
    void findByEstado_devuelveSoloLasIncidenciasConEseEstado() {
        entityManager.persist(nuevaIncidencia("EQ-01", "Pendiente", null, LocalDateTime.now()));
        entityManager.persist(nuevaIncidencia("EQ-02", "Solucionado", null, LocalDateTime.now()));
        entityManager.flush();

        List<Incidencia> resultado = repository.findByEstado("Pendiente");

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getCodigoEquipo()).isEqualTo("EQ-01");
    }

    @Test
    void findByCodigoEquipoOrderByFechaRegistroDesc_devuelveElHistorialOrdenadoDelMasRecienteAlMasAntiguo() {
        LocalDateTime hace3dias = LocalDateTime.now().minusDays(3);
        LocalDateTime hoy = LocalDateTime.now();
        LocalDateTime hace1dia = LocalDateTime.now().minusDays(1);

        entityManager.persist(nuevaIncidencia("EQ-01", "Solucionado", null, hace3dias));
        entityManager.persist(nuevaIncidencia("EQ-01", "Pendiente", null, hoy));
        entityManager.persist(nuevaIncidencia("EQ-01", "Solucionado", null, hace1dia));
        entityManager.persist(nuevaIncidencia("EQ-02", "Pendiente", null, hoy)); // de otro equipo
        entityManager.flush();

        List<Incidencia> resultado = repository.findByCodigoEquipoOrderByFechaRegistroDesc("EQ-01");

        assertThat(resultado).hasSize(3);
        assertThat(resultado).extracting(Incidencia::getFechaRegistro)
                .isSortedAccordingTo((a, b) -> b.compareTo(a));
        assertThat(resultado.get(0).getFechaRegistro()).isEqualTo(hoy);
        assertThat(resultado.get(2).getFechaRegistro()).isEqualTo(hace3dias);
    }

    @Test
    void save_persisteYAsignaUnIdAutogenerado() {
        Incidencia incidencia = nuevaIncidencia("EQ-09", "Pendiente", null, LocalDateTime.now());

        Incidencia guardada = repository.save(incidencia);

        assertThat(guardada.getIdIncidencia()).isNotNull();
    }
}
