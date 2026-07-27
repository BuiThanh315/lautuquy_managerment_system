package com.lautuquy.management.controller.admin;

import com.lautuquy.management.entity.Feedback;
import com.lautuquy.management.service.FeedbackService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/feedbacks")
public class AdminFeedbackController {

    private final FeedbackService feedbackService;

    public AdminFeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @GetMapping
    public String listFeedbacks(Model model) {
        List<Feedback> feedbacks = feedbackService.getAllFeedbacks();
        model.addAttribute("feedbacks", feedbacks);
        model.addAttribute("pageTitle", "Quản Lý Phản Hồi — Admin");
        return "admin/feedbacks";
    }

    @PostMapping("/{id}/reply")
    public String replyFeedback(@PathVariable Long id,
                                @RequestParam("replyContent") String replyContent,
                                RedirectAttributes redirectAttributes) {
        try {
            feedbackService.replyFeedback(id, replyContent);
            redirectAttributes.addFlashAttribute("successMessage", "Đã gửi câu trả lời cho phản hồi #" + id);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể trả lời: " + e.getMessage());
        }
        return "redirect:/admin/feedbacks";
    }
}
