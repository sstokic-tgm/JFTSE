package com.ft.emulator.common.service;

import com.ft.emulator.common.validation.ValidationException;

public interface CRUDService<T> {

    T create(T entity) throws ValidationException;

    T read(Long id) throws ValidationException;

    T update(Long entityId, T entity) throws ValidationException;

    void delete(Long id) throws ValidationException;
}