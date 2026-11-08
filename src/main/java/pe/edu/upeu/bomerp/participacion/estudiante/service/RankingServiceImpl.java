package pe.edu.upeu.bomerp.participacion.estudiante.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.bomerp.campanias.campania.dto.CampaniaResponse;
import pe.edu.upeu.bomerp.campanias.campania.service.CampaniaService;
import pe.edu.upeu.bomerp.participacion.estudiante.dto.RankingResponse;
import pe.edu.upeu.bomerp.participacion.estudiante.entity.Estudiante;
import pe.edu.upeu.bomerp.participacion.estudiante.repository.EstudianteRepository;
import pe.edu.upeu.bomerp.participacion.registro.repository.RegistroRepository;
import pe.edu.upeu.bomerp.sedes.Sede;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RankingServiceImpl implements RankingService {

    private final CampaniaService campaniaService;
    private final RegistroRepository registroRepository;
    private final EstudianteRepository estudianteRepository;

    @Override
    @Transactional(readOnly = true)
    public RankingResponse ranking(Sede sede, Long campaniaId, Long estudianteId, int limite) {
        CampaniaResponse campania = campaniaId == null
                ? campaniaService.obtenerActiva(sede)
                : campaniaService.obtener(campaniaId);
        if (!campania.getSedes().contains(sede.name())) {
            throw new IllegalArgumentException("La campania " + campania.getNombre() + " no se realiza en esta sede");
        }
        Map<Long, Estudiante> estudiantes = estudianteRepository.findBySede(sede).stream()
                .collect(Collectors.toMap(Estudiante::getId, Function.identity()));

        List<Object[]> puntos = new ArrayList<>(registroRepository.puntosValidadosPorEstudiante(campania.getId()));
        puntos.removeIf(fila -> !estudiantes.containsKey((Long) fila[0]));
        puntos.sort(Comparator.<Object[], BigDecimal>comparing(fila -> (BigDecimal) fila[1]).reversed()
                .thenComparing(fila -> ((Number) fila[2]).longValue(), Comparator.reverseOrder()));

        List<RankingResponse.Fila> filas = new ArrayList<>();
        RankingResponse.Fila mia = null;
        int posicion = 0;
        BigDecimal anterior = null;
        for (int i = 0; i < puntos.size(); i++) {
            Object[] fila = puntos.get(i);
            BigDecimal valor = (BigDecimal) fila[1];
            if (anterior == null || valor.compareTo(anterior) != 0) {
                posicion = i + 1;
                anterior = valor;
            }
            Estudiante e = estudiantes.get((Long) fila[0]);
            RankingResponse.Fila actual = new RankingResponse.Fila(posicion, e.getId(), e.getNombre(), e.getEscuela(),
                    valor, ((Number) fila[2]).longValue());
            if (i < limite) {
                filas.add(actual);
            }
            if (e.getId().equals(estudianteId)) {
                mia = actual;
            }
        }
        return new RankingResponse(campania.getId(), campania.getNombre(), sede.name(), filas, mia, puntos.size());
    }
}
