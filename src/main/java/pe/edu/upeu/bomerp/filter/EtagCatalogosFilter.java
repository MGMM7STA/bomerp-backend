package pe.edu.upeu.bomerp.filter;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import java.util.Set;

@Component
public class EtagCatalogosFilter extends ShallowEtagHeaderFilter {

    private static final Set<String> RUTAS = Set.of("/api/v1/categorias-habito", "/api/v1/habitos");

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"GET".equals(request.getMethod()) || !RUTAS.contains(request.getRequestURI());
    }
}
