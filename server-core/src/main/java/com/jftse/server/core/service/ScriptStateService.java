package com.jftse.server.core.service;

import com.jftse.emulator.common.exception.ValidationException;

import java.util.List;
import java.util.Optional;

public interface ScriptStateService {
    Optional<String> get(String scriptId, Long accountId, String name);
    Optional<String> get(String scriptId, String name);
    List<String> getAll(String scriptId, Long accountId);
    List<String> getAll(String scriptId);
    void set(String scriptId, Long accountId, String name, String data);
    void set(String scriptId, String name, String data);
    void delete(String scriptId, Long accountId, String name);
    void delete(String scriptId, String name);
    <T> T getJson(String scriptId, Long accountId, String name, Class<T> clazz);
    <T> T getJson(String scriptId, String name, Class<T> clazz);
    <T> void setJson(String scriptId, Long accountId, String name, T data) throws ValidationException ;
    <T> void setJson(String scriptId, String name, T data) throws ValidationException;
}
