package com.mns;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

import java.util.List;
import java.util.Optional;

@Controller("/orders")
class OrderController {

    private final OrderService orderService;

    OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Get
    List<Order> listAll() {
        return orderService.listAll();
    }

    @Get("/{id}")
    Optional<Order> findOrder(String id) {
        return orderService.findById(id);
    }
}