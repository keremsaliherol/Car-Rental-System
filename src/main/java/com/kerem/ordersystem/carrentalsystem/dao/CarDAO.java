package com.kerem.ordersystem.carrentalsystem.dao;

import com.kerem.ordersystem.carrentalsystem.model.Car;
import java.util.List;

public interface CarDAO extends BaseDAO<Car, Integer> {
    List<Car> findByStatus(String status);
    List<Car> findByCategory(String category);
    List<Car> findAvailableCars();
    List<Car> findByMake(String make);
    List<Car> findByModel(String model);
    List<Car> findByYear(Integer year);
    boolean updateStatus(Integer carId, String status);
    boolean updateMileage(Integer carId, Double mileage);
} 