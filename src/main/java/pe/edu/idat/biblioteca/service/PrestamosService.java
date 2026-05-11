package pe.edu.idat.biblioteca.service;

import pe.edu.idat.biblioteca.dto.prestamo.PrestamoRequest;
import pe.edu.idat.biblioteca.dto.prestamo.PrestamoResponse;

import java.util.List;

public interface PrestamosService {

    PrestamoResponse createPrestamo(PrestamoRequest prestamoRequest);
    PrestamoResponse findById(Long id);
    List<PrestamoResponse>listarPrestamo();
    List<PrestamoResponse>listarPrestamoPorId(Long id);
    void devolverPrestamo(Long id);
}
