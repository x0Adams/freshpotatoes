package hu.notulonme.MovieMiner;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.repository.init.RepositoriesPopulatedEvent;
import org.springframework.stereotype.Repository;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Repository
class JsonDumpRepository {
    private BlockingQueue<String> lines;
    private boolean finished;
    private ApplicationEventPublisher publisher;

    public JsonDumpRepository(ApplicationEventPublisher publisher) {
        this.lines = new ArrayBlockingQueue<>(1000);
        this.publisher = publisher;
    }

    public void offer(String line) {
        lines.offer(line);
    }

    public String take() throws InterruptedException {
        if (lines.size() == 1 && finished)
            publisher.publishEvent(new Repositories);


    }

    public void finish() {
        finished = true;
    }


}
