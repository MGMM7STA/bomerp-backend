package pe.edu.upeu.bomerp.sedes.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.bomerp.sedes.Sede;
import pe.edu.upeu.bomerp.sedes.SedeService;
import pe.edu.upeu.bomerp.sedes.entity.SedeUsuario;
import pe.edu.upeu.bomerp.sedes.repository.SedeUsuarioRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SedeServiceImpl implements SedeService {

    private final SedeUsuarioRepository repository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Sede> de(String usuario) {
        return usuario == null ? Optional.empty() : repository.findById(usuario).map(SedeUsuario::getSede);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Sede> todas() {
        return repository.findAll().stream().collect(Collectors.toMap(SedeUsuario::getUsuario, SedeUsuario::getSede));
    }

    @Override
    @Transactional
    public void asignar(String usuario, Sede sede) {
        SedeUsuario registro = repository.findById(usuario).orElseGet(SedeUsuario::new);
        registro.setUsuario(usuario);
        registro.setSede(sede);
        repository.save(registro);
    }

    @Override
    @Transactional
    public void quitar(String usuario) {
        repository.deleteById(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Sede> alcance(Jwt jwt, Sede solicitada) {
        if (esAdmin(jwt)) {
            return Optional.ofNullable(solicitada);
        }
        return Optional.of(propia(jwt));
    }

    @Override
    @Transactional(readOnly = true)
    public Sede requerida(Jwt jwt, Sede solicitada) {
        return alcance(jwt, solicitada).orElseThrow(
                () -> new IllegalArgumentException("Elige una sede para ver esta informacion"));
    }

    private Sede propia(Jwt jwt) {
        return de(jwt.getClaimAsString("preferred_username")).orElseThrow(
                () -> new AccessDeniedException("Tu cuenta no tiene una sede asignada. Pide al administrador que la asigne."));
    }

    private boolean esAdmin(Jwt jwt) {
        Object acceso = jwt.getClaim("realm_access");
        if (acceso instanceof Map<?, ?> mapa && mapa.get("roles") instanceof List<?> roles) {
            return roles.contains("ADMIN");
        }
        return false;
    }
}
