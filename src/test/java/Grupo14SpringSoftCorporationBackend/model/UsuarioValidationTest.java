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

class UsuarioValidationTest {

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

    private Usuario usuarioValido() {
        Usuario u = new Usuario();
        u.setNombreCompleto("Ana Torres");
        u.setCorreo("ana@correo.com");
        u.setPasswordHash("123456");
        return u;
    }

    @Test
    void usuarioValido_noGeneraViolaciones() {
        Set<ConstraintViolation<Usuario>> violaciones = validator.validate(usuarioValido());

        assertThat(violaciones).isEmpty();
    }

    @Test
    void nombreCompleto_esObligatorio() {
        Usuario usuario = usuarioValido();
        usuario.setNombreCompleto("  ");

        Set<ConstraintViolation<Usuario>> violaciones = validator.validate(usuario);

        assertThat(violaciones).anyMatch(v -> v.getPropertyPath().toString().equals("nombreCompleto"));
    }

    @Test
    void nombreCompleto_debeTenerAlMenosTresCaracteres() {
        Usuario usuario = usuarioValido();
        usuario.setNombreCompleto("Al");

        Set<ConstraintViolation<Usuario>> violaciones = validator.validate(usuario);

        assertThat(violaciones).anyMatch(v -> v.getPropertyPath().toString().equals("nombreCompleto"));
    }

    @Test
    void nombreCompleto_noPuedeSuperarLos100Caracteres() {
        Usuario usuario = usuarioValido();
        usuario.setNombreCompleto("A".repeat(101));

        Set<ConstraintViolation<Usuario>> violaciones = validator.validate(usuario);

        assertThat(violaciones).anyMatch(v -> v.getPropertyPath().toString().equals("nombreCompleto"));
    }

    @Test
    void correo_esObligatorio() {
        Usuario usuario = usuarioValido();
        usuario.setCorreo("");

        Set<ConstraintViolation<Usuario>> violaciones = validator.validate(usuario);

        assertThat(violaciones).anyMatch(v -> v.getPropertyPath().toString().equals("correo"));
    }

    @Test
    void correo_debeTenerFormatoValido() {
        Usuario usuario = usuarioValido();
        usuario.setCorreo("esto-no-es-un-correo");

        Set<ConstraintViolation<Usuario>> violaciones = validator.validate(usuario);

        assertThat(violaciones).anyMatch(v -> v.getPropertyPath().toString().equals("correo"));
    }

    @Test
    void passwordHash_debeTenerAlMenosSeisCaracteresSiSeEnvia() {
        Usuario usuario = usuarioValido();
        usuario.setPasswordHash("123");

        Set<ConstraintViolation<Usuario>> violaciones = validator.validate(usuario);

        assertThat(violaciones).anyMatch(v -> v.getPropertyPath().toString().equals("passwordHash"));
    }
}
