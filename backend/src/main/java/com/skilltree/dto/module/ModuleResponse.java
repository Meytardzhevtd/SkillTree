package com.skilltree.dto.module;

import java.util.List;

import com.skilltree.dto.tasks.TaskSimpleDto;

/**
 * Этот dto возвращается при GET-запросе по id модуля. Важно, что если этот dto
 * возвращается, то гарантируется, что модуль доступен. Когда мы возращаем
 * ModuleResponse. Мы не видим содержимое тасок. Мы видим список тасок. Можно
 * кликнуть по кнопочке таски и откроется страничка с самой задачей.
 *
 */

public record ModuleResponse(Long moduleId, String name, List<TaskSimpleDto> tasks, Long courseId) {
}
