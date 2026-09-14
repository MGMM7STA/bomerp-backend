# LP2 - S04 (Crea) - Semestre Saludable  [v2, alineado al brief del equipo]
# Modulos: campanias (Campania, Habito, CupoDiario) + participacion (RegistroParticipacion / DetalleEvidencia)
# Ejecutar desde la raiz del proyecto.

$ErrorActionPreference = "Stop"
$base = "src\main\java\pe\edu\upeu\bomerp"
$utf8 = New-Object System.Text.UTF8Encoding($false)

function Escribir($ruta, $texto) {
  $dir = Split-Path $ruta -Parent
  if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Force -Path $dir | Out-Null }
  [System.IO.File]::WriteAllText((Join-Path (Get-Location) $ruta), $texto, $utf8)
  Write-Host "  $ruta"
}

# El intento anterior de hoy (modulo 'habitos') se reemplaza por 'campanias'.
if (Test-Path "$base\habitos") {
  Remove-Item -Recurse -Force "$base\habitos"
  Write-Host "Eliminado el modulo provisional 'habitos'." -ForegroundColor Yellow
}

Write-Host "== Modulo campanias : habito =="

Escribir "$base\campanias\habito\entity\Habito.java" @'
package pe.edu.upeu.bomerp.campanias.habito.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "HABITOS", schema = "BOM_CAMPANIAS")
@Getter
@Setter
@NoArgsConstructor
public class Habito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NOMBRE", nullable = false, length = 120)
    private String nombre;

    @Column(name = "DESCRIPCION", length = 255)
    private String descripcion;

    @Column(name = "PUNTAJE_BASE", nullable = false, precision = 10, scale = 2)
    private BigDecimal puntajeBase;

    @Column(name = "ACTIVO", nullable = false)
    private Integer activo;
}
'@

Escribir "$base\campanias\habito\dto\HabitoResponse.java" @'
package pe.edu.upeu.bomerp.campanias.habito.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HabitoResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal puntajeBase;
    private Integer activo;
}
'@

Escribir "$base\campanias\habito\dto\package-info.java" @'
@org.springframework.modulith.NamedInterface("habito-dto")
package pe.edu.upeu.bomerp.campanias.habito.dto;
'@

Escribir "$base\campanias\habito\mapper\HabitoMapper.java" @'
package pe.edu.upeu.bomerp.campanias.habito.mapper;

import org.mapstruct.Mapper;
import pe.edu.upeu.bomerp.campanias.habito.dto.HabitoResponse;
import pe.edu.upeu.bomerp.campanias.habito.entity.Habito;

@Mapper(componentModel = "spring")
public interface HabitoMapper {
    HabitoResponse toResponse(Habito habito);
}
'@

Escribir "$base\campanias\habito\repository\HabitoRepository.java" @'
package pe.edu.upeu.bomerp.campanias.habito.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.bomerp.campanias.habito.entity.Habito;

public interface HabitoRepository extends JpaRepository<Habito, Long> {
}
'@

Escribir "$base\campanias\habito\service\HabitoService.java" @'
package pe.edu.upeu.bomerp.campanias.habito.service;

import pe.edu.upeu.bomerp.campanias.habito.dto.HabitoResponse;
import java.util.List;

public interface HabitoService {
    List<HabitoResponse> listar();
    HabitoResponse obtener(Long id);
}
'@

Escribir "$base\campanias\habito\service\package-info.java" @'
@org.springframework.modulith.NamedInterface("habito-service")
package pe.edu.upeu.bomerp.campanias.habito.service;
'@

