# LP2 - S04 (Crea) - Semestre Saludable
# Crea los modulos habitos y participacion dentro de bomerp-backend.
# Ejecutar desde la raiz del proyecto: .\crear_participacion.ps1

$ErrorActionPreference = "Stop"
$base = "src\main\java\pe\edu\upeu\bomerp"

$dirs = @(
  "$base\habitos\habito\entity",
  "$base\habitos\habito\dto",
  "$base\habitos\habito\mapper",
  "$base\habitos\habito\repository",
  "$base\habitos\habito\service",
  "$base\habitos\habito\controller",
  "$base\participacion\registro\entity",
  "$base\participacion\registro\dto",
  "$base\participacion\registro\mapper",
  "$base\participacion\registro\repository",
  "$base\participacion\registro\service",
  "$base\participacion\registro\controller"
)
foreach ($d in $dirs) { New-Item -ItemType Directory -Force -Path $d | Out-Null }

function Escribir($ruta, $texto) {
  Set-Content -Path $ruta -Value $texto -Encoding UTF8
  Write-Host "  creado: $ruta"
}

Write-Host "== Modulo habitos =="

Escribir "$base\habitos\habito\entity\Habito.java" @'
package pe.edu.upeu.bomerp.habitos.habito.entity;

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
@Table(name = "HABITOS", schema = "BOM_HABITOS")
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

    @Column(name = "PUNTAJE_UNITARIO", nullable = false, precision = 10, scale = 2)
    private BigDecimal puntajeUnitario;

    @Column(name = "CUPO_DIARIO", nullable = false)
    private Integer cupoDiario;

    @Column(name = "CUPO_DISPONIBLE", nullable = false)
    private Integer cupoDisponible;

    @Column(name = "ACTIVO", nullable = false)
    private Integer activo;
}
'@

Escribir "$base\habitos\habito\dto\HabitoResponse.java" @'
package pe.edu.upeu.bomerp.habitos.habito.dto;

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
    private BigDecimal puntajeUnitario;
    private Integer cupoDiario;
    private Integer cupoDisponible;
    private Integer activo;
}
'@

Escribir "$base\habitos\habito\dto\package-info.java" @'
@org.springframework.modulith.NamedInterface("habito-dto")
package pe.edu.upeu.bomerp.habitos.habito.dto;
'@

Escribir "$base\habitos\habito\mapper\HabitoMapper.java" @'
package pe.edu.upeu.bomerp.habitos.habito.mapper;

import org.mapstruct.Mapper;
import pe.edu.upeu.bomerp.habitos.habito.dto.HabitoResponse;
import pe.edu.upeu.bomerp.habitos.habito.entity.Habito;

@Mapper(componentModel = "spring")
public interface HabitoMapper {
    HabitoResponse toResponse(Habito habito);
}
'@

Escribir "$base\habitos\habito\repository\HabitoRepository.java" @'
package pe.edu.upeu.bomerp.habitos.habito.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.bomerp.habitos.habito.entity.Habito;

public interface HabitoRepository extends JpaRepository<Habito, Long> {
}
'@

Escribir "$base\habitos\habito\service\HabitoService.java" @'
package pe.edu.upeu.bomerp.habitos.habito.service;

import pe.edu.upeu.bomerp.habitos.habito.dto.HabitoResponse;
import java.util.List;

public interface HabitoService {
    List<HabitoResponse> listar();
    HabitoResponse obtener(Long id);
    void descontarCupo(Long id, Integer cantidad);
}
'@

Escribir "$base\habitos\habito\service\package-info.java" @'
@org.springframework.modulith.NamedInterface("habito-service")
package pe.edu.upeu.bomerp.habitos.habito.service;
'@

Escribir "$base\habitos\habito\service\HabitoServiceImpl.java" @'
package pe.edu.upeu.bomerp.habitos.habito.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.bomerp.exception.CupoDiarioExcedidoException;
import pe.edu.upeu.bomerp.exception.ResourceNotFoundException;
import pe.edu.upeu.bomerp.habitos.habito.dto.HabitoResponse;
import pe.edu.upeu.bomerp.habitos.habito.entity.Habito;
import pe.edu.upeu.bomerp.habitos.habito.mapper.HabitoMapper;
import pe.edu.upeu.bomerp.habitos.habito.repository.HabitoRepository;
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
        return habitoMapper.toResponse(buscarOFallar(id));
    }

    @Override
    @Transactional
    public void descontarCupo(Long id, Integer cantidad) {
        Habito habito = buscarOFallar(id);

        if (habito.getActivo() == 0) {
            throw new CupoDiarioExcedidoException(
                    "El habito " + habito.getNombre() + " no esta activo en la campania");
        }
        if (habito.getCupoDisponible() < cantidad) {
            throw new CupoDiarioExcedidoException(
                    "Cupo diario excedido para " + habito.getNombre()
                            + ": disponible " + habito.getCupoDisponible()
                            + ", solicitado " + cantidad);
        }
        habito.setCupoDisponible(habito.getCupoDisponible() - cantidad);
        habitoRepository.save(habito);
    }

    private Habito buscarOFallar(Long id) {
        return habitoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Habito no encontrado: " + id));
    }
}
'@

