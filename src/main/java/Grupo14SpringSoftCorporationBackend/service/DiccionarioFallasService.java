package Grupo14SpringSoftCorporationBackend.service;

import Grupo14SpringSoftCorporationBackend.model.DiccionarioFallas;
import Grupo14SpringSoftCorporationBackend.model.Usuario;
import Grupo14SpringSoftCorporationBackend.repository.DiccionarioFallasRepository;
import Grupo14SpringSoftCorporationBackend.repository.UsuarioRepository;
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

    @Autowired
    private UsuarioRepository usuarioRepo;

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

        DiccionarioFallas guardada = repo.save(falla);
        return conNombreAutor(guardada);
    }

    public List<DiccionarioFallas> listar() {
        return conNombreAutor(repo.findAll());
    }

    public List<DiccionarioFallas> buscar(String keyword) {
        List<DiccionarioFallas> resultado = repo.findByProblemaComunContainingIgnoreCaseOrSolucionSugeridaContainingIgnoreCase(
                keyword, keyword);
        return conNombreAutor(resultado);
    }

    // Completa el nombre del tecnico/usuario asignado a partir de id_autor,
    // para que el frontend pueda mostrarlo en el detalle de la falla.
    private List<DiccionarioFallas> conNombreAutor(List<DiccionarioFallas> fallas) {
        fallas.forEach(this::conNombreAutor);
        return fallas;
    }

    private DiccionarioFallas conNombreAutor(DiccionarioFallas falla) {
        if (falla.getIdAutor() != null) {
            usuarioRepo.findById(falla.getIdAutor())
                    .map(Usuario::getNombreCompleto)
                    .ifPresent(falla::setNombreAutor);
        }
        return falla;
    }
}