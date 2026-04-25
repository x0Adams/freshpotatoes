package hu.notkulonme.DataTransferer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DataTransfererApplication implements CommandLineRunner {
	private static final Logger log = LoggerFactory.getLogger(DataTransfererApplication.class);

	private final TransferApplication transferApplication;
	private final boolean runOnStartup;

	public DataTransfererApplication(TransferApplication transferApplication,
									 @Value("${transfer.run-on-startup:true}") boolean runOnStartup) {
		this.transferApplication = transferApplication;
		this.runOnStartup = runOnStartup;
	}

	public static void main(String[] args) {
		SpringApplication.run(DataTransfererApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		if (!runOnStartup) {
			log.info("Startup transfer is disabled (`transfer.run-on-startup=false`).");
			return;
		}
		transferApplication.transfer();
	}
}
