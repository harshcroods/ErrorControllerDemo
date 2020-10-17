package com.croods.errorcontrollerdemo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.croods.errorcontrollerdemo.entity.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

}
