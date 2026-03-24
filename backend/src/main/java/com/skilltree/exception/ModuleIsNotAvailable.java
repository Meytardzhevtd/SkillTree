package com.skilltree.exception;

public class ModuleIsNotAvailable extends RuntimeException {
	public ModuleIsNotAvailable(Long id) {
		super("Module with id = " + id + " is not available");
	}
}
