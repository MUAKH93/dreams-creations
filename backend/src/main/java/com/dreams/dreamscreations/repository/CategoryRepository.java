package com.dreams.dreamscreations.repository;

import com.dreams.dreamscreations.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category,Long> {
}
