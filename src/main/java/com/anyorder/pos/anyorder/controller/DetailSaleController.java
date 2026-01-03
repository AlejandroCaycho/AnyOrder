package com.anyorder.pos.anyorder.controller;

import com.anyorder.pos.anyorder.model.DetailSale;
import com.anyorder.pos.anyorder.service.DetailSaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sale-details")
@RequiredArgsConstructor
@Tag(name = "Detalles de Ventas", description = "Gestión de líneas/items de ventas")
public class DetailSaleController {

    private final DetailSaleService detailSaleService;

    @Operation(summary = "Listar todos los detalles de venta")
    @GetMapping
    public ResponseEntity<List<DetailSale>> getAll() {
        return ResponseEntity.ok(detailSaleService.findAll());
    }

    @Operation(summary = "Obtener detalle por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Detalle encontrado"),
            @ApiResponse(responseCode = "404", description = "Detalle no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<DetailSale> getById(
            @Parameter(description = "ID del detalle") @PathVariable Integer id) {
        return ResponseEntity.ok(detailSaleService.findById(id));
    }

    @Operation(summary = "Obtener detalles de una venta", description = "Lista todos los items/productos vendidos en una venta específica")
    @GetMapping("/sale/{saleId}")
    public ResponseEntity<List<DetailSale>> getBySaleId(
            @Parameter(description = "ID de la venta") @PathVariable Integer saleId) {
        return ResponseEntity.ok(detailSaleService.findBySaleId(saleId));
    }

    @Operation(summary = "Detalles por presentación", description = "Historial de ventas de un producto específico")
    @GetMapping("/presentation/{presentationId}")
    public ResponseEntity<List<DetailSale>> getByPresentationId(@PathVariable Integer presentationId) {
        return ResponseEntity.ok(detailSaleService.findByPresentationId(presentationId));
    }

    @Operation(summary = "Total de items vendidos en una venta")
    @GetMapping("/sale/{saleId}/total-items")
    public ResponseEntity<Map<String, Integer>> getTotalItems(@PathVariable Integer saleId) {
        Map<String, Integer> response = new HashMap<>();
        response.put("totalItems", detailSaleService.getTotalItems(saleId));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Total en dinero de una venta")
    @GetMapping("/sale/{saleId}/total-amount")
    public ResponseEntity<Map<String, BigDecimal>> getTotalAmount(@PathVariable Integer saleId) {
        Map<String, BigDecimal> response = new HashMap<>();
        response.put("totalAmount", detailSaleService.getTotalAmount(saleId));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Contar líneas de detalle de una venta")
    @GetMapping("/sale/{saleId}/count")
    public ResponseEntity<Map<String, Long>> countDetails(@PathVariable Integer saleId) {
        Map<String, Long> response = new HashMap<>();
        response.put("total", detailSaleService.countDetailsBySale(saleId));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Productos más vendidos", description = "Top de productos ordenados por cantidad vendida (para reportes)")
    @GetMapping("/top-selling")
    public ResponseEntity<List<Object[]>> getTopSellingProducts() {
        return ResponseEntity.ok(detailSaleService.getTopSellingProducts());
    }
}