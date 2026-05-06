package com.jftse.server.core.service;

import com.jftse.emulator.common.exception.ValidationException;

import java.util.List;
import java.util.Optional;

public interface ScriptStateService {
    Optional<String> get(String scriptId, String name, Long accountId);
    Optional<String> get(String scriptId, String name);
    List<String> getAll(String scriptId, Long accountId);
    List<String> getAll(String scriptId);
    void set(String scriptId, String name, Long accountId, String data);
    void set(String scriptId, String name, String data);
    void delete(String scriptId, String name, Long accountId);
    void delete(String scriptId, String name);
    <T> T getJson(String scriptId, String name, Long accountId, Class<T> clazz);
    <T> T getJson(String scriptId, String name, Class<T> clazz);
    <T> void setJson(String scriptId, String name, Long accountId, T data) throws ValidationException ;
    <T> void setJson(String scriptId, String name, T data) throws ValidationException;
}
