package com.jftse.entities.database.repository.event;

import com.jftse.entities.database.model.event.SavedVariables;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavedVariablesRepository extends JpaRepository<SavedVariables, Long> {
    Optional<SavedVariables> findByScriptIdAndAccountIdAndName(String scriptId, Long accountId, String name);
    Optional<SavedVariables> findByScriptIdAndNameAndAccountIdIsNull(String scriptId, String name);

    List<SavedVariables> findAllByScriptIdAndAccountId(String scriptId, Long accountId);
    List<SavedVariables> findAllByScriptIdAndAccountIdIsNull(String scriptId);
}
