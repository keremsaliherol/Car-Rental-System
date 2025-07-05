package com.kerem.ordersystem.carrentalsystem.dao;

import java.util.List;
import java.util.Optional;

public interface BaseDAO<T, ID> {
    List<T> findAll();
    Optional<T> findById(ID id);
    T save(T entity);
    T update(T entity);
    boolean deleteById(ID id);
    long count();
} 