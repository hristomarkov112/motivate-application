//package app.web;
//
//import app.comment.model.Comment;
//import app.comment.service.CommentService;
//import app.post.model.Post;
//import app.post.service.PostService;
//import app.security.AuthenticationMetaData;
//import app.user.model.User;
//import app.user.service.UserService;
//import app.web.dto.CommentRequest;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.PutMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.servlet.ModelAndView;
//
////import java.util.UUID;
////
////@Controller
////@RequestMapping("/comments")
////public class CommentController {
////
////    private final PostService postService;
////    private final CommentService commentService;
////    private final UserService userService;
////
////    @Autowired
////    public CommentController(PostService postService, CommentService commentService, UserService userService) {
////        this.postService = postService;
////        this.commentService = commentService;
////        this.userService = userService;
////    }
//
////    @PostMapping("/{id}")
////    public ModelAndView addComment(@PathVariable UUID id, CommentRequest commentRequest, @AuthenticationPrincipal AuthenticationMetaData authenticationMetaData) {
////
////        User user = userService.getById(authenticationMetaData.getId());
////        Post post = postService.getById();
////
////        Comment comment = commentService.createComment(commentRequest, user);
////        postService.addComment(id, comment);
////
////        ModelAndView modelAndView = new ModelAndView();
////        modelAndView.addObject("comment", comment);
////        modelAndView.addObject("post", post);
////
////
////        return new ModelAndView("redirect:/home");
////    }
//
//
//
////    @PutMapping("/{id}")
////          public ModelAndView addComment(@PathVariable UUID id, @AuthenticationPrincipal AuthenticationMetaData authenticationMetaData) {
////
////        postService.addComment(id);
////
////        return new ModelAndView("redirect:/home");
////    }
//}
