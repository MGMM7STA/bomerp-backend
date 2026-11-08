package pe.edu.upeu.bomerp.participacion;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import pe.edu.upeu.bomerp.campanias.campania.dto.CampaniaRequest;
import pe.edu.upeu.bomerp.campanias.campania.dto.CampaniaResponse;
import pe.edu.upeu.bomerp.campanias.campania.service.CampaniaService;
import pe.edu.upeu.bomerp.campanias.categoriahabito.dto.CategoriaHabitoRequest;
import pe.edu.upeu.bomerp.campanias.categoriahabito.service.CategoriaHabitoService;
import pe.edu.upeu.bomerp.campanias.habito.dto.HabitoRequest;
import pe.edu.upeu.bomerp.campanias.habito.dto.HabitoResponse;
import pe.edu.upeu.bomerp.campanias.habito.service.HabitoService;
import pe.edu.upeu.bomerp.exception.CupoDiarioExcedidoException;
import pe.edu.upeu.bomerp.participacion.estudiante.dto.EstudianteResponse;
import pe.edu.upeu.bomerp.participacion.estudiante.dto.NuevoEstudiante;
import pe.edu.upeu.bomerp.participacion.estudiante.dto.RankingResponse;
import pe.edu.upeu.bomerp.participacion.estudiante.service.EstudianteService;
import pe.edu.upeu.bomerp.participacion.estudiante.service.RankingService;
import pe.edu.upeu.bomerp.participacion.registro.dto.DetalleEvidenciaRequest;
import pe.edu.upeu.bomerp.participacion.registro.dto.PaginaResponse;
import pe.edu.upeu.bomerp.participacion.registro.dto.RegistroRequest;
import pe.edu.upeu.bomerp.participacion.registro.dto.RegistroResponse;
import pe.edu.upeu.bomerp.participacion.registro.dto.RegistroTotales;
import pe.edu.upeu.bomerp.participacion.registro.service.RegistroService;
import pe.edu.upeu.bomerp.sedes.Sede;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class ParticipacionEndToEndTest {

    @Autowired
    private CategoriaHabitoService categoriaHabitoService;
    @Autowired
    private HabitoService habitoService;
    @Autowired
    private CampaniaService campaniaService;
    @Autowired
    private EstudianteService estudianteService;
    @Autowired
    private RegistroService registroService;
    @Autowired
    private RankingService rankingService;
    @Autowired
    private JdbcTemplate jdbc;

    private final String marca = "E2E" + System.currentTimeMillis();
    private Long categoriaId;
    private Long habitoId;
    private Long campaniaId;
    private Long estudianteId;
    private String usuario;
    private final List<Long> registros = new ArrayList<>();

    @Test
    void flujoCompletoContraOracleReal() {
        Optional<Sede> libre = Arrays.stream(Sede.values())
                .filter(sede -> campaniaService.buscarActiva(sede).isEmpty())
                .findFirst();
        Assumptions.assumeTrue(libre.isPresent(), "Las tres sedes tienen campania activa; no se puede crear otra");
        Sede sede = libre.get();

        CategoriaHabitoRequest categoria = new CategoriaHabitoRequest();
        categoria.setNombre(marca);
        categoria.setDescripcion("Categoria de la prueba end-to-end");
        categoriaId = categoriaHabitoService.crear(categoria).getId();

        HabitoRequest habitoRequest = new HabitoRequest();
        habitoRequest.setCategoriaId(categoriaId);
        habitoRequest.setNombre(marca + " caminata");
        habitoRequest.setDescripcion("Habito de la prueba end-to-end");
        habitoRequest.setPuntajeBase(new BigDecimal("2.00"));
        habitoRequest.setActivo(1);
        HabitoResponse habito = habitoService.crear(habitoRequest);
        habitoId = habito.getId();

        CampaniaRequest campaniaRequest = new CampaniaRequest();
        campaniaRequest.setNombre(marca + " campania");
        campaniaRequest.setFechaInicio(LocalDate.now().minusDays(1));
        campaniaRequest.setFechaFin(LocalDate.now().plusDays(30));
        campaniaRequest.setMaxEvidenciasDia(3);
        campaniaRequest.setEstado("ACTIVA");
        campaniaRequest.setHabitoIds(List.of(habitoId));
        campaniaRequest.setSedes(List.of(sede));
        CampaniaResponse campania = campaniaService.crear(campaniaRequest);
        campaniaId = campania.getId();
        assertThat(campania.getSedes()).containsExactly(sede.name());
        assertThat(campania.getHabitoIds()).containsExactly(habitoId);

        usuario = marca.toLowerCase();
        EstudianteResponse estudiante = estudianteService.registrar(new NuevoEstudiante(
                "kc-" + marca, usuario, "Estudiante de prueba", usuario + "@upeu.edu.pe",
                null, null, "Ingenieria de Sistemas", 4, null, sede));
        estudianteId = estudiante.getId();

        RegistroResponse primero = registroService.crear(registro(2), estudianteId, sede);
        registros.add(primero.getId());
        assertThat(primero.getTotalEvidencias()).isEqualTo(2);
        assertThat(primero.getPuntajeTotal()).isEqualByComparingTo("4.00");
        assertThat(primero.getSede()).isEqualTo(sede.name());

        RegistroRequest excedido = new RegistroRequest();
        excedido.setDetalles(List.of(detalle(1), detalle(1)));
        assertThatThrownBy(() -> registroService.crear(excedido, estudianteId, sede))
                .isInstanceOf(CupoDiarioExcedidoException.class);
        Integer usadas = jdbc.queryForObject(
                "SELECT EVIDENCIAS_USADAS FROM BOM_CAMPANIAS.CUPOS_DIARIOS WHERE ID_CAMPANIA = ? AND ID_ESTUDIANTE = ?",
                Integer.class, campaniaId, estudianteId);
        assertThat(usadas).as("el rollback debe deshacer el cupo de la primera linea").isEqualTo(2);

        RegistroResponse segundo = registroService.crear(registro(1), estudianteId, sede);
        registros.add(segundo.getId());

        RegistroResponse validado = registroService.validarDetalle(
                primero.getId(), primero.getDetalles().get(0).getId(), "Prueba E2E");
        assertThat(validado.getPuntajeValidado()).isEqualByComparingTo("4.00");

        PaginaResponse<RegistroResponse> pagina = registroService.buscarPagina(
                null, estudianteId, null, null, "fecha", "DESC", sede, 0, 1);
        assertThat(pagina.contenido()).hasSize(1);
        assertThat(pagina.totalElementos()).isEqualTo(2);
        assertThat(pagina.totalPaginas()).isEqualTo(2);

        RegistroTotales totales = registroService.totales(null, estudianteId, null, null, sede);
        assertThat(totales.totalRegistros()).isEqualTo(2);
        assertThat(totales.evidenciasTotales()).isEqualTo(3);
        assertThat(totales.puntajeValidado()).isEqualByComparingTo("4.00");
        assertThat(totales.habitoMasRegistrado()).isEqualTo(habito.getNombre());

        RankingResponse ranking = rankingService.ranking(sede, campaniaId, estudianteId, 10);
        assertThat(ranking.mia()).isNotNull();
        assertThat(ranking.mia().posicion()).isEqualTo(1);
        assertThat(ranking.mia().puntos()).isEqualByComparingTo("4.00");
    }

    private RegistroRequest registro(int veces) {
        RegistroRequest request = new RegistroRequest();
        request.setDetalles(List.of(detalle(veces)));
        return request;
    }

    private DetalleEvidenciaRequest detalle(int veces) {
        DetalleEvidenciaRequest detalle = new DetalleEvidenciaRequest();
        detalle.setHabitoId(habitoId);
        detalle.setVeces(veces);
        detalle.setDescripcion("Evidencia de la prueba end-to-end");
        return detalle;
    }

    @AfterEach
    void limpiar() {
        for (Long id : registros) {
            jdbc.update("DELETE FROM BOM_PARTICIPACION.DETALLE_EVIDENCIAS WHERE ID_REGISTRO = ?", id);
            jdbc.update("DELETE FROM BOM_PARTICIPACION.REGISTROS WHERE ID = ?", id);
        }
        if (campaniaId != null) {
            jdbc.update("DELETE FROM BOM_CAMPANIAS.CUPOS_DIARIOS WHERE ID_CAMPANIA = ?", campaniaId);
            jdbc.update("DELETE FROM BOM_CAMPANIAS.CAMPANIA_HABITOS WHERE ID_CAMPANIA = ?", campaniaId);
            jdbc.update("DELETE FROM BOM_CAMPANIAS.CAMPANIA_SEDES WHERE ID_CAMPANIA = ?", campaniaId);
            jdbc.update("DELETE FROM BOM_CAMPANIAS.CAMPANIAS WHERE ID = ?", campaniaId);
        }
        if (habitoId != null) {
            jdbc.update("DELETE FROM BOM_CAMPANIAS.HABITOS WHERE ID = ?", habitoId);
        }
        if (categoriaId != null) {
            jdbc.update("DELETE FROM BOM_CAMPANIAS.CATEGORIAS_HABITO WHERE ID = ?", categoriaId);
        }
        if (estudianteId != null) {
            jdbc.update("DELETE FROM BOM_PARTICIPACION.ESTUDIANTES WHERE ID = ?", estudianteId);
        }
        if (usuario != null) {
            jdbc.update("DELETE FROM BOM_PARTICIPACION.SEDES_USUARIO WHERE USUARIO = ?", usuario);
        }
    }
}
