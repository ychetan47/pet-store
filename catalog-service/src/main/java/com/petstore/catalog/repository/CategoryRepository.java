package com.petstore.catalog.repository;

import com.petstore.catalog.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByIsActiveTrueOrderByNameAsc();

    List<Category> findByParentIsNullAndIsActiveTrueOrderByNameAsc();

    List<Category> findByParentIdAndIsActiveTrueOrderByNameAsc(Long parentId);

    Optional<Category> findBySlugAndIsActiveTrue(String slug);

    Optional<Category> findBySlug(String slug);

    boolean existsBySlug(String slug);

    @Query(value = "WITH RECURSIVE category_tree AS (" +
            "  SELECT id, parent_id FROM categories WHERE id = :categoryId " +
            "  UNION ALL " +
            "  SELECT c.id, c.parent_id FROM categories c " +
            "  INNER JOIN category_tree ct ON c.parent_id = ct.id" +
            ") SELECT id FROM category_tree", nativeQuery = true)
    List<Long> findDescendantCategoryIds(@Param("categoryId") Long categoryId);
}
