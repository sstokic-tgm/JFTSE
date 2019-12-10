package com.ft.emulator.common.service;

import javax.persistence.EntityManagerFactory;

public abstract class Service {

    protected EntityManagerFactory entityManagerFactory;

    public Service(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }
}