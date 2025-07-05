package com.kerem.ordersystem.carrentalsystem.dao;

import com.kerem.ordersystem.carrentalsystem.model.Rental;
import java.time.LocalDate;
import java.util.List;

public interface RentalDAO extends BaseDAO<Rental, Integer> {
    List<Rental> findByCustomerId(Integer customerId);
    List<Rental> findByCarId(Integer carId);
    List<Rental> findByStatus(String status);
    List<Rental> findActiveRentals();
    List<Rental> findByDateRange(LocalDate startDate, LocalDate endDate);
    List<Rental> findOverdueRentals();
    boolean updateStatus(Integer rentalId, String status);
} 