package pe.edu.upeu.bomerp;

import java.util.List;

public record PerfilResponse(
        String sub,
        String usuario,
        String email,
        String nombre,
        List<String> roles) {
}
