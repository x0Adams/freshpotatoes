package hu.notulonme.MovieMiner.service;

import hu.notulonme.MovieMiner.component.Bz2Reader;
import hu.notulonme.MovieMiner.repository.JsonDumpRepository;
import jakarta.annotation.PreDestroy;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ZipReaderService implements Runnable {
    private static final Logger log = LogManager.getLogger(ZipReaderService.class);
    private final JsonDumpRepository dumpRepository;
    private final Bz2Reader dump;

    public ZipReaderService(Bz2Reader dump, JsonDumpRepository dumpRepository) throws CompressorException {
        this.dumpRepository = dumpRepository;
        this.dump = dump;
    }

    @Override
    public void run() {
        read();
    }

    public void read() {
        try {
            dump.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String line;
        double gbRead = -1;
        try {
            while ((line = dump.readLine()) != null) {
                dumpRepository.offer(line);
                gbRead = logGB(gbRead, dump.getReadGB());
                log.debug("GB read: " + dump.getReadGB());
            }
        } catch (IOException e) {
            log.error("zip can't be read");
            throw new RuntimeException(e);
        }
        dumpRepository.finish();
        log.info("zip file is read, consisted of " + dump.getReadGB() + " GB");

    }


    private double logGB(double previous, double current) {
        if ((int) previous % 10 != (int) current % 10) {
            log.info(current + "GB is read");
        }
        return current;
    }

    @PreDestroy
    private void close() {
        try {
            dump.close();
        } catch (IOException e) {
            log.error(e.getStackTrace().toString());
        }
    }
}
