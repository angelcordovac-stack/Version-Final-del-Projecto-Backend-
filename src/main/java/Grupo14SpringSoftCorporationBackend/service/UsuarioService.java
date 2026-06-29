package Grupo14SpringSoftCorporationBackend.service;

import Grupo14SpringSoftCorporationBackend.model.RefreshToken;
import Grupo14SpringSoftCorporationBackend.model.Tecnico;
import Grupo14SpringSoftCorporationBackend.model.Usuario;
import Grupo14SpringSoftCorporationBackend.repository.TecnicoRepository;
import Grupo14SpringSoftCorporationBackend.repository.UsuarioRepository;
import Grupo14SpringSoftCorporationBackend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UsuarioService {

    private static final int PERFIL_TECNICO = 2;

    @Autowired
    private UsuarioRepository repo;

    @Autowired
    private TecnicoRepository tecnicoRepo;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RefreshTokenService refreshTokenService;

    /**
     * LOGIN - devuelve access token + refresh token + datos del usuario.
     * Verifica la password con BCrypt (no en texto plano).
     */
    public Map<String, Object> login(String correo, String password) {
        Usuario u = repo.findByCorreo(correo).orElse(null);

        // Si el usuario no existe o esta inactivo, no decimos cual de los dos
        // para no dar pistas a un atacante.
        if (u == null || Boolean.FALSE.equals(u.getActivo())) {
            return null;
        }

        // Comparacion segura con BCrypt
        if (!passwordEncoder.matches(password, u.getPasswordHash())) {
            return null;
        }

        String accessToken = jwtUtil.generateToken(u.getCorreo(), u.getIdUsuario(), u.getIdPerfil());

        // Crear (o rotar) el refresh token para este usuario
        RefreshToken refreshToken = refreshTokenService.crear(u.getIdUsuario());

        Map<String, Object> response = new HashMap<>();
        response.put("token", accessToken);
        response.put("refreshToken", refreshToken.getToken());
        response.put("idUsuario", u.getIdUsuario());
        response.put("nombreCompleto", u.getNombreCompleto());
        response.put("correo", u.getCorreo());
        response.put("idPerfil", u.getIdPerfil());
        response.put("activo", u.getActivo());
        response.put("perfil", switch (u.getIdPerfil()) {
            case 1 -> "Jefe";
            case 2 -> "Tecnico";
            case 3 -> "Sistemas";
            default -> "Usuario";
        });

        return response;
    }

    // LISTAR
    public List<Usuario> listar() {
        return repo.findAll();
    }

    // BUSCAR
    public Usuario buscar(Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Usuario no encontrado con id: " + id));
    }

    // BUSCAR POR CORREO
    public Usuario buscarPorCorreo(String correo) {
        return repo.findByCorreo(correo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Usuario no encontrado con correo: " + correo));
    }

    /**
     * GUARDAR (crear o actualizar).
     * Si la password viene en texto plano (no esta hasheada), la hashea con BCrypt.
     * Para detectar si esta hasheada, se chequea el prefijo $2 que usan los hashes
     * BCrypt ($2a$, $2b$, $2y$).
     *
     * SINCRONIZACION CON TABLA TECNICOS:
     * - Si el usuario tiene idPerfil == 2 (Técnico), se crea automáticamente
     *   un registro en la tabla "tecnicos" si no existe aún.
     * - Si se cambia el perfil a otro distinto de 2, se elimina el registro
     *   de "tecnicos" (si existía).
     */
    @Transactional
    public Usuario guardar(Usuario usuario) {
        // Validacion: correo unico
        if (usuario.getIdUsuario() == null) {
            // Creacion
            repo.findByCorreo(usuario.getCorreo()).ifPresent(u -> {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Ya existe un usuario con el correo " + usuario.getCorreo());
            });
        } else {
            // Actualizacion: si cambio el correo, verificar que no exista otro con ese correo
            repo.findByCorreo(usuario.getCorreo()).ifPresent(existente -> {
                if (!existente.getIdUsuario().equals(usuario.getIdUsuario())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Ya existe otro usuario con el correo " + usuario.getCorreo());
                }
            });
        }

        // Hashear password si viene en texto plano
        String pwd = usuario.getPasswordHash();
        if (pwd != null && !pwd.isBlank() && !pwd.startsWith("$2")) {
            usuario.setPasswordHash(passwordEncoder.encode(pwd));
        } else if (pwd == null || pwd.isBlank()) {
            // En actualizacion, si no se manda password, mantener la existente
            if (usuario.getIdUsuario() != null) {
                Usuario existente = repo.findById(usuario.getIdUsuario()).orElse(null);
                if (existente != null) {
                    usuario.setPasswordHash(existente.getPasswordHash());
                }
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La contrasena es obligatoria al crear un usuario");
            }
        }

        // Guardar el usuario primero para tener el id generado (en caso de creacion)
        Usuario guardado = repo.save(usuario);

        // --- SINCRONIZACION CON TABLA TECNICOS ---
        sincronizarTecnico(guardado);

        return guardado;
    }

    /**
     * Si el usuario es Técnico (idPerfil == 2), garantiza que exista un registro
     * en la tabla "tecnicos". Si ya existía, lo deja intacto (no pisa especialidad
     * ni maxIncidencias que hayan sido configurados manualmente).
     *
     * Si el usuario dejó de ser Técnico, elimina su registro de "tecnicos".
     */
    private void sincronizarTecnico(Usuario usuario) {
        Integer idUsuario = usuario.getIdUsuario();

        if (PERFIL_TECNICO == usuario.getIdPerfil()) {
            // Crear registro en tecnicos si no existe todavia
            boolean yaTecnico = tecnicoRepo.existsById(idUsuario);
            if (!yaTecnico) {
                Tecnico nuevoTecnico = new Tecnico();
                nuevoTecnico.setIdUsuario(idUsuario);
                nuevoTecnico.setEspecialidad("General");
                nuevoTecnico.setMaxIncidencias(5);
                nuevoTecnico.setDisponibilidad(true);
                tecnicoRepo.save(nuevoTecnico);
            }
        } else {
            // Si el usuario ya no es Técnico, quitar su registro de tecnicos (si existia)
            if (tecnicoRepo.existsById(idUsuario)) {
                tecnicoRepo.deleteById(idUsuario);
            }
        }
    }

    // ELIMINAR
    @Transactional
    public void eliminar(Integer id) {
        if (!repo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No existe el usuario con id: " + id);
        }
        // Si era técnico, eliminar su registro de tecnicos primero
        tecnicoRepo.deleteById(id);
        repo.deleteById(id);
    }
}
