package com.ft.emulator.common.dao.helper;

public class OrderBy {

    private String property;
    private String direction;

    public OrderBy(String property, String direction) {

        this.property = property;
        this.direction = direction;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }
}