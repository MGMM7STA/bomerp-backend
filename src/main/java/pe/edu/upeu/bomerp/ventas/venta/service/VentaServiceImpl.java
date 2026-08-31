package pe.edu.upeu.bomerp.ventas.venta.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.bomerp.catalogo.producto.dto.ProductoResponse;
import pe.edu.upeu.bomerp.catalogo.producto.service.ProductoService;
import pe.edu.upeu.bomerp.exception.ResourceNotFoundException;
import pe.edu.upeu.bomerp.ventas.venta.dto.DetalleVentaRequest;
import pe.edu.upeu.bomerp.ventas.venta.dto.VentaRequest;
import pe.edu.upeu.bomerp.ventas.venta.dto.VentaResponse;
import pe.edu.upeu.bomerp.ventas.venta.entity.DetalleVenta;
import pe.edu.upeu.bomerp.ventas.venta.entity.EstadoVenta;
import pe.edu.upeu.bomerp.ventas.venta.entity.Venta;
import pe.edu.upeu.bomerp.ventas.venta.mapper.VentaMapper;
import pe.edu.upeu.bomerp.ventas.venta.repository.VentaRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VentaServiceImpl implements VentaService {
    private final VentaRepository ventaRepository;
    private final ProductoService productoService;
    private final VentaMapper ventaMapper;

    @Override
    @Transactional(readOnly = true)
    public List<VentaResponse> listar() {
        return ventaRepository.findAll().stream().map(ventaMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VentaResponse obtener(Long id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada: " + id));
        return ventaMapper.toResponse(venta);
    }

    @Override
    @Transactional
    public VentaResponse crear(VentaRequest request) {
        Venta venta = new Venta();
        venta.setFecha(LocalDateTime.now());
        venta.setEstado(EstadoVenta.REGISTRADA);

        BigDecimal total = BigDecimal.ZERO;
        for (DetalleVentaRequest detalleRequest : request.getDetalles()) {
            ProductoResponse producto = productoService.obtener(detalleRequest.getProductoId());
            productoService.descontarStock(detalleRequest.getProductoId(), detalleRequest.getCantidad());

            DetalleVenta detalle = ventaMapper.toDetalle(detalleRequest, producto);
            detalle.setVenta(venta);
            venta.getDetalles().add(detalle);
            total = total.add(detalle.getSubtotal());
        }
        venta.setTotal(total);

        return ventaMapper.toResponse(ventaRepository.save(venta));
    }
}