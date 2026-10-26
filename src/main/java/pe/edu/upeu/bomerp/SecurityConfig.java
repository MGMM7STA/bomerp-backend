package pe.edu.upeu.bomerp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.config.Customizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] RUTAS_CAMPANIAS = {
            "/api/v1/habitos/**",
            "/api/v1/categorias-habito/**",
            "/api/v1/campanias/**"
    };

    private static final String RUTA_REGISTROS = "/api/v1/participacion/registros";

    private static final String[] RUTAS_PREMIOS = {
            "/api/v1/categorias-premio/**",
            "/api/v1/premios/**"
    };

    private static final String RUTA_ACTAS = "/api/v1/actas-premiacion";

    @Value("${bomerp.cors.allowed-origins}")
    private String origenesPermitidos;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers(HttpMethod.GET, RUTAS_CAMPANIAS).authenticated()
                        .requestMatchers(RUTAS_CAMPANIAS).hasAnyRole("COORDINADOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, RUTA_REGISTROS + "/resumen").hasAnyRole("COORDINADOR", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, RUTA_REGISTROS + "/*/anular").hasAnyRole("COORDINADOR", "ADMIN")
                        .requestMatchers(HttpMethod.POST, RUTA_REGISTROS).hasAnyRole("ESTUDIANTE", "COORDINADOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, RUTA_REGISTROS + "/**").authenticated()
                        .requestMatchers(HttpMethod.GET, RUTAS_PREMIOS).authenticated()
                        .requestMatchers(RUTAS_PREMIOS).hasAnyRole("COORDINADOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, RUTA_ACTAS + "/**").authenticated()
                        .requestMatchers(RUTA_ACTAS, RUTA_ACTAS + "/**").hasAnyRole("COORDINADOR", "ADMIN")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(convertidorDeRoles())));
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuracion = new CorsConfiguration();
        configuracion.setAllowedOrigins(Arrays.stream(origenesPermitidos.split(","))
                .map(String::trim)
                .toList());
        configuracion.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuracion.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/api/**", configuracion);
        return fuente;
    }

    private JwtAuthenticationConverter convertidorDeRoles() {
        JwtAuthenticationConverter convertidor = new JwtAuthenticationConverter();
        convertidor.setJwtGrantedAuthoritiesConverter(SecurityConfig::extraerRoles);
        return convertidor;
    }

    private static Collection<GrantedAuthority> extraerRoles(Jwt jwt) {
        Collection<GrantedAuthority> autoridades = new ArrayList<>();
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null) {
            return autoridades;
        }
        Object roles = realmAccess.get("roles");
        if (!(roles instanceof Collection<?> lista)) {
            return autoridades;
        }
        for (Object rol : lista) {
            autoridades.add(new SimpleGrantedAuthority("ROLE_" + rol));
        }
        return autoridades;
    }
}
