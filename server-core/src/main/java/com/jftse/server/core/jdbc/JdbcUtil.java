package com.jftse.server.core.jdbc;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.function.Function;

@Getter
@Service
@Log4j2
@Transactional(isolation = Isolation.READ_COMMITTED)
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

    public <T> T execute(Function<EntityManager, T> function) {
        return function.apply(entityManager);
    }
}
