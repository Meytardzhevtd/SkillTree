package com.skilltree.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

	@NotBlank(message = "Имя пользователя не может быть пустым")
	@Size(min = 5, max = 50, message = "Имя пользователя должно быть от 3 до 50 символов")
	@Pattern(regexp = "^[a-zA-Z0-9_]+$",
			message = "Имя пользователя может содержать только буквы, цифры и подчеркивание")
	private String username;

	@NotBlank(message = "Email не может быть пустым")
	@Email(message = "Некорректный формат email")
	private String email;

	@NotBlank(message = "Пароль не может быть пустым")
	@Size(min = 8, message = "Пароль должен содержать минимум 8 символов")
	@Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$",
			message = "Пароль должен содержать хотя бы одну цифру, одну заглавную и одну строчную букву, один спецсимвол, и не содержать пробелов")
	private String password;
}