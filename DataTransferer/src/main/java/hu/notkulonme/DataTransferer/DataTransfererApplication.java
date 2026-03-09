package hu.notkulonme.DataTransferer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DataTransfererApplication implements CommandLineRunner {

	@Autowired
	TransferApplication transferApplication;

	public static void main(String[] args) {
		SpringApplication.run(DataTransfererApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		transferApplication.transfer();
	}
}
