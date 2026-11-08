package pe.edu.upeu.bomerp.participacion.estudiante.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.bomerp.campanias.campania.service.CampaniaService;
import pe.edu.upeu.bomerp.participacion.estudiante.entity.Estudiante;
import pe.edu.upeu.bomerp.participacion.estudiante.repository.EstudianteRepository;
import pe.edu.upeu.bomerp.participacion.registro.repository.RegistroRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import pe.edu.upeu.bomerp.campanias.campania.dto.CampaniaResponse;
import pe.edu.upeu.bomerp.sedes.Sede;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ExportacionOdooServiceImpl implements ExportacionOdooService {

    private final EstudianteRepository estudianteRepository;
    private final RegistroRepository registroRepository;
    private final CampaniaService campaniaService;

    @Value("${bomerp.odoo.programa-puntos:Puntos Saludables}")
    private String programaPuntos;

    @Override
    @Transactional(readOnly = true)
    public String contactos(Sede sede) {
        StringBuilder csv = new StringBuilder();
        fila(csv, "id", "name", "email", "phone", "ref", "function", "category_id",
                "x_dni", "x_escuela", "x_ciclo", "x_sede");
        for (Estudiante e : ordenados(sede)) {
            fila(csv, idContacto(e), e.getNombre(), e.getCorreo(), e.getCelular(), e.getCodigoUniversitario(),
                    funcion(e), "Estudiante", e.getDni(), e.getEscuela(),
                    e.getCiclo() == null ? "" : e.getCiclo().toString(),
                    e.getSede() == null ? "" : e.getSede().name());
        }
        return csv.toString();
    }

    @Override
    @Transactional(readOnly = true)
    public String puntos(Sede sede) {
        StringBuilder csv = new StringBuilder();
        fila(csv, "id", "program_id", "partner_id/id", "points");
        for (Sede actual : sede == null ? List.of(Sede.values()) : List.of(sede)) {
            Optional<CampaniaResponse> campania = campaniaService.buscarActiva(actual);
            if (campania.isEmpty()) {
                continue;
            }
            Long id = campania.get().getId();
            Map<Long, BigDecimal> puntos = new HashMap<>();
            for (Object[] fila : registroRepository.puntosValidadosPorEstudiante(id)) {
                puntos.put((Long) fila[0], (BigDecimal) fila[1]);
            }
            for (Estudiante e : ordenados(actual)) {
                BigDecimal valor = puntos.getOrDefault(e.getId(), BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
                fila(csv, "bomerp_tarjeta_c" + id + "_e" + e.getId(), programaPuntos, idContacto(e),
                        valor.toPlainString());
            }
        }
        return csv.toString();
    }

    private List<Estudiante> ordenados(Sede sede) {
        List<Estudiante> lista = sede == null ? estudianteRepository.findAll() : estudianteRepository.findBySede(sede);
        return lista.stream()
                .sorted(Comparator.comparing(Estudiante::getNombre, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private String idContacto(Estudiante e) {
        return "bomerp_estudiante_" + e.getId();
    }

    private String funcion(Estudiante e) {
        if (e.getEscuela() == null) {
            return "Estudiante";
        }
        return e.getCiclo() == null ? e.getEscuela() : e.getEscuela() + " - ciclo " + e.getCiclo();
    }

    private void fila(StringBuilder csv, String... valores) {
        csv.append(Stream.of(valores).map(this::celda).collect(Collectors.joining(","))).append("\r\n");
    }

    private String celda(String valor) {
        if (valor == null) {
            return "";
        }
        String limpio = valor.replace("\"", "\"\"");
        return limpio.matches(".*[\",\\r\\n].*") ? "\"" + limpio + "\"" : limpio;
    }
}
