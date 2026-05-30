package com.nopunnygames.pbservice.repository;

import com.nopunnygames.pbservice.entity.Product;
import com.nopunnygames.tanuki.core.repository.MasterRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for product records.
 */
public interface ProductRepository extends MasterRepository<Product, UUID> {
    /**
     * Finds a product by stable code.
     *
     * @param code stable code
     * @return matching product, if present
     */
    Optional<Product> findByCode(String code);
}
