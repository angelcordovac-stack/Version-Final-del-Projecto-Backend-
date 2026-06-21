package Grupo14SpringSoftCorporationBackend.repository;

import Grupo14SpringSoftCorporationBackend.model.Tecnico;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TecnicoRepository extends JpaRepository<Tecnico, Integer> {

    // Buscar técnicos disponibles
    List<Tecnico> findByDisponibilidad(Boolean disponibilidad);
}