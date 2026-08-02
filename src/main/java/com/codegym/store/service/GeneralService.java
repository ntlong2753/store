package com.codegym.store.service;

import java.util.List;
import java.util.Optional;

public interface GeneralService<T,ID> {
    Iterable<T> findAll();

    Optional<T> findById(Long id);

    void save(T t);

    void remove(Long id);
}
