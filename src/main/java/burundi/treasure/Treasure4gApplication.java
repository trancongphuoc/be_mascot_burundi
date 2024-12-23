package burundi.treasure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Treasure4gApplication {

	public static void main(String[] args) {
		SpringApplication.run(Treasure4gApplication.class, args);
	}

}
