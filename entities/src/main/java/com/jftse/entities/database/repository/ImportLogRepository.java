package com.jftse.entities.database.repository;

import com.jftse.entities.database.model.ImportLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImportLogRepository extends JpaRepository<ImportLog, Long> {
    Optional<ImportLog> findByFileName(String fileName);
}
