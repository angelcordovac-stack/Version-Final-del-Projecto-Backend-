package Grupo14SpringSoftCorporationBackend.repository;

import Grupo14SpringSoftCorporationBackend.model.InformeTecnico;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class InformeTecnicoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private InformeTecnicoRepository repository;

    @Test
    void findByIdIncidencia_devuelveElInformeCuandoExiste() {
        InformeTecnico informe = new InformeTecnico();
        informe.setIdIncidencia(10);
        informe.setDiagnostico("Falla de fuente de poder");
        informe.setFechaInforme(LocalDateTime.now());
        entityManager.persist(informe);
        entityManager.flush();

        Optional<InformeTecnico> resultado = repository.findByIdIncidencia(10);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getDiagnostico()).isEqualTo("Falla de fuente de poder");
    }

    @Test
    void findByIdIncidencia_devuelveVacioCuandoNoExisteInforme() {
        Optional<InformeTecnico> resultado = repository.findByIdIncidencia(999);

        assertThat(resultado).isEmpty();
    }

    @Test
    void save_persisteElInformeConSusDatos() {
        InformeTecnico informe = new InformeTecnico();
        informe.setIdIncidencia(20);
        informe.setDiagnostico("Sobrecalentamiento");
        informe.setProcedimientoRealizado("Limpieza de ventiladores");
        informe.setObservaciones("Revisar en 30 dias");
        informe.setFechaInforme(LocalDateTime.now());

        InformeTecnico guardado = repository.save(informe);

        assertThat(guardado.getIdInforme()).isNotNull();
        assertThat(repository.findById(guardado.getIdInforme())).isPresent();
    }
}
