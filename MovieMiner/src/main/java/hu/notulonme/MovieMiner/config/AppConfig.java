package hu.notulonme.MovieMiner.config;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import hu.notulonme.MovieMiner.util.TrackedFileInputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;

import java.io.*;
import java.util.concurrent.*;

@Configuration
public class AppConfig {
    @Value("${miner.dump.path}")
    private String path;

    @Value("${threadpool.size.max}")
    private int maxThreadPoolSize;

    @Value("${threadpool.size.initial}")
    private int initialThreadPoolSize;

    @Value("classpath:paths.json")
    private Resource wikiPath;


    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public TrackedFileInputStream trackedFileInputStreamProvider() throws IOException {
        return new TrackedFileInputStream(path);
    }

    @Bean
    public ExecutorService threadPoolProvider(){
        return new ThreadPoolExecutor(initialThreadPoolSize, maxThreadPoolSize,
                2L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>());
    }

    @Bean
    public DocumentContext wikiPath() throws IOException {
        return JsonPath.parse(wikiPath.getInputStream());
    }

}
