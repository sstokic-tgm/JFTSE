package com.jftse.server.core.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jftse.emulator.common.exception.ValidationException;
import com.jftse.entities.database.model.event.SavedVariables;
import com.jftse.entities.database.repository.event.SavedVariablesRepository;
import com.jftse.server.core.service.ScriptStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ScriptStateServiceImpl implements ScriptStateService {
    private final SavedVariablesRepository repo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Optional<String> get(String scriptId, Long accountId, String name) {
        return repo.findByScriptIdAndAccountIdAndName(scriptId, accountId, name)
                .map(SavedVariables::getData);
    }

    @Override
    public Optional<String> get(String scriptId, String name) {
        return repo.findByScriptIdAndNameAndAccountIdIsNull(scriptId, name)
                .map(SavedVariables::getData);
    }

    @Override
    public List<String> getAll(String scriptId, Long accountId) {
        return repo.findAllByScriptIdAndAccountId(scriptId, accountId)
                .stream()
                .map(SavedVariables::getData)
                .toList();
    }

    @Override
    public List<String> getAll(String scriptId) {
        return repo.findAllByScriptIdAndAccountIdIsNull(scriptId)
                .stream()
                .map(SavedVariables::getData)
                .toList();
    }


    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void set(String scriptId, Long accountId, String name, String data) {
        SavedVariables savedVar = repo.findByScriptIdAndAccountIdAndName(scriptId, accountId, name)
                .orElseGet(SavedVariables::new);

        savedVar.setScriptId(scriptId);
        savedVar.setAccountId(accountId);
        savedVar.setName(name);
        savedVar.setData(data);
        repo.save(savedVar);
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void set(String scriptId, String name, String data) {
        SavedVariables savedVar = repo.findByScriptIdAndNameAndAccountIdIsNull(scriptId, name)
                .orElseGet(SavedVariables::new);

        savedVar.setScriptId(scriptId);
        savedVar.setAccountId(null);
        savedVar.setName(name);
        savedVar.setData(data);
        repo.save(savedVar);
    }

    @Override
    public void delete(String scriptId, Long accountId, String name) {
        repo.findByScriptIdAndAccountIdAndName(scriptId, accountId, name)
                .ifPresent(repo::delete);
    }

    @Override
    public void delete(String scriptId, String name) {
        repo.findByScriptIdAndNameAndAccountIdIsNull(scriptId, name)
                .ifPresent(repo::delete);
    }

    @Override
    public <T> T getJson(String scriptId, Long accountId, String name, Class<T> clazz) {
        Optional<SavedVariables> savedVarOpt = repo.findByScriptIdAndAccountIdAndName(scriptId, accountId, name);
        return savedVarOpt.map(savedVar -> {
            try {
                return objectMapper.readValue(savedVar.getData(), clazz);
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to parse JSON data", e);
            }
        }).orElse(null);
    }

    @Override
    public <T> T getJson(String scriptId, String name, Class<T> clazz) {
        Optional<SavedVariables> savedVarOpt = repo.findByScriptIdAndNameAndAccountIdIsNull(scriptId, name);
        return savedVarOpt.map(savedVar -> {
            try {
                return objectMapper.readValue(savedVar.getData(), clazz);
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to parse JSON data", e);
            }
        }).orElse(null);
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public <T> void setJson(String scriptId, Long accountId, String name, T data) throws ValidationException {
        if (data == null) {
            throw new ValidationException("Data cannot be null");
        }

        try {
            String jsonData = objectMapper.writeValueAsString(data);
            set(scriptId, accountId, name, jsonData);
        } catch (Exception e) {
            throw new ValidationException("Failed to serialize data to JSON", e);
        }
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public <T> void setJson(String scriptId, String name, T data) throws ValidationException {
        if (data == null) {
            throw new ValidationException("Data cannot be null");
        }

        try {
            String jsonData = objectMapper.writeValueAsString(data);
            set(scriptId, name, jsonData);
        } catch (Exception e) {
            throw new ValidationException("Failed to serialize data to JSON", e);
        }
    }
}