Escribir "$base\campanias\habito\service\HabitoServiceImpl.java" @'
package pe.edu.upeu.bomerp.campanias.habito.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.bomerp.campanias.habito.dto.HabitoResponse;
import pe.edu.upeu.bomerp.campanias.habito.entity.Habito;
import pe.edu.upeu.bomerp.campanias.habito.mapper.HabitoMapper;
import pe.edu.upeu.bomerp.campanias.habito.repository.HabitoRepository;
import pe.edu.upeu.bomerp.exception.ResourceNotFoundException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HabitoServiceImpl implements HabitoService {

    private final HabitoRepository habitoRepository;
    private final HabitoMapper habitoMapper;

    @Override
    @Transactional(readOnly = true)
    public List<HabitoResponse> listar() {
        return habitoRepository.findAll().stream().map(habitoMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public HabitoResponse obtener(Long id) {
        Habito habito = habitoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Habito no encontrado: " + id));
        return habitoMapper.toResponse(habito);
    }
}
'@

Escribir "$base\campanias\habito\controller\HabitoController.java" @'
package pe.edu.upeu.bomerp.campanias.habito.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.bomerp.campanias.habito.dto.HabitoResponse;
import pe.edu.upeu.bomerp.campanias.habito.service.HabitoService;
import java.util.List;

@Tag(name = "Habitos")
@RestController
@RequestMapping("/api/v1/habitos")
@RequiredArgsConstructor
public class HabitoController {

    private final HabitoService habitoService;

    @Operation(summary = "Lista los habitos de la campania")
    @GetMapping
    public ResponseEntity<List<HabitoResponse>> listar() {
        return ResponseEntity.ok(habitoService.listar());
    }

    @Operation(summary = "Consulta un habito por id")
    @GetMapping("/{id}")
    public ResponseEntity<HabitoResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(habitoService.obtener(id));
    }
}
'@

Write-Host "== Modulo campanias : campania y cupo diario =="

Escribir "$base\campanias\campania\entity\Campania.java" @'
package pe.edu.upeu.bomerp.campanias.campania.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "CAMPANIAS", schema = "BOM_CAMPANIAS")
@Getter
@Setter
@NoArgsConstructor
public class Campania {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NOMBRE", nullable = false, length = 120)
    private String nombre;

    @Column(name = "FECHA_INICIO", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "FECHA_FIN", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "MAX_EVIDENCIAS_DIA", nullable = false)
    private Integer maxEvidenciasDia;

    @Column(name = "ESTADO", nullable = false, length = 20)
    private String estado;
}
'@

Escribir "$base\campanias\campania\entity\CupoDiario.java" @'
package pe.edu.upeu.bomerp.campanias.campania.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "CUPOS_DIARIOS", schema = "BOM_CAMPANIAS")
@Getter
@Setter
@NoArgsConstructor
public class CupoDiario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "ID_CAMPANIA", nullable = false)
    private Long campaniaId;

    @Column(name = "ID_ESTUDIANTE", nullable = false)
    private Long estudianteId;

    @Column(name = "FECHA", nullable = false)
    private LocalDate fecha;

    @Column(name = "EVIDENCIAS_USADAS", nullable = false)
    private Integer evidenciasUsadas;
}
'@

Escribir "$base\campanias\campania\dto\CampaniaResponse.java" @'
package pe.edu.upeu.bomerp.campanias.campania.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaniaResponse {
    private Long id;
    private String nombre;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Integer maxEvidenciasDia;
    private String estado;
}
'@

Escribir "$base\campanias\campania\dto\package-info.java" @'
@org.springframework.modulith.NamedInterface("campania-dto")
package pe.edu.upeu.bomerp.campanias.campania.dto;
'@

Escribir "$base\campanias\campania\mapper\CampaniaMapper.java" @'
package pe.edu.upeu.bomerp.campanias.campania.mapper;

import org.mapstruct.Mapper;
import pe.edu.upeu.bomerp.campanias.campania.dto.CampaniaResponse;
import pe.edu.upeu.bomerp.campanias.campania.entity.Campania;

@Mapper(componentModel = "spring")
public interface CampaniaMapper {
    CampaniaResponse toResponse(Campania campania);
}
'@

Escribir "$base\campanias\campania\repository\CampaniaRepository.java" @'
package pe.edu.upeu.bomerp.campanias.campania.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.bomerp.campanias.campania.entity.Campania;
import java.util.Optional;

public interface CampaniaRepository extends JpaRepository<Campania, Long> {
    Optional<Campania> findFirstByEstado(String estado);
}
'@

