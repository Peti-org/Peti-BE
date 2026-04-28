package com.peti.backend.controller;

import com.peti.backend.dto.content.CommentDto;
import com.peti.backend.dto.content.RequestCommentDto;
import com.peti.backend.dto.content.RequestReactionDto;
import com.peti.backend.model.projection.UserProjection;
import com.peti.backend.security.annotation.HasUserRole;
import com.peti.backend.service.content.CommentService;
import com.peti.backend.service.content.ReactionService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/interactions")
@Tag(name = "Interactions", description = "Comments and reactions on content")
@SecurityRequirement(name = "bearerAuth")
public class InteractionController {

  private final CommentService commentService;
  private final ReactionService reactionService;

  @GetMapping("/comments")
  public ResponseEntity<List<CommentDto>> getComments(
      @RequestParam String targetType,
      @RequestParam UUID targetId,
      @Parameter(hidden = true) @ModelAttribute("userProjection") UserProjection userProjection) {
    return ResponseEntity.ok(commentService.getComments(targetType, targetId, userProjection.getUserId()));
  }

  @HasUserRole
  @PostMapping("/comments")
  public ResponseEntity<CommentDto> createComment(
      @Valid @RequestBody RequestCommentDto request,
      @Parameter(hidden = true) @ModelAttribute("userProjection") UserProjection userProjection) {
    return ResponseEntity.ok(commentService.createComment(request, userProjection.getUserId()));
  }

  @HasUserRole
  @DeleteMapping("/comments/{commentId}")
  public ResponseEntity<Void> deleteComment(
      @PathVariable UUID commentId,
      @Parameter(hidden = true) @ModelAttribute("userProjection") UserProjection userProjection) {
    commentService.deleteComment(commentId, userProjection.getUserId());
    return ResponseEntity.noContent().build();
  }

  @HasUserRole
  @PostMapping("/reactions")
  public ResponseEntity<Map<String, Object>> toggleReaction(
      @Valid @RequestBody RequestReactionDto request,
      @Parameter(hidden = true) @ModelAttribute("userProjection") UserProjection userProjection) {
    boolean added = reactionService.toggleReaction(request, userProjection.getUserId());
    long count = reactionService.countReactions(request.targetType(), request.targetId());
    return ResponseEntity.ok(Map.of("reacted", added, "totalReactions", count));
  }

  @ModelAttribute("userProjection")
  public UserProjection getUserProjection(Authentication authentication) {
    try {
      return (UserProjection) authentication.getPrincipal();
    } catch (ClassCastException e) {
      throw new IllegalArgumentException("Authentication is wrong");
    }
  }
}

