package com.skilltree.dto.content;

/**
 * Интерфейс для хранения тасок. У нас пока три вида задач (один вариант ответа,
 * несколько, написание кода). Это используется в Task::content
 */

public interface TaskContent {
	String getQuestion();
}
