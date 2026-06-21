package Grupo14SpringSoftCorporationBackend.security;

import Grupo14SpringSoftCorporationBackend.model.Usuario;
import Grupo14SpringSoftCorporationBackend.repository.UsuarioRepository;
import Grupo14SpringSoftCorporationBackend.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        String email = jwtUtil.extractEmail(token);

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(email);

            if (usuarioOpt.isPresent() && jwtUtil.isTokenValid(token)) {
                Usuario usuario = usuarioOpt.get();

                // Si el usuario fue desactivado, no autorizamos aunque tenga JWT valido
                if (Boolean.FALSE.equals(usuario.getActivo())) {
                    filterChain.doFilter(request, response);
                    return;
                }

                // Mapear idPerfil a ROLE_* usando el dato actualizado de la BD,
                // no el que viene del token (evita escalada de privilegios si el
                // token fuera fabricado o si el rol cambio).
                String rol = switch (usuario.getIdPerfil()) {
                    case 1 -> "ROLE_JEFE";
                    case 2 -> "ROLE_TECNICO";
                    case 3 -> "ROLE_SISTEMAS";
                    default -> "ROLE_USUARIO";
                };

                SimpleGrantedAuthority authority = new SimpleGrantedAuthority(rol);

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                usuario,
                                null,
                                Collections.singletonList(authority)
                        );

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