Escribir "$base\campanias\campania\repository\CupoDiarioRepository.java" @'
package pe.edu.upeu.bomerp.campanias.campania.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.bomerp.campanias.campania.entity.CupoDiario;
import java.time.LocalDate;
import java.util.Optional;

public interface CupoDiarioRepository extends JpaRepository<CupoDiario, Long> {
    Optional<CupoDiario> findByCampaniaIdAndEstudianteIdAndFecha(Long campaniaId, Long estudianteId, LocalDate fecha);
}
'@

Escribir "$base\campanias\campania\service\CampaniaService.java" @'
package pe.edu.upeu.bomerp.campanias.campania.service;

import pe.edu.upeu.bomerp.campanias.campania.dto.CampaniaResponse;
import java.time.LocalDate;
import java.util.List;

public interface CampaniaService {
    List<CampaniaResponse> listar();
    CampaniaResponse obtener(Long id);
    CampaniaResponse obtenerActiva();
    void consumirCupo(Long campaniaId, Long estudianteId, LocalDate fecha, Integer veces);
}
'@

Escribir "$base\campanias\campania\service\package-info.java" @'
@org.springframework.modulith.NamedInterface("campania-service")
package pe.edu.upeu.bomerp.campanias.campania.service;
'@

Escribir "$base\campanias\campania\service\CampaniaServiceImpl.java" @'
package pe.edu.upeu.bomerp.campanias.campania.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.bomerp.campanias.campania.dto.CampaniaResponse;
import pe.edu.upeu.bomerp.campanias.campania.entity.Campania;
import pe.edu.upeu.bomerp.campanias.campania.entity.CupoDiario;
import pe.edu.upeu.bomerp.campanias.campania.mapper.CampaniaMapper;
import pe.edu.upeu.bomerp.campanias.campania.repository.CampaniaRepository;
import pe.edu.upeu.bomerp.campanias.campania.repository.CupoDiarioRepository;
import pe.edu.upeu.bomerp.exception.CupoDiarioExcedidoException;
import pe.edu.upeu.bomerp.exception.ResourceNotFoundException;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CampaniaServiceImpl implements CampaniaService {

    private final CampaniaRepository campaniaRepository;
    private final CupoDiarioRepository cupoDiarioRepository;
    private final CampaniaMapper campaniaMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CampaniaResponse> listar() {
        return campaniaRepository.findAll().stream().map(campaniaMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CampaniaResponse obtener(Long id) {
        return campaniaMapper.toResponse(buscarOFallar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public CampaniaResponse obtenerActiva() {
        Campania campania = campaniaRepository.findFirstByEstado("ACTIVA")
                .orElseThrow(() -> new ResourceNotFoundException("No hay ninguna campania activa"));
        return campaniaMapper.toResponse(campania);
    }

    @Override
    @Transactional
    public void consumirCupo(Long campaniaId, Long estudianteId, LocalDate fecha, Integer veces) {
        Campania campania = buscarOFallar(campaniaId);

        CupoDiario cupo = cupoDiarioRepository
                .findByCampaniaIdAndEstudianteIdAndFecha(campaniaId, estudianteId, fecha)
                .orElseGet(() -> {
                    CupoDiario nuevo = new CupoDiario();
                    nuevo.setCampaniaId(campaniaId);
                    nuevo.setEstudianteId(estudianteId);
                    nuevo.setFecha(fecha);
                    nuevo.setEvidenciasUsadas(0);
                    return nuevo;
                });

        int usadas = cupo.getEvidenciasUsadas() + veces;
        if (usadas > campania.getMaxEvidenciasDia()) {
            throw new CupoDiarioExcedidoException(
                    "Cupo diario excedido: la campania " + campania.getNombre()
                            + " permite " + campania.getMaxEvidenciasDia()
                            + " evidencias por dia; el estudiante " + estudianteId
                            + " ya uso " + cupo.getEvidenciasUsadas()
                            + " y esta intentando registrar " + veces + " mas");
        }
        cupo.setEvidenciasUsadas(usadas);
        cupoDiarioRepository.save(cupo);
    }

    private Campania buscarOFallar(Long id) {
        return campaniaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campania no encontrada: " + id));
    }
}
'@

Escribir "$base\campanias\campania\controller\CampaniaController.java" @'
package pe.edu.upeu.bomerp.campanias.campania.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.bomerp.campanias.campania.dto.CampaniaResponse;
import pe.edu.upeu.bomerp.campanias.campania.service.CampaniaService;
import java.util.List;

@Tag(name = "Campanias")
@RestController
@RequestMapping("/api/v1/campanias")
@RequiredArgsConstructor
public class CampaniaController {

    private final CampaniaService campaniaService;

    @Operation(summary = "Lista las campanias")
    @GetMapping
    public ResponseEntity<List<CampaniaResponse>> listar() {
        return ResponseEntity.ok(campaniaService.listar());
    }

    @Operation(summary = "Devuelve la campania activa con su maximo de evidencias por dia")
    @GetMapping("/activa")
    public ResponseEntity<CampaniaResponse> activa() {
        return ResponseEntity.ok(campaniaService.obtenerActiva());
    }

    @Operation(summary = "Consulta una campania por id")
    @GetMapping("/{id}")
    public ResponseEntity<CampaniaResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(campaniaService.obtener(id));
    }
}
'@

Write-Host "== Modulo participacion =="

Escribir "$base\participacion\registro\entity\EstadoRegistro.java" @'
package pe.edu.upeu.bomerp.participacion.registro.entity;

public enum EstadoRegistro {
    BORRADOR,
    ENVIADO,
    VALIDADO,
    RECHAZADO
}
'@

Escribir "$base\participacion\registro\entity\RegistroParticipacion.java" @'
package pe.edu.upeu.bomerp.participacion.registro.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "REGISTROS", schema = "BOM_PARTICIPACION")
@Getter
@Setter
@NoArgsConstructor
public class RegistroParticipacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "ID_ESTUDIANTE", nullable = false)
    private Long estudianteId;

    @Column(name = "ID_CAMPANIA", nullable = false)
    private Long campaniaId;

    @Column(name = "FECHA", nullable = false)
    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false, length = 20)
    private EstadoRegistro estado;

    @Column(name = "TOTAL_EVIDENCIAS", nullable = false)
    private Integer totalEvidencias;

    @Column(name = "PUNTAJE_TOTAL", nullable = false, precision = 12, scale = 2)
    private BigDecimal puntajeTotal;

    @OneToMany(mappedBy = "registro", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleEvidencia> detalles = new ArrayList<>();
}
'@

