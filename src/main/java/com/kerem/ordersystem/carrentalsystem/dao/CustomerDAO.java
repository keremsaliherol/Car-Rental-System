package com.kerem.ordersystem.carrentalsystem.dao;

import com.kerem.ordersystem.carrentalsystem.model.Customer;
import java.util.List;
import java.util.Optional;

public interface CustomerDAO extends BaseDAO<Customer, Integer> {
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByDriverLicense(String driverLicense);
    List<Customer> findByFullNameContaining(String name);
    List<Customer> findByPhone(String phone);
} 