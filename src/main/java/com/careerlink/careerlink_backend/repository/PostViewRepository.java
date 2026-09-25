package com.careerlink.careerlink_backend.repository;

import com.careerlink.careerlink_backend.entity.PostView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostViewRepository extends JpaRepository<PostView, Long> {
    boolean existsByPostIdAndViewedById(Long postId, Long viewedById);
    long countByPostId(Long postId);
}