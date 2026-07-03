package com.hotelbooking.models;

import java.util.Objects;

public class RoomCategory {
    private final String name;
    private final double basePrice;

    public RoomCategory(String name, double basePrice) {
        this.name = name;
        this.basePrice = basePrice;
    }

    public String getName() {
        return name;
    }

    public double getBasePrice() {
        return basePrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoomCategory category = (RoomCategory) o;
        return Objects.equals(name, category.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "RoomCategory{name='" + name + "', basePrice=" + basePrice + "}";
    }
}
