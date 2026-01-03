package com.anyorder.pos.anyorder.service;

import com.anyorder.pos.anyorder.model.DetailSale;
import com.anyorder.pos.anyorder.repository.DetailSaleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DetailSaleService {

    private final DetailSaleRepository detailSaleRepository;

    @Transactional(readOnly = true)
    public List<DetailSale> findAll() {
        log.debug("Obteniendo todos los detalles de venta");
        return detailSaleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public DetailSale findById(Integer id) {
        log.debug("Buscando detalle de venta con ID: {}", id);
        return detailSaleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Detalle de venta no encontrado con ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<DetailSale> findBySaleId(Integer saleId) {
        log.debug("Obteniendo detalles de la venta: {}", saleId);
        return detailSaleRepository.findDetailsBySaleId(saleId);
    }

    @Transactional(readOnly = true)
    public List<DetailSale> findByPresentationId(Integer presentationId) {
        log.debug("Obteniendo detalles de la presentación: {}", presentationId);
        return detailSaleRepository.findByPresentation_IdPresentation(presentationId);
    }

    @Transactional(readOnly = true)
    public Integer getTotalItems(Integer saleId) {
        log.debug("Obteniendo total de items de la venta: {}", saleId);
        Integer total = detailSaleRepository.sumAmountBySaleId(saleId);
        return total != null ? total : 0;
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalAmount(Integer saleId) {
        log.debug("Obteniendo total de la venta: {}", saleId);
        BigDecimal total = detailSaleRepository.sumSubtotalBySaleId(saleId);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public long countDetailsBySale(Integer saleId) {
        log.debug("Contando detalles de la venta: {}", saleId);
        return detailSaleRepository.countBySaleId(saleId);
    }

    @Transactional(readOnly = true)
    public List<Object[]> getTopSellingProducts() {
        log.debug("Obteniendo productos más vendidos");
        return detailSaleRepository.findTopSellingProducts();
    }
}