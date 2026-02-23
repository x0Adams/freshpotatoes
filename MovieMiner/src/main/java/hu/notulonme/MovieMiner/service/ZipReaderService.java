package hu.notulonme.MovieMiner.service;

import hu.notulonme.MovieMiner.component.BufferedReaderSupplier;
import hu.notulonme.MovieMiner.repository.JsonDumpRepository;
import hu.notulonme.MovieMiner.util.TrackedFileInputStream;
import jakarta.annotation.PreDestroy;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;

@Service
public class ZipReaderService implements Runnable{
    private static final Logger log = LogManager.getLogger(ZipReaderService.class);
    private final JsonDumpRepository dumpRepository;
    private final BufferedReader dump;
    private final TrackedFileInputStream tracker;

    public ZipReaderService(JsonDumpRepository dumpRepository, TrackedFileInputStream tracker, BufferedReaderSupplier supplier) throws CompressorException {
        this.dumpRepository = dumpRepository;
        this.dump = supplier.supply(tracker);
        this.tracker = tracker;
    }

    @Override
    public void run() {
        read();
    }

    public void read() {
        String line;
        long lineCount = 0;
        int percentage = -1;
        try {
            while ((line = dump.readLine()) != null) {
                dumpRepository.offer(line);
                logLineCount(lineCount);
                percentage = logPercentage(percentage, tracker.getPercentage());
                log.debug(tracker.getRead());
            }
        } catch (IOException e) {
            log.error("zip can't be read");
            throw new RuntimeException(e);
        }
        dumpRepository.finish();
        log.info("zip file is read, consisted of " + lineCount + " lines");

    }

    private void logLineCount(long lineCount) {
        if (++lineCount % 1000 == 0) {
            log.debug("lines read: " + lineCount);
        } else {
            log.debug("lines read: " + lineCount);
        }
    }
    private int logPercentage(int previous, int current){
        if (previous != current) {
            log.info("zip is read: "+current + "%");
            return current;
        }
        return previous;
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
