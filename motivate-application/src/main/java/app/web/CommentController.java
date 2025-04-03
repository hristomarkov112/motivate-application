package app.web;

import app.comment.service.CommentService;
import app.web.dto.CommentRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;


@Controller
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/new")
    public String showCommentForm() {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("comments");
        modelAndView.addObject("comment", new CommentRequest());
        return "/posts";
    }

    @GetMapping("/delete/{id}")
    public String deleteComment(@PathVariable UUID id) {
        commentService.deleteComment(id);
        return "redirect:/posts";
    }
}
