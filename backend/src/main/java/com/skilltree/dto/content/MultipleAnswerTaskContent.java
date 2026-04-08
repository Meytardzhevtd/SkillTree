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
public class MultipleAnswerTaskContent implements TaskContent {
	private String question;
	private List<String> options;
	private List<Integer> correctAnswers;
}