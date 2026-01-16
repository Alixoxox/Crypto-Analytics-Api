package starter;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
@EnableAsync
@SpringBootApplication
@EnableScheduling
public class CryptoApplication {
	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.load();
		SpringApplication.run(CryptoApplication.class, args);
	}
}