Escribir "$base\participacion\registro\entity\DetalleEvidencia.java" @'
package pe.edu.upeu.bomerp.participacion.registro.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "DETALLE_EVIDENCIAS", schema = "BOM_PARTICIPACION")
@Getter
@Setter
@NoArgsConstructor
public class DetalleEvidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ID_REGISTRO", nullable = false)
    private RegistroParticipacion registro;

    @Column(name = "ID_HABITO", nullable = false)
    private Long habitoId;

    @Column(name = "NOMBRE_HABITO", nullable = false, length = 120)
    private String nombreHabito;

    @Column(name = "DESCRIPCION", length = 255)
    private String descripcion;

    @Column(name = "URL_IMAGEN", length = 255)
    private String urlImagen;

    @Column(name = "PUNTAJE_UNITARIO", nullable = false, precision = 10, scale = 2)
    private BigDecimal puntajeUnitario;

    @Column(name = "VECES", nullable = false)
    private Integer veces;

    @Column(name = "SUBTOTAL", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;
}
'@

Escribir "$base\participacion\registro\dto\DetalleEvidenciaRequest.java" @'
package pe.edu.upeu.bomerp.participacion.registro.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DetalleEvidenciaRequest {

    @NotNull
    private Long habitoId;

    @NotNull
    @Positive
    private Integer veces;

    private String descripcion;

    private String urlImagen;
}
'@

Escribir "$base\participacion\registro\dto\RegistroRequest.java" @'
package pe.edu.upeu.bomerp.participacion.registro.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class RegistroRequest {

    @NotNull
    private Long estudianteId;

    @NotEmpty
    @Valid
    private List<DetalleEvidenciaRequest> detalles;
}
'@

