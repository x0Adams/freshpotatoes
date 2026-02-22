package hu.notulonme.MovieMiner.repository;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Repository
public class JsonDumpRepository {
    private BlockingQueue<String> lines;
    private boolean finished;
    private ApplicationEventPublisher publisher;

    public JsonDumpRepository(ApplicationEventPublisher publisher) {
        this.lines = new ArrayBlockingQueue<>(1000);
        this.publisher = publisher;
    }

    public void offer(String line) {
        if (!finished)
            lines.offer(line);
    }

    public String take() throws InterruptedException {
        if (lines.isEmpty() && finished) {
            //poison
            return null;
        }

        return lines.take();

    }

    public void finish() {
        finished = true;
    }



}
