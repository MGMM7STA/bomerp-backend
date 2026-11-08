package pe.edu.upeu.bomerp.sedes;

import org.springframework.security.oauth2.jwt.Jwt;
import java.util.Map;
import java.util.Optional;

public interface SedeService {

    Optional<Sede> de(String usuario);

    Map<String, Sede> todas();

    void asignar(String usuario, Sede sede);

    void quitar(String usuario);

    Optional<Sede> alcance(Jwt jwt, Sede solicitada);

    Sede requerida(Jwt jwt, Sede solicitada);
}
