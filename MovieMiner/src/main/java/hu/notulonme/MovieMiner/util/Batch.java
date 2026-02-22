package hu.notulonme.MovieMiner.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class Batch<T> {
    private static final Logger log = LogManager.getLogger(Batch.class);
    private LinkedBlockingQueue<T> queue;
    public static final int BATCH_SIZE = 100;

    public Batch() {
        queue = new LinkedBlockingQueue<>(BATCH_SIZE);
    }

    public int size() {
        return queue.size();
    }

    public List<T> drain() {
        log.debug("batch was drained");
        List<T> buffer = new LinkedList<>();
        queue.drainTo(buffer);
        return buffer;
    }

    public void put(T entity) {
        log.info("entity was put inside batch");
        queue.add(entity);
    }
}
