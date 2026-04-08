package com.skilltree.mapper;

import com.skilltree.dto.UserSimpleDto;
import com.skilltree.dto.comments.CommentTaskResponse;
import com.skilltree.model.CommentTask;
import com.skilltree.model.Users;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CommentTaskMapper {

	CommentTaskMapper INSTANCE = Mappers.getMapper(CommentTaskMapper.class);

	@Mapping(source = "task.id", target = "taskId")
	@Mapping(source = "author", target = "author")
	@Mapping(source = "createdAt", target = "createdAt")
	@Mapping(source = "isEdited", target = "isEdited")
	CommentTaskResponse toResponse(CommentTask comment);

	default UserSimpleDto mapUserToSimpleDto(Users user) {
		return new UserSimpleDto(user.getId(), user.getUsername());
	}
}