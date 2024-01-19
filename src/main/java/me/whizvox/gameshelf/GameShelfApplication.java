package me.whizvox.gameshelf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GameShelfApplication {

	public static void main(String[] args) {
		SpringApplication.run(GameShelfApplication.class, args);
	}

}
