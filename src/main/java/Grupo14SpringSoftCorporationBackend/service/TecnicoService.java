package Grupo14SpringSoftCorporationBackend.service;

import Grupo14SpringSoftCorporationBackend.model.Tecnico;
import Grupo14SpringSoftCorporationBackend.model.Usuario;
import Grupo14SpringSoftCorporationBackend.repository.TecnicoRepository;
import Grupo14SpringSoftCorporationBackend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TecnicoService {

    @Autowired
    private TecnicoRepository repo;

    @Autowired
    private UsuarioRepository usuarioRepo;

    public List<Tecnico> listarDisponibles() {
        return repo.findByDisponibilidad(true);
    }

    public List<Map<String, Object>> listarTodosConNombre() {
        List<Tecnico> tecnicos = repo.findAll();
        List<Map<String, Object>> resultado = new ArrayList<>();

        for (Tecnico t : tecnicos) {
            Usuario u = usuarioRepo.findById(t.getIdUsuario()).orElse(null);
            Map<String, Object> map = new HashMap<>();
            map.put("idUsuario", t.getIdUsuario());
            map.put("nombre", u != null ? u.getNombreCompleto() : "Desconocido");
            map.put("especialidad", t.getEspecialidad());
            map.put("disponibilidad", t.getDisponibilidad());
            map.put("maxIncidencias", t.getMaxIncidencias());
            resultado.add(map);
        }
        return resultado;
    }
}