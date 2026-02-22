package hu.notulonme.MovieMiner.service;

import hu.notulonme.MovieMiner.repository.JsonDumpRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;

@Service
public class ReadManagerService {
    private static final Logger log = LogManager.getLogger(ReadManagerService.class);
    private ExecutorService threadPool;
    private ZipReaderService reader;
    private JsonDumpRepository repository;
    private Thread readerThread;
    private ApplicationContext context;

    public ReadManagerService(ExecutorService threadPool, ZipReaderService reader, JsonDumpRepository repository, ApplicationContext context) {
        this.threadPool = threadPool;
        this.reader = reader;
        this.repository = repository;
        readerThread = new Thread(reader);
        this.context = context;
    }

    public void start(){
        log.info("manager is started");
        readerThread.start();
        String line;
        try {
            while((line = repository.take()) != null) {
                DumpEntityProccessor proccessor = context.getBean(DumpEntityProccessor.class);
                proccessor.setWikiDump(line);
                threadPool.execute(() -> proccessor.run());
            }
            log.debug("repository was poisoned");
        } catch (InterruptedException e) {
            log.error("reading thread was interrupted");
        }

    }


}
