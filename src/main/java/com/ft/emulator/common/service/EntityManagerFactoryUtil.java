package com.ft.emulator.common.service;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public enum EntityManagerFactoryUtil {

    INSTANCE;

    private final EntityManagerFactory emFactory;

    EntityManagerFactoryUtil() {
        emFactory = Persistence.createEntityManagerFactory("DbPersistenceUnit");
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return emFactory;
    }

    public void close() {
        emFactory.close();
    }
}