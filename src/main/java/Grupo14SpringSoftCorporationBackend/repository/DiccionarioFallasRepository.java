package Grupo14SpringSoftCorporationBackend.repository;

import Grupo14SpringSoftCorporationBackend.model.DiccionarioFallas;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DiccionarioFallasRepository extends JpaRepository<DiccionarioFallas, Integer> {

    List<DiccionarioFallas> findByProblemaComunContainingIgnoreCaseOrSolucionSugeridaContainingIgnoreCase(
            String problema, String solucion);
}