package com.croods.errorcontrollerdemo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.croods.errorcontrollerdemo.entity.ErrorEntity;

@Repository
public interface ErrorRepository extends JpaRepository<ErrorEntity, Long> {

}
