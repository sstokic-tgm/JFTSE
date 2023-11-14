package com.jftse.emulator.server.core.jdbc;

import com.jftse.entities.database.model.gameserver.GameServer;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Getter
@Service
@Log4j2
@Transactional(isolation = Isolation.SERIALIZABLE)
public class JdbcUtil {
    @PersistenceContext
    private EntityManager entityManager;

    @PostConstruct
    public void init() {
        log.info("JdbcUtil initialized");
    }

    public interface Operation {
        void doInTransaction(final EntityManager entityManager);
    }

    public void execute(Operation operation) {
        operation.doInTransaction(entityManager);
    }
}
