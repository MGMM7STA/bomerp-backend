package pe.edu.upeu.bomerp.cuentas.service;

import pe.edu.upeu.bomerp.sedes.Sede;
import pe.edu.upeu.bomerp.cuentas.dto.CuentaRequest;
import pe.edu.upeu.bomerp.cuentas.dto.CuentaResponse;
import java.util.List;

public interface CuentaService {

    List<CuentaResponse> listar();

    CuentaResponse crear(CuentaRequest request);

    CuentaResponse cambiarRol(String id, String rol, Sede sede, String solicitante);

    CuentaResponse cambiarEstado(String id, boolean activa, String solicitante);

    void restablecerContrasenia(String id, String contrasenia);

    void eliminar(String id);
}
