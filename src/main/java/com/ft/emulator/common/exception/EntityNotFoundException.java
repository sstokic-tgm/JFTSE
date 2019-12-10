package com.ft.emulator.common.exception;

public class EntityNotFoundException extends javax.persistence.EntityNotFoundException {

    private String entityName;

    private Long id;

    public EntityNotFoundException(String name, Long id) {

        this.entityName = name;
        this.id = id;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String name) {
        this.entityName = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}