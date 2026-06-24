package com.skilltree.dto.module;

import lombok.Data;

@Data
public class UpdateModuleDto {
	private String name;
	private Boolean canBeOpen;
	private Float positionX;
	private Float positionY;
}