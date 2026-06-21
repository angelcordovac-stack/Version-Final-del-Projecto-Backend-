package Grupo14SpringSoftCorporationBackend.controller;

import Grupo14SpringSoftCorporationBackend.model.Incidencia;
import Grupo14SpringSoftCorporationBackend.service.IncidenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/incidencias")
public class IncidenciaController {

    @Autowired
    private IncidenciaService service;

    // REGISTRAR INCIDENCIA - cualquier usuario autenticado del area
    @PostMapping
    public Incidencia registrar(@RequestBody Incidencia incidencia) {
        return service.registrar(incidencia);
    }

    // LISTAR TODAS
    @GetMapping
    public List<Incidencia> listar() {
        return service.listar();
    }

    // BUSCAR POR ID
    @GetMapping("/{id}")
    public Incidencia buscar(@PathVariable Integer id) {
        return service.buscar(id);
    }

    // TAREAS DE UN TECNICO
    @GetMapping("/tecnico/{idTecnico}")
    public List<Incidencia> tareasDeTecnico(@PathVariable Integer idTecnico) {
        return service.tareasDeTecnico(idTecnico);
    }

    // HISTORIAL DE UN EQUIPO
    @GetMapping("/equipo/{codigoEquipo}")
    public List<Incidencia> historialEquipo(@PathVariable String codigoEquipo) {
        return service.historialEquipo(codigoEquipo);
    }

    // ASIGNAR TECNICO - solo JEFE
    @PreAuthorize("hasRole('JEFE')")
    @PutMapping("/{id}/asignar")
    public Incidencia asignarTecnico(@PathVariable Integer id,
                                     @RequestBody Map<String, Integer> body) {
        Integer idTecnico = body.get("idTecnico");
        return service.asignarTecnico(id, idTecnico);
    }

    // SOLUCIONAR INCIDENCIA - solo TECNICO o JEFE
    @PreAuthorize("hasAnyRole('TECNICO','JEFE')")
    @PutMapping("/{id}/solucionar")
    public Incidencia solucionar(@PathVariable Integer id,
                                 @RequestBody Map<String, String> body) {
        String tipoSolucion = body != null ? body.get("tipoSolucion") : null;
        return service.solucionar(id, tipoSolucion);
    }

    @GetMapping("/pendientes")
    public List<Incidencia> pendientes() {
        return service.pendientes();
    }

    @GetMapping("/solucionadas")
    public List<Incidencia> solucionadas() {
        return service.solucionadas();
    }
}
