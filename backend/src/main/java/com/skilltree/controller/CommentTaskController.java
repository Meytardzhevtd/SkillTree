package com.skilltree.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skilltree.Service.CommentsTaskService;
import com.skilltree.dto.comments.CommentTaskRequest;
import com.skilltree.dto.comments.CommentTaskResponse;

import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/comments")
public class CommentTaskController {
	private final CommentsTaskService commentsTaskService;

	public CommentTaskController(CommentsTaskService commentsTaskService) {
		this.commentsTaskService = commentsTaskService;
	}

	@PostMapping
	public ResponseEntity<CommentTaskResponse> create(@RequestBody CommentTaskRequest request,
			@RequestHeader("Authorization") String authHeader) {
		return ResponseEntity.ok(commentsTaskService.addComment(request, authHeader));
	}

	@GetMapping("/task/{taskId}")
	public ResponseEntity<List<CommentTaskResponse>> getByTask(@PathVariable Long taskId) {
		return ResponseEntity.ok(commentsTaskService.getCommentsByTaskId(taskId));
	}
}
