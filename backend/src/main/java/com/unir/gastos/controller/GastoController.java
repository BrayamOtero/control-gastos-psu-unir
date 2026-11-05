package com.unir.gastos.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.unir.gastos.dto.GastoFiltroDto;
import com.unir.gastos.dto.GastoRequestDto;
import com.unir.gastos.dto.GastoResponseDto;
import com.unir.gastos.dto.ResumenMensualDto;
import com.unir.gastos.service.ConsultaGastoService;
import com.unir.gastos.service.CreacionGastoService;
import com.unir.gastos.service.EliminacionGastoService;
import com.unir.gastos.service.ModificacionGastoService;
import com.unir.gastos.service.ResumenMensualService;

@RestController
@RequestMapping("/api/gastos")
public class GastoController {

    private final ConsultaGastoService consultaGastoService;
    private final CreacionGastoService creacionGastoService;
    private final ModificacionGastoService modificacionGastoService;
    private final EliminacionGastoService eliminacionGastoService;
    private final ResumenMensualService resumenMensualService;

    public GastoController(
            ConsultaGastoService consultaGastoService,
            CreacionGastoService creacionGastoService,
            ModificacionGastoService modificacionGastoService,
            EliminacionGastoService eliminacionGastoService,
            ResumenMensualService resumenMensualService) {
        this.consultaGastoService = consultaGastoService;
        this.creacionGastoService = creacionGastoService;
        this.modificacionGastoService = modificacionGastoService;
        this.eliminacionGastoService = eliminacionGastoService;
        this.resumenMensualService = resumenMensualService;
    }

    @GetMapping("/resumen")
    public ResumenMensualDto resumen(@RequestParam int mes, @RequestParam int anio) {
        return resumenMensualService.obtener(anio, mes);
    }

    @GetMapping("/{id}")
    public GastoResponseDto obtenerPorId(@PathVariable Long id) {
        return consultaGastoService.obtenerPorId(id);
    }

    @GetMapping
    public PagedModel<GastoResponseDto> buscar(
            @ModelAttribute GastoFiltroDto filtro,
            @PageableDefault(sort = "id") Pageable pageable) {
        return new PagedModel<>(consultaGastoService.buscar(filtro, pageable));
    }

    @PostMapping
    public ResponseEntity<GastoResponseDto> crear(@RequestBody GastoRequestDto request) {
        var creado = creacionGastoService.crear(request);
        var ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creado.id())
                .toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @PutMapping("/{id}")
    public GastoResponseDto modificar(@PathVariable Long id, @RequestBody GastoRequestDto request) {
        return modificacionGastoService.modificar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        eliminacionGastoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
