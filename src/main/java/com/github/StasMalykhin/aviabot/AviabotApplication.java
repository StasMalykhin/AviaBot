package com.github.StasMalykhin.aviabot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Stanislav Malykhin
 */
@SpringBootApplication
@EnableScheduling
public class AviabotApplication {

	public static void main(String[] args) {
		SpringApplication.run(AviabotApplication.class, args);
	}

}