Escribir "$base\participacion\registro\dto\DetalleEvidenciaResponse.java" @'
package pe.edu.upeu.bomerp.participacion.registro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleEvidenciaResponse {
    private Long habitoId;
    private String nombreHabito;
    private String descripcion;
    private String urlImagen;
    private BigDecimal puntajeUnitario;
    private Integer veces;
    private BigDecimal subtotal;
}
'@

Escribir "$base\participacion\registro\dto\RegistroResponse.java" @'
package pe.edu.upeu.bomerp.participacion.registro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistroResponse {
    private Long id;
    private Long estudianteId;
    private Long campaniaId;
    private LocalDateTime fecha;
    private String estado;
    private Integer totalEvidencias;
    private BigDecimal puntajeTotal;
    private List<DetalleEvidenciaResponse> detalles;
}
'@

Escribir "$base\participacion\registro\mapper\RegistroMapper.java" @'
package pe.edu.upeu.bomerp.participacion.registro.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pe.edu.upeu.bomerp.campanias.habito.dto.HabitoResponse;
import pe.edu.upeu.bomerp.participacion.registro.dto.DetalleEvidenciaRequest;
import pe.edu.upeu.bomerp.participacion.registro.dto.DetalleEvidenciaResponse;
import pe.edu.upeu.bomerp.participacion.registro.dto.RegistroResponse;
import pe.edu.upeu.bomerp.participacion.registro.entity.DetalleEvidencia;
import pe.edu.upeu.bomerp.participacion.registro.entity.RegistroParticipacion;

@Mapper(componentModel = "spring")
public interface RegistroMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "registro", ignore = true)
    @Mapping(target = "habitoId", source = "habito.id")
    @Mapping(target = "nombreHabito", source = "habito.nombre")
    @Mapping(target = "puntajeUnitario", source = "habito.puntajeBase")
    @Mapping(target = "descripcion", source = "request.descripcion")
    @Mapping(target = "urlImagen", source = "request.urlImagen")
    @Mapping(target = "veces", source = "request.veces")
    @Mapping(target = "subtotal", expression = "java(habito.getPuntajeBase().multiply(java.math.BigDecimal.valueOf(request.getVeces())))")
    DetalleEvidencia toDetalle(DetalleEvidenciaRequest request, HabitoResponse habito);

    RegistroResponse toResponse(RegistroParticipacion registro);

    DetalleEvidenciaResponse toDetalleResponse(DetalleEvidencia detalle);
}
'@

Escribir "$base\participacion\registro\repository\RegistroRepository.java" @'
package pe.edu.upeu.bomerp.participacion.registro.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.bomerp.participacion.registro.entity.RegistroParticipacion;
import java.util.List;
import java.util.Optional;

public interface RegistroRepository extends JpaRepository<RegistroParticipacion, Long> {

    @Override
    @EntityGraph(attributePaths = "detalles")
    List<RegistroParticipacion> findAll();

    @Override
    @EntityGraph(attributePaths = "detalles")
    Optional<RegistroParticipacion> findById(Long id);
}
'@

Escribir "$base\participacion\registro\service\RegistroService.java" @'
package pe.edu.upeu.bomerp.participacion.registro.service;

import pe.edu.upeu.bomerp.participacion.registro.dto.RegistroRequest;
import pe.edu.upeu.bomerp.participacion.registro.dto.RegistroResponse;
import java.util.List;

public interface RegistroService {
    List<RegistroResponse> listar();
    RegistroResponse obtener(Long id);
    RegistroResponse crear(RegistroRequest request);
}
'@

