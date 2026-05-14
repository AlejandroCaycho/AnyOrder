package com.anyorder.pos.anyorder.modules.orders.service;

import com.anyorder.pos.anyorder.modules.orders.model.Order;
import com.anyorder.pos.anyorder.modules.orders.model.OrderDetail;
import com.anyorder.pos.anyorder.modules.orders.repository.OrderRepository;
import com.anyorder.pos.anyorder.modules.orders.repository.OrderDetailRepository;
import com.anyorder.pos.anyorder.modules.presentations.model.Presentation;
import com.anyorder.pos.anyorder.modules.presentations.repository.PresentationRepository;
import com.anyorder.pos.anyorder.modules.ingredients.model.Ingredient;
import com.anyorder.pos.anyorder.modules.ingredients.repository.IngredientRepository;
import com.anyorder.pos.anyorder.modules.presentations_ingredients.model.PresentationIngredient;
import com.anyorder.pos.anyorder.modules.presentations_ingredients.service.PresentationIngredientService;
import com.anyorder.pos.anyorder.modules.tables.service.TablesService;
import com.anyorder.pos.anyorder.modules.tables.model.Tables;
import com.anyorder.pos.anyorder.modules.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final IngredientRepository ingredientRepository;
    private final TablesService tablesService;
    private final InventoryService inventoryService;


    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Order findById(Integer id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<Order> findByStatus(Order.OrderStatus status) {
        return orderRepository.findByOrderStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Order> findByUser(Integer userId) {
        return orderRepository.findByUser_IdUser(userId);
    }

    @Transactional(readOnly = true)
    public List<Order> findByCustomer(Integer customerId) {
        return orderRepository.findByCustomer_IdCustomer(customerId);
    }


    /**
     * Crea un pedido nuevo (PENDIENTE).
     * Valida datos básicos y verifica que haya stock suficiente
     * para los ingredientes antes de persistir.
     */
    @Transactional
    public Order create(Order order) {
        log.info("Creando pedido para usuario ID: {}", order.getUser().getIdUser());

        validateBasicData(order);

        if (order.getTypeOfConsumption() == Order.ConsumptionType.LOCAL) {
            validateTable(order.getTable().getIdTable(), order.getNumberOfPeople());
        }

        // Verificar stock antes de crear (sin descontar aún)
        Map<Integer, BigDecimal> stockRequerido = calcularStockRequerido(order.getDetails());
        verificarStockDisponible(stockRequerido);

        order.setTotal(BigDecimal.ZERO);
        order.setTotalItems(0);
        order.setOrderStatus(Order.OrderStatus.PENDIENTE);
        order.setConfirmed(false);

        Order saved = orderRepository.save(order);
        procesarDetalles(saved, order.getDetails());

        log.info("Pedido #{} creado con total {}", saved.getIdOrder(), saved.getTotal());
        return saved;
    }

    /**
     * Agrega detalles a un pedido existente.
     * Útil para pedidos adicionales en la misma mesa (incluyendo QR).
     */
    @Transactional
    public Order addDetails(Integer orderId, List<OrderDetail> newDetails) {
        Order order = findById(orderId);

        if (order.getOrderStatus() == Order.OrderStatus.CERRADO || 
            order.getOrderStatus() == Order.OrderStatus.CANCELADO) {
            throw new RuntimeException("No se pueden agregar ítems a un pedido cerrado o cancelado");
        }

        // Si el pedido ya estaba confirmado, debemos verificar stock y descontarlo inmediatamente para estos nuevos ítems
        if (order.getConfirmed()) {
            Map<Integer, BigDecimal> stockRequerido = calcularStockRequerido(newDetails);
            verificarStockDisponible(stockRequerido);
            
            // Procesar y guardar detalles
            procesarDetalles(order, newDetails);
            
            // Descontar stock solo para los nuevos detalles (un poco complejo sin tracking de qué se descontó)
            // Para simplificar, descontamos stock de los nuevos ítems manualmente aquí
            for (OrderDetail detail : newDetails) {
                List<PresentationIngredient> pis = presentationIngredientService
                        .findByPresentationId(detail.getPresentation().getIdPresentation());

                for (PresentationIngredient pi : pis) {
                    Ingredient ingredient = ingredientRepository.findById(pi.getIngredient().getIdIngredient())
                            .orElseThrow(() -> new RuntimeException("Ingrediente no encontrado"));

                    BigDecimal cantidad = pi.getQuantity().multiply(new BigDecimal(detail.getAmount()));
                    String reason = "Adición Pedido #" + order.getIdOrder() + " - " + detail.getPresentation().getName();

                    inventoryService.registerSalida(ingredient, cantidad, order.getUser(), reason);
                }
            }
        } else {
            // Si no estaba confirmado, solo agregamos los detalles (se descontarán al confirmar el pedido completo)
            procesarDetalles(order, newDetails);
        }

        return order;
    }

    /**
     * Confirma el pedido: descuenta stock de ingredientes y pasa a EN_PREPARACION.
     * Registra movimientos de inventario (SALIDA) por cada ingrediente consumido.
     */
    @Transactional
    public Order confirm(Integer id) {
        log.info("Confirmando pedido #{}", id);

        Order order = findById(id);

        if (order.getConfirmed()) {
            throw new RuntimeException("El pedido ya está confirmado");
        }
        if (order.getOrderStatus() == Order.OrderStatus.CANCELADO) {
            throw new RuntimeException("No se puede confirmar un pedido cancelado");
        }
        if (order.getTotalItems() == null || order.getTotalItems() <= 0) {
            throw new RuntimeException("El pedido no tiene ítems");
        }

        // Descontar stock y registrar movimientos
        descontarStockConMovimiento(order);

        order.setConfirmed(true);
        order.setOrderStatus(Order.OrderStatus.EN_PREPARACION);
        orderRepository.save(order);

        log.info("Pedido #{} confirmado, stock descontado", id);
        return order;
    }

    /**
     * Marca el pedido como LISTO (preparación terminada).
     */
    @Transactional
    public Order markAsReady(Integer id) {
        Order order = findById(id);

        if (order.getOrderStatus() != Order.OrderStatus.EN_PREPARACION) {
            throw new RuntimeException("El pedido debe estar EN_PREPARACION. Estado actual: " + order.getOrderStatus());
        }

        order.setOrderStatus(Order.OrderStatus.LISTO);
        return orderRepository.save(order);
    }

    /**
     * Marca el pedido como ENTREGADO.
     * Solo desde este estado puede crearse una venta.
     */
    @Transactional
    public Order deliver(Integer id) {
        Order order = findById(id);

        if (order.getOrderStatus() != Order.OrderStatus.LISTO) {
            throw new RuntimeException("El pedido debe estar LISTO para poder entregarse. Estado actual: " + order.getOrderStatus());
        }

        order.setOrderStatus(Order.OrderStatus.ENTREGADO);
        return orderRepository.save(order);
    }

    /**
     * Cancela el pedido. Si ya estaba confirmado, devuelve el stock.
     */
    @Transactional
    public void cancel(Integer id) {
        log.info("Cancelando pedido #{}", id);

        Order order = findById(id);

        if (order.getOrderStatus() == Order.OrderStatus.ENTREGADO ||
                order.getOrderStatus() == Order.OrderStatus.CERRADO) {
            throw new RuntimeException("No se puede cancelar un pedido ya entregado o cerrado");
        }

        if (order.getConfirmed()) {
            devolverStockConMovimiento(order);
        }

        order.setOrderStatus(Order.OrderStatus.CANCELADO);
        orderRepository.save(order);

        log.info("Pedido #{} cancelado", id);
    }


    private void validateBasicData(Order order) {
        if (order.getUser() == null || order.getUser().getIdUser() == null) {
            throw new RuntimeException("El usuario es obligatorio");
        }
        if (order.getTypeOfConsumption() == null) {
            throw new RuntimeException("El tipo de consumo es obligatorio");
        }
        if (order.getTypeOfConsumption() == Order.ConsumptionType.LOCAL) {
            if (order.getTable() == null || order.getTable().getIdTable() == null) {
                throw new RuntimeException("Para consumo LOCAL la mesa es obligatoria");
            }
        }
        if (order.getTypeOfConsumption() == Order.ConsumptionType.DELIVERY) {
            if (order.getDeliveryAddress() == null || order.getDeliveryAddress().trim().length() < 10) {
                throw new RuntimeException("Para DELIVERY se requiere una dirección válida (mínimo 10 caracteres)");
            }
        }
        if (order.getDetails() == null || order.getDetails().isEmpty()) {
            throw new RuntimeException("El pedido debe tener al menos 1 ítem");
        }
    }

    private void validateTable(Integer tableId, Integer numberOfPeople) {
        Tables table = tablesService.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada con ID: " + tableId));

        if (!table.getState()) {
            throw new RuntimeException("La mesa no está activa");
        }
        if (table.getIsOccupied()) {
            throw new RuntimeException("La mesa ya está ocupada");
        }
        if (numberOfPeople != null && numberOfPeople > table.getCapacity()) {
            throw new RuntimeException("El número de personas excede la capacidad de la mesa (" + table.getCapacity() + ")");
        }
    }

    private Map<Integer, BigDecimal> calcularStockRequerido(List<OrderDetail> details) {
        Map<Integer, BigDecimal> required = new HashMap<>();

        for (OrderDetail detail : details) {
            Integer presentationId = detail.getPresentation().getIdPresentation();
            List<PresentationIngredient> pis = presentationIngredientService.findByPresentationId(presentationId);

            for (PresentationIngredient pi : pis) {
                Integer ingredientId = pi.getIngredient().getIdIngredient();
                BigDecimal cantidad = pi.getQuantity().multiply(new BigDecimal(detail.getAmount()));
                required.merge(ingredientId, cantidad, BigDecimal::add);
            }
        }

        return required;
    }

    private void verificarStockDisponible(Map<Integer, BigDecimal> required) {
        for (Map.Entry<Integer, BigDecimal> entry : required.entrySet()) {
            Ingredient ingredient = ingredientRepository.findById(entry.getKey())
                    .orElseThrow(() -> new RuntimeException("Ingrediente no encontrado"));

            if (ingredient.getQuantity().compareTo(entry.getValue()) < 0) {
                throw new RuntimeException(
                        "Stock insuficiente para: " + ingredient.getName() +
                        ". Disponible: " + ingredient.getQuantity() +
                        ", Requerido: " + entry.getValue());
            }
        }
    }

    private void descontarStockConMovimiento(Order order) {
        List<OrderDetail> details = detailRepository.findByOrder_IdOrder(order.getIdOrder());

        for (OrderDetail detail : details) {
            List<PresentationIngredient> pis = presentationIngredientService
                    .findByPresentationId(detail.getPresentation().getIdPresentation());

            for (PresentationIngredient pi : pis) {
                Ingredient ingredient = ingredientRepository.findById(pi.getIngredient().getIdIngredient())
                        .orElseThrow(() -> new RuntimeException("Ingrediente no encontrado"));

                BigDecimal cantidad = pi.getQuantity().multiply(new BigDecimal(detail.getAmount()));
                String reason = "Pedido #" + order.getIdOrder() + " - " + detail.getPresentation().getName();

                inventoryService.registerSalida(ingredient, cantidad, order.getUser(), reason);
            }
        }
    }

    private void devolverStockConMovimiento(Order order) {
        List<OrderDetail> details = detailRepository.findByOrder_IdOrder(order.getIdOrder());

        for (OrderDetail detail : details) {
            List<PresentationIngredient> pis = presentationIngredientService
                    .findByPresentationId(detail.getPresentation().getIdPresentation());

            for (PresentationIngredient pi : pis) {
                Ingredient ingredient = ingredientRepository.findById(pi.getIngredient().getIdIngredient())
                        .orElseThrow(() -> new RuntimeException("Ingrediente no encontrado"));

                BigDecimal cantidad = pi.getQuantity().multiply(new BigDecimal(detail.getAmount()));
                String reason = "Cancelación pedido #" + order.getIdOrder() + " - " + detail.getPresentation().getName();

                inventoryService.registerDevolucion(ingredient, cantidad, order.getUser(), reason);
            }
        }
    }

    private void procesarDetalles(Order saved, List<OrderDetail> details) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalItems = 0;

        for (OrderDetail detail : details) {
            Presentation presentation = presentationRepository
                    .findById(detail.getPresentation().getIdPresentation())
                    .orElseThrow(() -> new RuntimeException("Presentación no encontrada"));

            BigDecimal price = getPriceForType(presentation, saved.getTypeOfConsumption());

            detail.setOrder(saved);
            detail.setPresentation(presentation);
            detail.setUnitPrice(price);
            detail.setSubtotal(price.multiply(new BigDecimal(detail.getAmount())));

            detailRepository.save(detail);

            totalAmount = totalAmount.add(detail.getSubtotal());
            totalItems += detail.getAmount();
        }

        saved.setTotal(totalAmount);
        saved.setTotalItems(totalItems);
        orderRepository.save(saved);
    }

    private BigDecimal getPriceForType(Presentation presentation, Order.ConsumptionType type) {
        return switch (type) {
            case DELIVERY -> presentation.getDeliveryPrice() != null ? presentation.getDeliveryPrice() : presentation.getPrice();
            case TAKEOUT  -> presentation.getTakeoutPrice() != null  ? presentation.getTakeoutPrice()  : presentation.getPrice();
            case PROMO    -> presentation.getPromoPrice()   != null  ? presentation.getPromoPrice()    : presentation.getPrice();
            case LOCAL    -> presentation.getPrice();
        };
    }
}
