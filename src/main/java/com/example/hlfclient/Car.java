package com.example.hlfclient;

/*
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.j2objc.annotations.Property;

//@DataType()
public final class Car {

    @Property()
    private final String make;

    @Property()
    private final String model;

    @Property()
    private final String color;

    @Property()
    private final String owner;

    public String getMake() {
        return make;
    }

    public String getModel() {
        return model;
    }

    public String getColor() {
        return color;
    }

    public String getOwner() {
        return owner;
    }

    public Car(@JsonProperty("make") final String make, @JsonProperty("model") final String model,
            @JsonProperty("color") final String color, @JsonProperty("owner") final String owner) {
        this.make = make;
        this.model = model;
        this.color = color;
        this.owner = owner;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        Car other = (Car) obj;

        return Objects.deepEquals(new String[] {getMake(), getModel(), getColor(), getOwner()},
                new String[] {other.getMake(), other.getModel(), other.getColor(), other.getOwner()});
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMake(), getModel(), getColor(), getOwner());
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "@" + Integer.toHexString(hashCode()) + " [make=" + make + ", model="
                + model + ", color=" + color + ", owner=" + owner + "]";
    }
}
