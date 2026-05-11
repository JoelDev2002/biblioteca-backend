package pe.edu.idat.biblioteca.service.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.idat.biblioteca.dto.prestamo.PrestamoRequest;
import pe.edu.idat.biblioteca.dto.prestamo.PrestamoResponse;
import pe.edu.idat.biblioteca.entity.Libro;
import pe.edu.idat.biblioteca.entity.Prestamo;
import pe.edu.idat.biblioteca.entity.Usuario;
import pe.edu.idat.biblioteca.exception.LibroNotFoundException;
import pe.edu.idat.biblioteca.exception.PrestamoNotFoundException;
import pe.edu.idat.biblioteca.exception.UsuarioNotFoundException;
import pe.edu.idat.biblioteca.mapper.PrestamoMapper;
import pe.edu.idat.biblioteca.repository.LibroRepository;
import pe.edu.idat.biblioteca.repository.PrestamoRepository;
import pe.edu.idat.biblioteca.repository.UsuarioRepository;
import pe.edu.idat.biblioteca.service.LibroService;
import pe.edu.idat.biblioteca.service.PrestamosService;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrestamosServiceImpl implements PrestamosService {

    private final PrestamoRepository prestamoRepository;
    private final LibroRepository libroRepository;
    private final UsuarioRepository usuarioRepository;
    private final PrestamoMapper prestamoMapper;
    private final LibroService libroService;


    @Transactional
    @Override
    public PrestamoResponse createPrestamo(PrestamoRequest prestamoRequest) {
        Libro libro =libroRepository.findById(prestamoRequest.idLibro())
                        .orElseThrow(()->new LibroNotFoundException("libro no encontrado: "+ prestamoRequest.idLibro()));

        Usuario usuario=usuarioRepository.findById(prestamoRequest.idUsuario())
                        .orElseThrow(()->new UsuarioNotFoundException("usuario no encontrado: "+prestamoRequest.idUsuario()));

        libroService.reducirStock(libro);
        Prestamo prestamo = prestamoMapper.toEntity(prestamoRequest);
        prestamo.setFechaPrestamo(LocalDate.now());
        prestamo.setLibro(libro);
        prestamo.setUsuario(usuario);
        prestamo.setEstado("PRESTADO");

        return prestamoMapper.toResponse(prestamoRepository.save(prestamo));
    }

    @Transactional(readOnly = true)
    @Override
    public PrestamoResponse findById(Long id) {
        Prestamo prestamo = prestamoRepository.findById(id)
                .orElseThrow(() -> new PrestamoNotFoundException("Préstamo no encontrado: "+ id));
        return prestamoMapper.toResponse(prestamo);
    }

    @Transactional(readOnly = true)
    @Override
    public List<PrestamoResponse> listarPrestamo() {
        return prestamoRepository.findAll()
                .stream()
                .map(prestamoMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<PrestamoResponse> listarPrestamoPorId(Long idUsuario) {
        return prestamoRepository.findByUsuario_IdUsuario(idUsuario)
                .stream()
                .map(prestamoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void devolverPrestamo(Long id) {
        Prestamo prestamo = prestamoRepository.findById(id)
                .orElseThrow(() -> new PrestamoNotFoundException("Préstamo no encontrado"));

        Libro libro =prestamo.getLibro();

        libroService.aumentarStock(libro);

        prestamo.setFechaEntregada(LocalDate.now());
        prestamo.setEstado("DEVUELTO");
        prestamoRepository.save(prestamo);
    }
}