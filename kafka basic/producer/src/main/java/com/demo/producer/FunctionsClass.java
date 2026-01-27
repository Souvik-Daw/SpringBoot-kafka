package com.demo.producer;

import java.util.function.Function;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FunctionsClass {

	@Bean
	public Function<String, String> uppercase() {
		return value -> value.toUpperCase();
	}

}
