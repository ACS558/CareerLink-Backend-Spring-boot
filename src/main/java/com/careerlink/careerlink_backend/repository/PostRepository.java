package com.careerlink.careerlink_backend.repository;

import com.careerlink.careerlink_backend.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("""
    SELECT p FROM Post p
    WHERE p.isDeleted = false
    AND (
        :viewerRole IN ('STUDENT', 'ADMIN')
        OR p.authorId = :viewerUserId
    )
    ORDER BY p.isPinned DESC, p.createdAt DESC
    """)
    Page<Post> findVisibleFeed(
            @Param("viewerRole") String viewerRole,
            @Param("viewerUserId") Long viewerUserId,
            Pageable pageable);
    Page<Post> findByIsDeletedFalseOrderByIsPinnedDescCreatedAtDesc(Pageable pageable);
    Page<Post> findByAuthorIdAndIsDeletedFalseOrderByCreatedAtDesc(Long authorId, Pageable pageable);
}