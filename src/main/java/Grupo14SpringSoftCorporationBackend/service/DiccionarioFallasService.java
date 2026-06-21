package Grupo14SpringSoftCorporationBackend.service;

import Grupo14SpringSoftCorporationBackend.model.DiccionarioFallas;
import Grupo14SpringSoftCorporationBackend.repository.DiccionarioFallasRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class DiccionarioFallasService {

    @Autowired
    private DiccionarioFallasRepository repo;

    // Estados permitidos para la solucion registrada en el diccionario de fallas
    private static final Set<String> ESTADOS_VALIDOS = Set.of(
            "CRITICO", "EN_CURSO", "MANTENIMIENTO", "RESUELTO"
    );

    public DiccionarioFallas registrar(DiccionarioFallas falla) {
        if (falla.getEstado() == null || falla.getEstado().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El estado es obligatorio.");
        }

        String estadoNormalizado = falla.getEstado().trim().toUpperCase();
        if (!ESTADOS_VALIDOS.contains(estadoNormalizado)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Estado invalido. Valores permitidos: " + ESTADOS_VALIDOS);
        }
        falla.setEstado(estadoNormalizado);

        // La fecha de registro (timestamp) se guarda de forma oculta y
        // automatica en el servidor; nunca se toma del cliente.
        falla.setFechaRegistro(LocalDateTime.now());

        return repo.save(falla);
    }

    public List<DiccionarioFallas> listar() {
        return repo.findAll();
    }

    public List<DiccionarioFallas> buscar(String keyword) {
        return repo.findByProblemaComunContainingIgnoreCaseOrSolucionSugeridaContainingIgnoreCase(
                keyword, keyword);
    }
}