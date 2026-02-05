package com.nguyen.server.model;

import java.util.Objects;

public class Container {
    private String id;
    private String locationDescription;

    public Container() {}

    public Container(String id, String locationDescription) {
        this.id = id;
        this.locationDescription = locationDescription;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLocationDescription() {
        return locationDescription;
    }

    public void setLocationDescription(String locationDescription) {
        this.locationDescription = locationDescription;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Container container = (Container) o;
        return Objects.equals(id, container.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
