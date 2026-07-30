package com.lautuquy.management.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedbacks")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id", nullable = true)
    private Account account;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "dish_id", nullable = true)
    private Dish dish;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "reply", columnDefinition = "TEXT")
    private String reply;

    @Column(name = "rating", nullable = false)
    private Integer rating = 5;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Feedback() {
        this.rating = 5;
    }

    public Feedback(Account account, Dish dish, String content) {
        this(account, dish, content, 5);
    }

    public Feedback(Account account, Dish dish, String content, Integer rating) {
        this.account = account;
        this.dish = dish;
        this.content = content;
        this.rating = (rating != null && rating >= 1 && rating <= 5) ? rating : 5;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Dish getDish() {
        return dish;
    }

    public void setDish(Dish dish) {
        this.dish = dish;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public Integer getRating() {
        return rating != null ? rating : 5;
    }

    public void setRating(Integer rating) {
        this.rating = (rating != null && rating >= 1 && rating <= 5) ? rating : 5;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
