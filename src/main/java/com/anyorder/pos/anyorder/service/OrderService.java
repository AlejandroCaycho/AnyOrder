package com.anyorder.pos.anyorder.service;

import com.anyorder.pos.anyorder.model.*;
import com.anyorder.pos.anyorder.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository detailRepository;
    private final PresentationRepository presentationRepository;
    private final PresentationIngredientService presentationIngredientService;
    private final IngredientsRepository ingredientsRepository;
    private final TablesService tablesService;

    @Transactional
    public Order create(Order order) {
        log.info("Creando pedido con validacion completa de stock");

        validateBasicOrderData(order);
        validateServiceHours();

        if (order.getConsumptionType() == Order.ConsumptionType.LOCAL) {
            validateTableAvailability(order.getTable().getIdTable(), order.getNumberOfPeople());
        }

        Map<Integer, BigDecimal> stockRequerido = calcularStockRequerido(order.getDetails());
        validateStockDisponible(stockRequerido);
        validateKitchenCapacity();

        order.setTotal(BigDecimal.ZERO);
        order.setTotalItems(0);
        order.setOrderStatus(Order.OrderStatus.PENDIENTE);
        order.setConfirmed(false);

        Order saved = orderRepository.save(order);
        log.info("Pedido creado con ID: {}", saved.getIdOrder());

        procesarDetalles(saved, order.getDetails());
        reservarStockTemporal(saved.getIdOrder(), stockRequerido);

        log.info("Pedido completado: {} items, Total: {}", saved.getTotalItems(), saved.getTotal());
        return saved;
    }

    @Transactional
    public void confirmOrder(Integer id) {
        log.info("Confirmando pedido {} y descontando stock", id);

        Order order = findById(id);

        if (order.getTotalItems() == null || order.getTotalItems() <= 0) {
            throw new RuntimeException("El pedido debe tener al menos 1 item");
        }

        if (order.getConfirmed()) {
            throw new RuntimeException("El pedido ya esta confirmado");
        }

        descontarStockIngredientes(order);

        order.setConfirmed(true);
        order.setOrderStatus(Order.OrderStatus.EN_PREPARACION);
        orderRepository.save(order);

        log.info("Pedido confirmado y stock descontado");
    }

    @Transactional
    public void cancelOrder(Integer id) {
        log.info("Cancelando pedido {} y liberando stock", id);

        Order order = findById(id);

        if (order.getOrderStatus() == Order.OrderStatus.ENTREGADO ||
                order.getOrderStatus() == Order.OrderStatus.CERRADO) {
            throw new RuntimeException("No se puede cancelar un pedido ya entregado/cerrado");
        }

        if (order.getConfirmed()) {
            devolverStockIngredientes(order);
        }

        order.setOrderStatus(Order.OrderStatus.CANCELADO);
        orderRepository.save(order);

        if (order.getConsumptionType() == Order.ConsumptionType.LOCAL &&
                order.getTable() != null) {
            try {
                tablesService.releaseTable(order.getTable().getIdTable());
            } catch (Exception e) {
                log.warn("No se pudo liberar mesa: {}", e.getMessage());
            }
        }

        log.info("Pedido cancelado y stock liberado");
    }

    private void validateBasicOrderData(Order order) {
        if (order.getUser() == null || order.getUser().getIdUser() == null) {
            throw new RuntimeException("El usuario es obligatorio");
        }

        if (order.getConsumptionType() == null) {
            throw new RuntimeException("El tipo de consumo es obligatorio");
        }

        if (order.getConsumptionType() == Order.ConsumptionType.LOCAL) {
            if (order.getTable() == null || order.getTable().getIdTable() == null) {
                throw new RuntimeException("Para consumo LOCAL, la mesa es obligatoria");
            }
        }

        if (order.getConsumptionType() == Order.ConsumptionType.DELIVERY) {
            if (order.getDeliveryAddress() == null ||
                    order.getDeliveryAddress().trim().length() < 10) {
                throw new RuntimeException("Para DELIVERY, debe proporcionar direccion valida");
            }
        }

        if (order.getNumberOfPeople() == null || order.getNumberOfPeople() <= 0) {
            throw new RuntimeException("El numero de personas debe ser al menos 1");
        }

        if (order.getDetails() == null || order.getDetails().isEmpty()) {
            throw new RuntimeException("El pedido debe tener al menos 1 item");
        }
    }

    private void validateServiceHours() {
        LocalTime now = LocalTime.now();
        LocalTime openTime = LocalTime.of(8, 0);
        LocalTime closeTime = LocalTime.of(23, 0);

        if (now.isBefore(openTime) || now.isAfter(closeTime)) {
            throw new RuntimeException(
                    "Fuera del horario de servicio (8:00 AM - 11:00 PM). Hora actual: " + now);
        }
    }

    private void validateTableAvailability(Integer tableId, Integer numberOfPeople) {
        Tables table = tablesService.findById(tableId);

        if (!table.getState()) {
            throw new RuntimeException("La mesa no esta activa");
        }

        if (table.getIsOccupied()) {
            throw new RuntimeException(
                    "La mesa ya esta ocupada desde: " + table.getOccupiedSince());
        }

        if (numberOfPeople > table.getCapacity()) {
            throw new RuntimeException(
                    "Numero de personas (" + numberOfPeople +
                            ") excede capacidad de mesa (" + table.getCapacity() + ")");
        }
    }

    private void validateKitchenCapacity() {
        long pendingOrders = orderRepository.countByOrderStatus(Order.OrderStatus.PENDIENTE);
        long preparingOrders = orderRepository.countByOrderStatus(Order.OrderStatus.EN_PREPARACION);

        long totalActive = pendingOrders + preparingOrders;

        if (totalActive >= 50) {
            throw new RuntimeException(
                    "Cocina saturada. Pedidos activos: " + totalActive + ". Intente en unos minutos.");
        }
    }

    private Map<Integer, BigDecimal> calcularStockRequerido(List<OrderDetail> details) {
        Map<Integer, BigDecimal> stockRequerido = new HashMap<>();

        for (OrderDetail detail : details) {
            Integer presentationId = detail.getPresentation().getIdPresentation();
            Integer cantidad = detail.getAmount();

            List<PresentationIngredient> ingredients = presentationIngredientService
                    .findByPresentationId(presentationId);

            for (PresentationIngredient pi : ingredients) {
                Integer ingredientId = pi.getIngredient().getIdIngredient();
                BigDecimal cantidadRequerida = pi.getQuantity().multiply(new BigDecimal(cantidad));

                stockRequerido.merge(ingredientId, cantidadRequerida, BigDecimal::add);
            }
        }

        return stockRequerido;
    }

    private void validateStockDisponible(Map<Integer, BigDecimal> stockRequerido) {
        StringBuilder errores = new StringBuilder();

        for (Map.Entry<Integer, BigDecimal> entry : stockRequerido.entrySet()) {
            Integer ingredientId = entry.getKey();
            BigDecimal cantidadRequerida = entry.getValue();

            Ingredients ingredient = ingredientsRepository.findById(ingredientId)
                    .orElseThrow(() -> new RuntimeException("Ingrediente no encontrado: " + ingredientId));

            if (ingredient.getQuantity().compareTo(cantidadRequerida) < 0) {
                errores.append(String.format(
                        "%s: Stock insuficiente (Requerido: %.2f %s, Disponible: %.2f %s)\n",
                        ingredient.getName(),
                        cantidadRequerida,
                        ingredient.getUnit(),
                        ingredient.getQuantity(),
                        ingredient.getUnit()));
            }
        }

        if (errores.length() > 0) {
            throw new RuntimeException("STOCK INSUFICIENTE:\n" + errores.toString());
        }
    }

    private void reservarStockTemporal(Integer orderId, Map<Integer, BigDecimal> stockRequerido) {
        log.info("Reservando stock temporal para pedido {}", orderId);
    }

    private void descontarStockIngredientes(Order order) {
        log.info("Descontando stock para pedido {}", order.getIdOrder());

        Map<Integer, BigDecimal> stockRequerido = new HashMap<>();

        List<OrderDetail> details = detailRepository.findByOrder_IdOrder(order.getIdOrder());

        for (OrderDetail detail : details) {
            List<PresentationIngredient> ingredients = presentationIngredientService.findByPresentationId(
                    detail.getPresentation().getIdPresentation());

            for (PresentationIngredient pi : ingredients) {
                Integer ingredientId = pi.getIngredient().getIdIngredient();
                BigDecimal cantidadRequerida = pi.getQuantity()
                        .multiply(new BigDecimal(detail.getAmount()));

                stockRequerido.merge(ingredientId, cantidadRequerida, BigDecimal::add);
            }
        }

        for (Map.Entry<Integer, BigDecimal> entry : stockRequerido.entrySet()) {
            Ingredients ingredient = ingredientsRepository.findById(entry.getKey())
                    .orElseThrow(() -> new RuntimeException("Ingrediente no encontrado"));

            BigDecimal nuevoStock = ingredient.getQuantity().subtract(entry.getValue());

            if (nuevoStock.compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException(
                        "Error critico: Stock negativo para " + ingredient.getName());
            }

            ingredient.setQuantity(nuevoStock);
            ingredientsRepository.save(ingredient);

            log.info("{} descontado: {} {}",
                    ingredient.getName(), entry.getValue(), ingredient.getUnit());
        }
    }

    private void devolverStockIngredientes(Order order) {
        log.info("Devolviendo stock para pedido cancelado {}", order.getIdOrder());

        List<OrderDetail> details = detailRepository.findByOrder_IdOrder(order.getIdOrder());

        for (OrderDetail detail : details) {
            List<PresentationIngredient> ingredients = presentationIngredientService.findByPresentationId(
                    detail.getPresentation().getIdPresentation());

            for (PresentationIngredient pi : ingredients) {
                Ingredients ingredient = pi.getIngredient();
                BigDecimal cantidadDevolver = pi.getQuantity()
                        .multiply(new BigDecimal(detail.getAmount()));

                ingredient.setQuantity(ingredient.getQuantity().add(cantidadDevolver));
                ingredientsRepository.save(ingredient);

                log.info("{} devuelto: {} {}",
                        ingredient.getName(), cantidadDevolver, ingredient.getUnit());
            }
        }
    }

    private void procesarDetalles(Order saved, List<OrderDetail> details) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        Integer totalItems = 0;

        for (OrderDetail detail : details) {
            Presentation presentation = presentationRepository.findById(
                    detail.getPresentation().getIdPresentation())
                    .orElseThrow(() -> new RuntimeException("Presentacion no encontrada"));

            if (detail.getAmount() == null || detail.getAmount() <= 0) {
                throw new RuntimeException("La cantidad debe ser mayor a 0");
            }

            BigDecimal price = getPriceByConsumptionType(presentation, saved.getConsumptionType());

            if (price == null || price.compareTo(BigDecimal.ZERO) == 0) {
                throw new RuntimeException("Sin precio para este tipo de consumo");
            }

            detail.setOrder(saved);
            detail.setPresentation(presentation);
            detail.setUnitPrice(price);
            detail.calculateSubtotal();

            detailRepository.save(detail);

            totalAmount = totalAmount.add(detail.getSubtotal());
            totalItems += detail.getAmount();
        }

        saved.setTotal(totalAmount);
        saved.setTotalItems(totalItems);
        orderRepository.save(saved);
    }

    private BigDecimal getPriceByConsumptionType(Presentation presentation,
            Order.ConsumptionType type) {
        return switch (type) {
            case DELIVERY ->
                presentation.getDeliveryPrice() != null ? presentation.getDeliveryPrice() : presentation.getPrice();
            case TAKEOUT ->
                presentation.getTakeoutPrice() != null ? presentation.getTakeoutPrice() : presentation.getPrice();
            case PROMO -> presentation.getPromoPrice() != null ? presentation.getPromoPrice() : presentation.getPrice();
            case LOCAL -> presentation.getPrice();
        };
    }

    @Transactional(readOnly = true)
    public Order findById(Integer id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Order> findByOrderStatus(Order.OrderStatus status) {
        return orderRepository.findByOrderStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Order> findByConsumptionType(Order.ConsumptionType type) {
        return orderRepository.findByConsumptionType(type);
    }

    @Transactional(readOnly = true)
    public List<Order> findByUserId(Integer userId) {
        return orderRepository.findByUser_IdUser(userId);
    }

    @Transactional(readOnly = true)
    public List<Order> findByCustomerId(Integer customerId) {
        return orderRepository.findByCustomer_IdCustomer(customerId);
    }

    @Transactional(readOnly = true)
    public List<Order> findByTableId(Integer tableId) {
        return orderRepository.findByTable_IdTable(tableId);
    }

    @Transactional(readOnly = true)
    public List<Order> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findByDateRange(startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<Order> findByStatusAndDateRange(Order.OrderStatus status, LocalDateTime startDate,
            LocalDateTime endDate) {
        return orderRepository.findByStatusAndDateRange(status, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<Order> findByConsumptionTypeAndDateRange(Order.ConsumptionType type, LocalDateTime startDate,
            LocalDateTime endDate) {
        return orderRepository.findByConsumptionTypeAndDateRange(type, startDate, endDate);
    }

    @Transactional
    public Order update(Integer id, Order orderData) {
        log.info("Actualizando pedido con ID: {}", id);

        Order order = findById(id);

        if (order.getOrderStatus() == Order.OrderStatus.CERRADO ||
                order.getOrderStatus() == Order.OrderStatus.CANCELADO) {
            throw new RuntimeException("No se puede modificar un pedido cerrado o cancelado");
        }

        if (orderData.getOrderStatus() != null) {
            order.setOrderStatus(orderData.getOrderStatus());
        }

        if (orderData.getNumberOfPeople() != null) {
            order.setNumberOfPeople(orderData.getNumberOfPeople());
        }

        if (orderData.getCustomerNotes() != null) {
            order.setCustomerNotes(orderData.getCustomerNotes());
        }

        if (orderData.getConfirmed() != null) {
            order.setConfirmed(orderData.getConfirmed());
        }

        Order updated = orderRepository.save(order);
        log.info("Pedido actualizado con ID: {}", id);

        return updated;
    }

    @Transactional
    public void updateOrderStatus(Integer id, Order.OrderStatus newStatus) {
        log.info("Actualizando estado del pedido {} a {}", id, newStatus);

        Order order = findById(id);

        if (order.getOrderStatus() == Order.OrderStatus.CERRADO ||
                order.getOrderStatus() == Order.OrderStatus.CANCELADO) {
            throw new RuntimeException("No se puede cambiar el estado de un pedido cerrado o cancelado");
        }

        order.setOrderStatus(newStatus);
        orderRepository.save(order);

        log.info("Estado actualizado correctamente");
    }

    @Transactional(readOnly = true)
    public List<Order> findPendingOrders() {
        return orderRepository.findByOrderStatus(Order.OrderStatus.PENDIENTE);
    }

    @Transactional(readOnly = true)
    public List<Order> findReadyOrders() {
        return orderRepository.findByOrderStatus(Order.OrderStatus.LISTO);
    }

    @Transactional(readOnly = true)
    public List<Order> findActiveOrdersByTable(Integer tableId) {
        return orderRepository.findActiveOrdersByTable(tableId);
    }

    @Transactional(readOnly = true)
    public long countByOrderStatus(Order.OrderStatus status) {
        return orderRepository.countByOrderStatus(status);
    }

    @Transactional(readOnly = true)
    public long countByConsumptionType(Order.ConsumptionType type) {
        return orderRepository.countByConsumptionType(type);
    }

    @Transactional(readOnly = true)
    public BigDecimal sumTotalByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.sumTotalByDateRange(startDate, endDate);
    }
}