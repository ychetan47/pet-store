package com.petstore.category.repository;

import com.petstore.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findBySlugAndIsActiveTrue(String slug);

    Optional<Category> findByIdAndIsActiveTrue(Long id);

    List<Category> findByParentCategoryIsNullAndIsActiveTrue();

    List<Category> findByParentCategoryIdAndIsActiveTrue(Long parentId);

    List<Category> findAllByIsActiveTrue();

    @Query(value = "WITH RECURSIVE category_tree AS (" +
            "  SELECT id FROM categories WHERE id = :categoryId AND is_active = TRUE " +
            "  UNION ALL " +
            "  SELECT c.id FROM categories c " +
            "  INNER JOIN category_tree ct ON c.parent_id = ct.id WHERE c.is_active = TRUE" +
            ") SELECT id FROM category_tree", nativeQuery = true)
    List<Long> findDescendantCategoryIds(@Param("categoryId") Long categoryId);
}
