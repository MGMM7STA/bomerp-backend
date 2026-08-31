package pe.edu.upeu.bomerp.ventas.venta.service;

import pe.edu.upeu.bomerp.ventas.venta.dto.VentaRequest;
import pe.edu.upeu.bomerp.ventas.venta.dto.VentaResponse;
import java.util.List;

public interface VentaService {
    List<VentaResponse> listar();
    VentaResponse obtener(Long id);
    VentaResponse crear(VentaRequest request);
}