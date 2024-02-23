package com.sean.cyberweb.repository;

import com.sean.cyberweb.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE lower(p.name) LIKE lower(concat('%', ?1, '%'))")
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

}