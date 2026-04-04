package com.skilltree.dto.content;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OneAnswerTaskContent implements TaskContent {
	private String question;
	private List<String> options; // варианты ответа
	private int indexCorrectAnswer;
}
