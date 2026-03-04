package com.skilltree.exception;

public class TaskTypesNotFound extends RuntimeException {
    public TaskTypesNotFound(Long id) {
        super("Task type with id = " + id + " not found");
    }

}
