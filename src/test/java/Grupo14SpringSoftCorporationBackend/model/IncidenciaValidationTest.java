package Grupo14SpringSoftCorporationBackend.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class IncidenciaValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void crearValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void cerrarFactory() {
        factory.close();
    }

    private Incidencia incidenciaValida() {
        Incidencia incidencia = new Incidencia();
        incidencia.setCodigoEquipo("EQ-01");
        incidencia.setDescripcionProblema("La pantalla no enciende desde esta mañana");
        return incidencia;
    }

    @Test
    void incidenciaValida_noGeneraViolaciones() {
        Set<ConstraintViolation<Incidencia>> violaciones = validator.validate(incidenciaValida());

        assertThat(violaciones).isEmpty();
    }

    @Test
    void codigoEquipo_esObligatorio() {
        Incidencia incidencia = incidenciaValida();
        incidencia.setCodigoEquipo("   ");

        Set<ConstraintViolation<Incidencia>> violaciones = validator.validate(incidencia);

        assertThat(violaciones).anyMatch(v -> v.getPropertyPath().toString().equals("codigoEquipo"));
    }

    @Test
    void descripcionProblema_esObligatoria() {
        Incidencia incidencia = incidenciaValida();
        incidencia.setDescripcionProblema(null);

        Set<ConstraintViolation<Incidencia>> violaciones = validator.validate(incidencia);

        assertThat(violaciones).anyMatch(v -> v.getPropertyPath().toString().equals("descripcionProblema"));
    }

    @Test
    void descripcionProblema_debeTenerAlMenosDiezCaracteres() {
        Incidencia incidencia = incidenciaValida();
        incidencia.setDescripcionProblema("muy corta");

        Set<ConstraintViolation<Incidencia>> violaciones = validator.validate(incidencia);

        assertThat(violaciones).anyMatch(v -> v.getPropertyPath().toString().equals("descripcionProblema"));
    }

    @Test
    void descripcionProblema_noPuedeSuperarLos500Caracteres() {
        Incidencia incidencia = incidenciaValida();
        incidencia.setDescripcionProblema("a".repeat(501));

        Set<ConstraintViolation<Incidencia>> violaciones = validator.validate(incidencia);

        assertThat(violaciones).anyMatch(v -> v.getPropertyPath().toString().equals("descripcionProblema"));
    }
}
