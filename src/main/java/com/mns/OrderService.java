package com.mns;

import jakarta.inject.Singleton;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Singleton
public class OrderService {

    private final List<Order> order = new ArrayList<>();

    @PostConstruct
    void init() {
        order.add(new Order("1491950358", "10151"));
        order.add(new Order("1680502395", "10151"));
        order.add(new Order("0321601912", "10151"));
    }

    public List<Order> listAll() {
        return order;
    }

    public Optional<Order> findById(String id) {
        return order.stream()
                .filter(b -> b.getId().equals(id))
                .findFirst();
    }
}