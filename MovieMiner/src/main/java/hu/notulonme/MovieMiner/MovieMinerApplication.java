package hu.notulonme.MovieMiner;

import hu.notulonme.MovieMiner.service.ReadManagerService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MovieMinerApplication implements CommandLineRunner {

	private static final Logger log = LogManager.getLogger(MovieMinerApplication.class);
	@Autowired
	private ReadManagerService readManagerService;

	public static void main(String[] args) {
		SpringApplication.run(MovieMinerApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		log.info("start executing the app");
		readManagerService.start();
	}
}
