package com.skilltree.dto.module;

/**
 * Этот dto создан для того, чтобы передавать все модули при запросе по id
 * курса. На фронтенде выводятся в виде списка (List<ModuleSimpleDto>), если
 * isOpen = true, то можно кликнуть и открыть модуль. Тогда сделается GET-запрос
 * по id модуля и высветиться вся информация о модуле.
 */

public record ModuleSimpleDto(Long moduleId, String name, boolean isOpen) {
}
