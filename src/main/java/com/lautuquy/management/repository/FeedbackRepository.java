package com.lautuquy.management.repository;

import com.lautuquy.management.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    List<Feedback> findByAccountIdOrderByCreatedAtDesc(Long accountId);

    List<Feedback> findAllByOrderByCreatedAtDesc();
}
