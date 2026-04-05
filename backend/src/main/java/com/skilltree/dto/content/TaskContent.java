package com.skilltree.dto.content;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Интерфейс для хранения тасок. У нас пока три вида задач (один вариант ответа,
 * несколько, написание кода). Это используется в Task::content
 */

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(value = OneAnswerTaskContent.class, name = "ONE_POSSIBLE_ANSWER"),
		@JsonSubTypes.Type(value = MultipleAnswerTaskContent.class, name = "MULTIPLE")})
public interface TaskContent {
	String getQuestion();
}
