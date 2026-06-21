package Grupo14SpringSoftCorporationBackend.controller;

import Grupo14SpringSoftCorporationBackend.model.Usuario;
import Grupo14SpringSoftCorporationBackend.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mantenimiento")
@PreAuthorize("hasRole('JEFE')")
public class MantenimientoController {

    @Autowired
    private UsuarioService service;

    @GetMapping("/usuarios")
    public ResponseEntity<List<Usuario>> listarUsuarios() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/usuarios/{id}")
    public ResponseEntity<Usuario> obtenerUsuario(@PathVariable Integer id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @PostMapping("/usuarios")
    public ResponseEntity<Usuario> crearUsuario(@RequestBody Usuario usuario) {
        // Aseguramos que es creacion
        usuario.setIdUsuario(null);
        return ResponseEntity.ok(service.guardar(usuario));
    }

    @PutMapping("/usuarios/{id}")
    public ResponseEntity<Usuario> actualizarUsuario(@PathVariable Integer id,
                                                     @RequestBody Usuario usuario) {
        usuario.setIdUsuario(id);
        return ResponseEntity.ok(service.guardar(usuario));
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<String> eliminarUsuario(@PathVariable Integer id) {
        service.eliminar(id);
        return ResponseEntity.ok("Usuario eliminado correctamente");
    }
}
