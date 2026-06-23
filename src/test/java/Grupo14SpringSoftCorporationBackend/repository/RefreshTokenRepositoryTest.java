package Grupo14SpringSoftCorporationBackend.repository;

import Grupo14SpringSoftCorporationBackend.model.RefreshToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RefreshTokenRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RefreshTokenRepository repository;

    private RefreshToken nuevoToken(String token, Integer idUsuario) {
        RefreshToken rt = new RefreshToken();
        rt.setToken(token);
        rt.setIdUsuario(idUsuario);
        rt.setExpiryDate(Instant.now().plusSeconds(3600));
        rt.setRevocado(false);
        return rt;
    }

    @Test
    void findByToken_devuelveElTokenCuandoExiste() {
        entityManager.persist(nuevoToken(UUID.randomUUID().toString(), 1));
        RefreshToken esperado = nuevoToken("token-buscado", 2);
        entityManager.persist(esperado);
        entityManager.flush();

        Optional<RefreshToken> resultado = repository.findByToken("token-buscado");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getIdUsuario()).isEqualTo(2);
    }

    @Test
    void findByToken_devuelveVacioCuandoNoExiste() {
        Optional<RefreshToken> resultado = repository.findByToken("token-inexistente");

        assertThat(resultado).isEmpty();
    }

    @Test
    void deleteByIdUsuario_eliminaUnicamenteLosTokensDeEseUsuario() {
        entityManager.persist(nuevoToken("token-usuario-1-a", 1));
        entityManager.persist(nuevoToken("token-usuario-1-b", 1));
        entityManager.persist(nuevoToken("token-usuario-2", 2));
        entityManager.flush();

        repository.deleteByIdUsuario(1);
        entityManager.flush();
        entityManager.clear();

        assertThat(repository.findByToken("token-usuario-1-a")).isEmpty();
        assertThat(repository.findByToken("token-usuario-1-b")).isEmpty();
        assertThat(repository.findByToken("token-usuario-2")).isPresent();
    }

    @Test
    void save_persisteUnTokenConRevocadoFalsePorDefectoSiNoSeEstablece() {
        RefreshToken rt = new RefreshToken();
        rt.setToken("token-nuevo");
        rt.setIdUsuario(5);
        rt.setExpiryDate(Instant.now().plusSeconds(60));

        RefreshToken guardado = repository.save(rt);

        assertThat(guardado.getId()).isNotNull();
        assertThat(guardado.getRevocado()).isFalse();
    }
}