Escribir "$base\habitos\habito\controller\HabitoController.java" @'
package pe.edu.upeu.bomerp.habitos.habito.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.bomerp.habitos.habito.dto.HabitoResponse;
import pe.edu.upeu.bomerp.habitos.habito.service.HabitoService;
import java.util.List;

@Tag(name = "Habitos")
@RestController
@RequestMapping("/api/v1/habitos")
@RequiredArgsConstructor
public class HabitoController {

    private final HabitoService habitoService;

    @Operation(summary = "Lista los habitos con su cupo diario disponible")
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

Write-Host "== Excepcion propia =="

Escribir "$base\exception\CupoDiarioExcedidoException.java" @'
package pe.edu.upeu.bomerp.exception;

public class CupoDiarioExcedidoException extends RuntimeException {
    public CupoDiarioExcedidoException(String mensaje) {
        super(mensaje);
    }
}
'@

Escribir "$base\exception\GlobalExceptionHandler.java" @'
package pe.edu.upeu.bomerp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Not Found");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", "Error de validacion en los datos enviados");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(StockInsuficienteException.class)
    public ResponseEntity<Map<String, Object>> handleStockInsuficiente(StockInsuficienteException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "Conflict");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(CupoDiarioExcedidoException.class)
    public ResponseEntity<Map<String, Object>> handleCupoDiarioExcedido(CupoDiarioExcedidoException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "Conflict");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }
}
'@

Write-Host "== Modulo participacion =="

Escribir "$base\participacion\registro\entity\EstadoRegistro.java" @'
package pe.edu.upeu.bomerp.participacion.registro.entity;

public enum EstadoRegistro {
    REGISTRADO
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

    @Column(name = "FECHA", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "ID_ESTUDIANTE", nullable = false)
    private Long estudianteId;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false, length = 20)
    private EstadoRegistro estado;

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

    @Column(name = "PUNTAJE_UNITARIO", nullable = false, precision = 10, scale = 2)
    private BigDecimal puntajeUnitario;

    @Column(name = "CANTIDAD", nullable = false)
    private Integer cantidad;

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
    private Integer cantidad;
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
    private BigDecimal puntajeUnitario;
    private Integer cantidad;
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
    private LocalDateTime fecha;
    private Long estudianteId;
    private String estado;
    private BigDecimal puntajeTotal;
    private List<DetalleEvidenciaResponse> detalles;
}
'@

Escribir "$base\participacion\registro\mapper\RegistroMapper.java" @'
package pe.edu.upeu.bomerp.participacion.registro.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pe.edu.upeu.bomerp.habitos.habito.dto.HabitoResponse;
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
    @Mapping(target = "puntajeUnitario", source = "habito.puntajeUnitario")
    @Mapping(target = "subtotal", expression = "java(habito.getPuntajeUnitario().multiply(java.math.BigDecimal.valueOf(request.getCantidad())))")
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
import pe.edu.upeu.bomerp.exception.ResourceNotFoundException;
import pe.edu.upeu.bomerp.habitos.habito.dto.HabitoResponse;
import pe.edu.upeu.bomerp.habitos.habito.service.HabitoService;
import pe.edu.upeu.bomerp.participacion.registro.dto.DetalleEvidenciaRequest;
import pe.edu.upeu.bomerp.participacion.registro.dto.RegistroRequest;
import pe.edu.upeu.bomerp.participacion.registro.dto.RegistroResponse;
import pe.edu.upeu.bomerp.participacion.registro.entity.DetalleEvidencia;
import pe.edu.upeu.bomerp.participacion.registro.entity.EstadoRegistro;
import pe.edu.upeu.bomerp.participacion.registro.entity.RegistroParticipacion;
import pe.edu.upeu.bomerp.participacion.registro.mapper.RegistroMapper;
import pe.edu.upeu.bomerp.participacion.registro.repository.RegistroRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RegistroServiceImpl implements RegistroService {

    private final RegistroRepository registroRepository;
    private final HabitoService habitoService;
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
        RegistroParticipacion registro = new RegistroParticipacion();
        registro.setFecha(LocalDateTime.now());
        registro.setEstudianteId(request.getEstudianteId());
        registro.setEstado(EstadoRegistro.REGISTRADO);

        BigDecimal puntajeTotal = BigDecimal.ZERO;
        for (DetalleEvidenciaRequest detalleRequest : request.getDetalles()) {
            HabitoResponse habito = habitoService.obtener(detalleRequest.getHabitoId());
            habitoService.descontarCupo(detalleRequest.getHabitoId(), detalleRequest.getCantidad());

            DetalleEvidencia detalle = registroMapper.toDetalle(detalleRequest, habito);
            detalle.setRegistro(registro);
            registro.getDetalles().add(detalle);
            puntajeTotal = puntajeTotal.add(detalle.getSubtotal());
        }
        registro.setPuntajeTotal(puntajeTotal);

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

    @Operation(summary = "Registra la participacion del dia descontando el cupo de cada habito")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RegistroResponse crear(@Valid @RequestBody RegistroRequest request) {
        return registroService.crear(request);
    }
}
'@

Write-Host ""
Write-Host "Listo. 21 archivos creados." -ForegroundColor Green
