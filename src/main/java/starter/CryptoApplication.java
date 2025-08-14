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
		System.setProperty("ATLAS_URL", dotenv.get("ATLAS_URL"));
		System.setProperty("COINGECKO_API_KEY",dotenv.get("COINGECKO_API_KEY"));
		SpringApplication.run(CryptoApplication.class, args);
	}
}