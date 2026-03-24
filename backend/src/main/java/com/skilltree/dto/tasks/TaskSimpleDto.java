package com.skilltree.dto.tasks;

/**
 * Dto для кнопочки. То есть чисто чтобы была кнопочка (зеленая или черая в
 * зависимости от прохождения). Кликаешь по кнопочке, делается запрос по id
 * таски, ты переходишь в задачу
 */

public record TaskSimpleDto(Long taskId, boolean isCompleted) {
}
