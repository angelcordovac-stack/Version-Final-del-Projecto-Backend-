package Grupo14SpringSoftCorporationBackend.repository;

import Grupo14SpringSoftCorporationBackend.model.DiccionarioFallas;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class DiccionarioFallasRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DiccionarioFallasRepository repository;

    private DiccionarioFallas nuevaFalla(String problema, String solucion) {
        DiccionarioFallas falla = new DiccionarioFallas();
        falla.setProblemaComun(problema);
        falla.setSolucionSugerida(solucion);
        falla.setEstado("RESUELTO");
        falla.setFechaRegistro(LocalDateTime.now());
        return falla;
    }

    @Test
    void buscar_encuentraPorCoincidenciaEnProblemaComunSinDistinguirMayusculas() {
        entityManager.persist(nuevaFalla("Pantalla en negro", "Revisar cable de video"));
        entityManager.persist(nuevaFalla("No enciende", "Revisar fuente de poder"));
        entityManager.flush();

        List<DiccionarioFallas> resultado = repository
                .findByProblemaComunContainingIgnoreCaseOrSolucionSugeridaContainingIgnoreCase("PANTALLA", "PANTALLA");

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getProblemaComun()).isEqualTo("Pantalla en negro");
    }

    @Test
    void buscar_encuentraPorCoincidenciaEnSolucionSugerida() {
        entityManager.persist(nuevaFalla("Equipo lento", "Liberar espacio en disco"));
        entityManager.flush();

        List<DiccionarioFallas> resultado = repository
                .findByProblemaComunContainingIgnoreCaseOrSolucionSugeridaContainingIgnoreCase("disco", "disco");

        assertThat(resultado).hasSize(1);
    }

    @Test
    void buscar_devuelveListaVaciaCuandoNoHayCoincidencias() {
        entityManager.persist(nuevaFalla("Equipo lento", "Liberar espacio en disco"));
        entityManager.flush();

        List<DiccionarioFallas> resultado = repository
                .findByProblemaComunContainingIgnoreCaseOrSolucionSugeridaContainingIgnoreCase("inexistente", "inexistente");

        assertThat(resultado).isEmpty();
    }

    @Test
    void save_persisteYAutogeneraElId() {
        DiccionarioFallas guardada = repository.save(nuevaFalla("Falla de red", "Reiniciar router"));

        assertThat(guardada.getIdFalla()).isNotNull();
    }
}
