package com.skilltree.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable).cors(Customizer.withDefaults())
				.sessionManagement(
						session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable).authorizeHttpRequests(auth -> auth
						// 1. Разрешаем OPTIONS запросы (для CORS preflight)
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

						// 2. РАЗРЕШАЕМ SWAGGER БЕЗ АУТЕНТИФИКАЦИИ
						.requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**",
								"/webjars/**")
						.permitAll()

						// 3. Ваши существующие публичные эндпоинты
						.requestMatchers("/", "/api/hello", "/api/auth/**").permitAll()

						// 4. Остальные API требуют авторизации
						.requestMatchers("/api/**").authenticated().anyRequest().authenticated());

		http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();

		// ДЛЯ РАЗРАБОТКИ: Разрешаем всё (или добавьте "http://localhost:8080")
		config.setAllowedOrigins(List.of("http://localhost:5173"));

		// Если хотите безопасно, то добавьте явно:
		// config.setAllowedOrigins(List.of("http://localhost:5173",
		// "http://127.0.0.1:5173", "http://localhost:8080"));

		config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("*"));
		config.setAllowCredentials(true); // При "*" это может игнорироваться браузером, но для
											// Swagger UI лучше
											// оставить так или убрать allowCredentials

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}
}
