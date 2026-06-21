package Grupo14SpringSoftCorporationBackend.repository;

import Grupo14SpringSoftCorporationBackend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByCorreo(String correo);
}