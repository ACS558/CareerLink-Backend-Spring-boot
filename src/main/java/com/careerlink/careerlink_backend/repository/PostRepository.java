package com.careerlink.careerlink_backend.repository;

import com.careerlink.careerlink_backend.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByIsDeletedFalseOrderByIsPinnedDescCreatedAtDesc(Pageable pageable);
    Page<Post> findByAuthorIdAndIsDeletedFalseOrderByCreatedAtDesc(Long authorId, Pageable pageable);
}