Escribir "$base\participacion\registro\service\RegistroServiceImpl.java" @'
package pe.edu.upeu.bomerp.participacion.registro.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.bomerp.campanias.campania.dto.CampaniaResponse;
import pe.edu.upeu.bomerp.campanias.campania.service.CampaniaService;
import pe.edu.upeu.bomerp.campanias.habito.dto.HabitoResponse;
import pe.edu.upeu.bomerp.campanias.habito.service.HabitoService;
import pe.edu.upeu.bomerp.exception.ResourceNotFoundException;
import pe.edu.upeu.bomerp.participacion.registro.dto.DetalleEvidenciaRequest;
import pe.edu.upeu.bomerp.participacion.registro.dto.RegistroRequest;
import pe.edu.upeu.bomerp.participacion.registro.dto.RegistroResponse;
import pe.edu.upeu.bomerp.participacion.registro.entity.DetalleEvidencia;
import pe.edu.upeu.bomerp.participacion.registro.entity.EstadoRegistro;
import pe.edu.upeu.bomerp.participacion.registro.entity.RegistroParticipacion;
import pe.edu.upeu.bomerp.participacion.registro.mapper.RegistroMapper;
import pe.edu.upeu.bomerp.participacion.registro.repository.RegistroRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RegistroServiceImpl implements RegistroService {

    private final RegistroRepository registroRepository;
    private final HabitoService habitoService;
    private final CampaniaService campaniaService;
    private final RegistroMapper registroMapper;

    @Override
    @Transactional(readOnly = true)
    public List<RegistroResponse> listar() {
        return registroRepository.findAll().stream().map(registroMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RegistroResponse obtener(Long id) {
        RegistroParticipacion registro = registroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registro no encontrado: " + id));
        return registroMapper.toResponse(registro);
    }

    @Override
    @Transactional
    public RegistroResponse crear(RegistroRequest request) {
        CampaniaResponse campania = campaniaService.obtenerActiva();
        LocalDate hoy = LocalDate.now();

        RegistroParticipacion registro = new RegistroParticipacion();
        registro.setEstudianteId(request.getEstudianteId());
        registro.setCampaniaId(campania.getId());
        registro.setFecha(LocalDateTime.now());
        registro.setEstado(EstadoRegistro.ENVIADO);

        BigDecimal puntajeTotal = BigDecimal.ZERO;
        int totalEvidencias = 0;

        for (DetalleEvidenciaRequest detalleRequest : request.getDetalles()) {
            HabitoResponse habito = habitoService.obtener(detalleRequest.getHabitoId());

            campaniaService.consumirCupo(
                    campania.getId(), request.getEstudianteId(), hoy, detalleRequest.getVeces());

            DetalleEvidencia detalle = registroMapper.toDetalle(detalleRequest, habito);
            detalle.setRegistro(registro);
            registro.getDetalles().add(detalle);

            puntajeTotal = puntajeTotal.add(detalle.getSubtotal());
            totalEvidencias += detalleRequest.getVeces();
        }

        registro.setPuntajeTotal(puntajeTotal);
        registro.setTotalEvidencias(totalEvidencias);

        return registroMapper.toResponse(registroRepository.save(registro));
    }
}
'@

Escribir "$base\participacion\registro\controller\RegistroController.java" @'
package pe.edu.upeu.bomerp.participacion.registro.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.bomerp.participacion.registro.dto.RegistroRequest;
import pe.edu.upeu.bomerp.participacion.registro.dto.RegistroResponse;
import pe.edu.upeu.bomerp.participacion.registro.service.RegistroService;
import java.util.List;

@Tag(name = "Participacion")
@RestController
@RequestMapping("/api/v1/participacion/registros")
@RequiredArgsConstructor
public class RegistroController {

    private final RegistroService registroService;

    @Operation(summary = "Lista los registros de participacion")
    @GetMapping
    public ResponseEntity<List<RegistroResponse>> listar() {
        return ResponseEntity.ok(registroService.listar());
    }

    @Operation(summary = "Consulta un registro con sus evidencias")
    @GetMapping("/{id}")
    public ResponseEntity<RegistroResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(registroService.obtener(id));
    }

    @Operation(summary = "Registra la participacion diaria consumiendo el cupo de la campania")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RegistroResponse crear(@Valid @RequestBody RegistroRequest request) {
        return registroService.crear(request);
    }
}
'@

Write-Host ""
Write-Host "Listo. Modulos campanias y participacion creados (sin BOM)." -ForegroundColor Green
