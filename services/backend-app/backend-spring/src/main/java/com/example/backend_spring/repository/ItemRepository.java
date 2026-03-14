package com.example.backend_spring.repository;

import com.example.backend_spring.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {}
