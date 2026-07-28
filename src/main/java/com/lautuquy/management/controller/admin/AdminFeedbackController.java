package com.lautuquy.management.controller.admin;

import com.lautuquy.management.entity.Feedback;
import com.lautuquy.management.service.FeedbackService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * AJAX endpoint — Admin trả lời feedback, trả về JSON để frontend cập nhật UI không reload trang.
     */
    @PostMapping("/{id}/reply-ajax")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> replyFeedbackAjax(
            @PathVariable Long id,
            @RequestParam("replyContent") String replyContent) {
        Map<String, Object> response = new HashMap<>();
        try {
            Feedback feedback = feedbackService.replyFeedback(id, replyContent);
            response.put("success", true);
            response.put("message", "Đã gửi câu trả lời thành công!");
            response.put("reply", feedback.getReply());
            response.put("feedbackId", id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Không thể trả lời: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
