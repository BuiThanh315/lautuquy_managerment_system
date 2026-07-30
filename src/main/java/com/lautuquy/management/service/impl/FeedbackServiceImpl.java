package com.lautuquy.management.service.impl;

import com.lautuquy.management.entity.Account;
import com.lautuquy.management.entity.Dish;
import com.lautuquy.management.entity.Feedback;
import com.lautuquy.management.exception.ResourceNotFoundException;
import com.lautuquy.management.repository.AccountRepository;
import com.lautuquy.management.repository.DishRepository;
import com.lautuquy.management.repository.FeedbackRepository;
import com.lautuquy.management.service.FeedbackService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final AccountRepository accountRepository;
    private final DishRepository dishRepository;

    public FeedbackServiceImpl(FeedbackRepository feedbackRepository,
                               AccountRepository accountRepository,
                               DishRepository dishRepository) {
        this.feedbackRepository = feedbackRepository;
        this.accountRepository = accountRepository;
        this.dishRepository = dishRepository;
    }

    @Override
    @Transactional
    public Feedback createFeedback(String username, Long dishId, String content) {
        return createGuestOrUserFeedback(username, dishId, content);
    }

    @Override
    @Transactional
    public Feedback createGuestOrUserFeedback(String username, Long dishId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Nội dung phản hồi không được để trống.");
        }

        Account account = null;
        if (username != null && !username.trim().isEmpty()) {
            account = accountRepository.findByUsername(username).orElse(null);
        }

        Dish dish = null;
        if (dishId != null && dishId > 0) {
            dish = dishRepository.findById(dishId).orElse(null);
        }

        Feedback feedback = new Feedback(account, dish, content.trim());
        return feedbackRepository.save(feedback);
    }

    @Override
    @Transactional
    public Feedback replyFeedback(Long feedbackId, String replyContent) {
        if (replyContent == null || replyContent.trim().isEmpty()) {
            throw new IllegalArgumentException("Nội dung trả lời không được để trống.");
        }

        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResourceNotFoundException("Phản hồi", feedbackId));

        feedback.setReply(replyContent.trim());
        return feedbackRepository.save(feedback);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Feedback> getFeedbacksByCustomer(String username) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản", username));
        return feedbackRepository.findByAccountIdOrderByCreatedAtDesc(account.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Feedback> getAllFeedbacks() {
        return feedbackRepository.findAllByOrderByCreatedAtDesc();
    }
}
