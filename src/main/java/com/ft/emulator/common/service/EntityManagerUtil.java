package com.ft.emulator.common.service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.SynchronizationType;

public class EntityManagerUtil {

    public static EntityManager getEntityManager(EntityManagerFactory emFactory) {
	return emFactory.createEntityManager();
    }

    public static void close(EntityManager em) {
        em.close();
    }
}