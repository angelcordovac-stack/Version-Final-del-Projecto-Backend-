package Grupo14SpringSoftCorporationBackend.repository;

import Grupo14SpringSoftCorporationBackend.model.InformeTecnico;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface InformeTecnicoRepository extends JpaRepository<InformeTecnico, Integer> {

    // Buscar informe por id de incidencia
    Optional<InformeTecnico> findByIdIncidencia(Integer idIncidencia);
}