package com.mns;

import io.micronaut.core.annotation.Creator;
import io.micronaut.serde.annotation.Serdeable;

import java.util.Objects;

@Serdeable
public class Order {

    private final String id;
    private final String location;

    @Creator
    public Order(String id, String location) {
        this.id = id;
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public String getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id='" + id + '\'' +
                ", location='" + location + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order other = (Order) o;
        return Objects.equals(id, other.id) &&
                Objects.equals(location, other.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, location);
    }
}