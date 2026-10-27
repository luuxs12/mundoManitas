package com.mundomanitas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

/**
 * Punto de entrada de Mundo Manitas.
 *
 * <p>{@code pageSerializationMode = VIA_DTO} serializa los {@code Page} de la
 * API con una estructura estable ({@code content} + {@code page}) en lugar de
 * exponer el {@code PageImpl} interno de Spring Data.</p>
 */
@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class MundomanitasApplication {

	public static void main(String[] args) {
		SpringApplication.run(MundomanitasApplication.class, args);
	}

}
