package com.skilltree.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skilltree.dto.comments.CommentTaskRequest;
import com.skilltree.dto.comments.CommentTaskResponse;
import com.skilltree.exception.TaskNotFoundException;
import com.skilltree.exception.UserNotFoundException;
import com.skilltree.mapper.CommentTaskMapper;
import com.skilltree.model.CommentTask;
import com.skilltree.model.Task;
import com.skilltree.model.Users;
import com.skilltree.repository.CommentTaskRepository;
import com.skilltree.repository.TaskRepository;
import com.skilltree.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CommentsTaskService {
	private final CommentTaskRepository commentTaskRepository;
	private final JwtService jwtService;
	private final UserRepository userRepository;
	private final TaskRepository taskRepository;
	private final CommentTaskMapper commentTaskMapper;

	public CommentsTaskService(CommentTaskRepository commentTaskRepository, JwtService jwtService,
			UserRepository userRepository, TaskRepository taskRepository,
			CommentTaskMapper commentTaskMapper) {
		this.commentTaskRepository = commentTaskRepository;
		this.jwtService = jwtService;
		this.userRepository = userRepository;
		this.taskRepository = taskRepository;
		this.commentTaskMapper = commentTaskMapper;
	}

	@Transactional
	public CommentTaskResponse addComment(CommentTaskRequest request, String authHeader) {

		// TODO: по хорошему надо проверить, проходит ли юзер наш курс

		String token = authHeader.substring(7);
		String email = jwtService.extractEmail(token);

		if (!jwtService.isTokenValid(token, email)) {
			throw new RuntimeException("Invalid token");
		}

		Long userId = jwtService.extractUserId(token);
		Users author = userRepository.findById(userId)
				.orElseThrow(() -> new UserNotFoundException(userId));

		// ИСПРАВЛЕНО: проверяем что taskId не null
		if (request.taskId() == null) {
			throw new RuntimeException("taskId must not be null");
		}

		Task task = taskRepository.findById(request.taskId())
				.orElseThrow(() -> new TaskNotFoundException(request.taskId())); // ИСПРАВЛЕНО

		CommentTask comment = new CommentTask(null, request.text(), task, author,
				LocalDateTime.now(), false);

		CommentTask saved = commentTaskRepository.save(comment);

		return commentTaskMapper.toResponse(saved);
	}

	public List<CommentTaskResponse> getCommentsByTaskId(Long taskId) {
		List<CommentTask> comments = commentTaskRepository.findByTaskIdOrderByCreatedAtAsc(taskId);
		return comments.stream().map(commentTaskMapper::toResponse).collect(Collectors.toList());
	}
}