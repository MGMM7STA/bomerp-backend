package pe.edu.upeu.bomerp.premios.premiacion.service;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.upeu.bomerp.premios.premiacion.dto.ActaPremiacionRequest;
import pe.edu.upeu.bomerp.premios.premiacion.dto.ActaPremiacionResponse;
import pe.edu.upeu.bomerp.premios.premiacion.dto.DetallePremiacionRequest;
//import pe.edu.upeu.bomerp.premios.premiacion.dto.DetallePremiacionResponse;
import pe.edu.upeu.bomerp.premios.premiacion.entity.ActaPremiacion;
import pe.edu.upeu.bomerp.premios.premiacion.entity.DetallePremiacion;
import pe.edu.upeu.bomerp.premios.premiacion.entity.EstadoActaPremiacion;
import pe.edu.upeu.bomerp.premios.premiacion.mapper.ActaPremiacionMapper;
import pe.edu.upeu.bomerp.premios.premiacion.repository.ActaPremiacionRepository;
import pe.edu.upeu.bomerp.premios.premiacion.repository.DetallePremiacionRepository;
import pe.edu.upeu.bomerp.premios.premio.dto.PremioResponse;
import pe.edu.upeu.bomerp.premios.premio.service.PremioService;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ActaPremiacionService {

    private final ActaPremiacionRepository actaRepository;
    private final DetallePremiacionRepository detalleRepository;
    private final ActaPremiacionMapper mapper;

    /*
     * IMPORTANTE:
     * Premiacion NO utiliza PremioRepository directamente.
     * Se comunica con el módulo premios mediante PremioService.
     */
    private final PremioService premioService;


    @Transactional
    public ActaPremiacionResponse emitir(
            ActaPremiacionRequest request) {

        // 1. Validar fechas
        if (request.getFechaFinPeriodo()
                .isBefore(request.getFechaInicioPeriodo())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La fecha fin no puede ser anterior a la fecha inicio"
            );
        }


        // 2. Verificar que no exista otra acta
        boolean existe = actaRepository
                .existsByCampaniaIdAndFechaInicioPeriodoAndFechaFinPeriodo(
                        request.getCampaniaId(),
                        request.getFechaInicioPeriodo(),
                        request.getFechaFinPeriodo()
                );

        if (existe) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un acta para la misma campaña y periodo"
            );
        }


        // 3. Verificar estudiantes repetidos
        Set<Long> estudiantes = new HashSet<>();

        for (DetallePremiacionRequest detalle :
                request.getDetalles()) {

            if (!estudiantes.add(detalle.getEstudianteId())) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Un mismo estudiante no puede aparecer dos veces en la misma acta"
                );
            }
        }


        // 4. Crear cabecera
        ActaPremiacion acta = new ActaPremiacion();

        acta.setCampaniaId(request.getCampaniaId());
        acta.setFechaInicioPeriodo(
                request.getFechaInicioPeriodo()
        );
        acta.setFechaFinPeriodo(
                request.getFechaFinPeriodo()
        );
        acta.setFechaEmision(LocalDateTime.now());
        acta.setEstado(EstadoActaPremiacion.EMITIDA);


        int totalGanadores = request.getDetalles().size();

        int totalPremiosEntregados = request.getDetalles()
                .stream()
                .mapToInt(DetallePremiacionRequest::getCantidad)
                .sum();

        acta.setTotalGanadores(totalGanadores);
        acta.setTotalPremiosEntregados(
                totalPremiosEntregados
        );


        // 5. Crear detalles y verificar stock
        for (DetallePremiacionRequest requestDetalle :
                request.getDetalles()) {

            PremioResponse premio =
                    premioService.obtener(
                            requestDetalle.getPremioId()
                    );


            // Premio inactivo
            if (!Boolean.TRUE.equals(premio.getActivo())) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "El premio no está activo: "
                                + premio.getNombre()
                );
            }


            // Stock insuficiente
            if (premio.getStock()
                    < requestDetalle.getCantidad()) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Stock insuficiente para el premio: "
                                + premio.getNombre()
                );
            }


            // Crear detalle
            DetallePremiacion detalle =
                    new DetallePremiacion();

            detalle.setActa(acta);
            detalle.setEstudianteId(
                    requestDetalle.getEstudianteId()
            );
            detalle.setPosicion(
                    requestDetalle.getPosicion()
            );
            detalle.setPremioId(
                    requestDetalle.getPremioId()
            );
            detalle.setCantidad(
                    requestDetalle.getCantidad()
            );
            detalle.setPuntajeAlcanzado(
                    requestDetalle.getPuntajeAlcanzado()
            );

            acta.getDetalles().add(detalle);
        }


        // 6. Descontar stock
        for (DetallePremiacion detalle :
                acta.getDetalles()) {

            premioService.descontarStock(
                    detalle.getPremioId(),
                    detalle.getCantidad()
            );
        }


        // 7. Guardar cabecera + detalles
        ActaPremiacion guardada =
                actaRepository.save(acta);

        return mapper.toResponse(guardada);
    }


    @Transactional(readOnly = true)
    public List<ActaPremiacionResponse> listar() {

        return actaRepository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }


    @Transactional(readOnly = true)
    public ActaPremiacionResponse obtener(Long id) {

        ActaPremiacion acta =
                actaRepository.findById(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Acta de premiación no encontrada"
                                )
                        );

        return mapper.toResponse(acta);
    }


    @Transactional
    public ActaPremiacionResponse anular(Long id) {

        ActaPremiacion acta =
                actaRepository.findById(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Acta de premiación no encontrada"
                                )
                        );


        if (acta.getEstado()
                != EstadoActaPremiacion.EMITIDA) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Solo se puede anular un acta emitida"
            );
        }


        // Devolver stock
        for (DetallePremiacion detalle :
                acta.getDetalles()) {

            premioService.aumentarStock(
                    detalle.getPremioId(),
                    detalle.getCantidad()
            );
        }


        acta.setEstado(
                EstadoActaPremiacion.ANULADA
        );

        return mapper.toResponse(
                actaRepository.save(acta)
        );
    }
}