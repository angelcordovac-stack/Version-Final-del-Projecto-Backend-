package Grupo14SpringSoftCorporationBackend.controller;

import Grupo14SpringSoftCorporationBackend.service.TecnicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tecnicos")
public class TecnicoController {

    @Autowired
    private TecnicoService service;

    // LISTAR TECNICOS CON NOMBRE - para que el jefe asigne
    @GetMapping("/disponibles")
    public List<Map<String, Object>> listarDisponibles() {
        return service.listarTodosConNombre();
    }
}
