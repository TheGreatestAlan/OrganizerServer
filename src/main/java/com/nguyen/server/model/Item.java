package com.nguyen.server.model;

import java.util.Objects;

public class Item {
    private Long id;
    private String name;
    private String containerId;
    private int quantity;

    public Item() {}

    public Item(String name, String containerId, int quantity) {
        this.name = name;
        this.containerId = containerId;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return Objects.equals(name, item.name) && Objects.equals(containerId, item.containerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, containerId);
    }
}
