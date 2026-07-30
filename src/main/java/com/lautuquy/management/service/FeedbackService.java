package com.lautuquy.management.service;

import com.lautuquy.management.entity.Feedback;

import java.util.List;

public interface FeedbackService {

    Feedback createFeedback(String username, Long dishId, String content);

    Feedback createGuestOrUserFeedback(String username, Long dishId, String content);

    Feedback replyFeedback(Long feedbackId, String replyContent);

    List<Feedback> getFeedbacksByCustomer(String username);

    List<Feedback> getAllFeedbacks();
}
