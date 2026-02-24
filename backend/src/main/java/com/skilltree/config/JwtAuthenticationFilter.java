package com.skilltree.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.skilltree.Service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

	private final JwtService jwtService;

	public JwtAuthenticationFilter(JwtService jwtService) {
		this.jwtService = jwtService;
	}

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request,
			@NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
			throws ServletException, IOException {
		// 1) Читаем заголовок Authorization из входящего HTTP-запроса.
		// Ожидаемый формат: "Bearer <jwt-token>".
		String authHeader = request.getHeader("Authorization");

		// 2) Если заголовка нет или он не Bearer-формата — пропускаем дальше без
		// аутентификации.
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		// 3) Вырезаем сам токен после префикса "Bearer ".
		String token = authHeader.substring(7);

		try {
			// 4) Достаём email (subject) из токена.
			String email = jwtService.extractEmail(token);

			// 5) Если email есть и пользователь ещё не аутентифицирован в текущем запросе,
			// создаём Authentication и кладём в SecurityContext.
			if (email != null && SecurityContextHolder.getContext().getAuthentication() == null
					&& jwtService.isTokenValid(token, email)) {

				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
						email, null,
						// На этом шаге роли/права не используем, поэтому список прав пустой.
						java.util.Collections.emptyList());

				authentication
						.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		} catch (Exception ex) {
			// Токен невалидный/битый/просроченный — просто не аутентифицируем запрос.
			// Финальное решение (401/403) примет SecurityConfig на уровне правил доступа.
			log.debug("JWT filter skipped invalid token: {}", ex.getMessage());
		}

		// 6) Передаём управление следующему фильтру в цепочке.
		filterChain.doFilter(request, response);
	}
}
