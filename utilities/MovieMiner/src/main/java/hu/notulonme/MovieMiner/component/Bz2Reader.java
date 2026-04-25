package hu.notulonme.MovieMiner.component;

import hu.notulonme.MovieMiner.util.TrackedInputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.CharBuffer;

@Component
public class Bz2Reader extends Reader {

    private Process process;
    private BufferedReader reader;
    private ProcessBuilder builder;
    private TrackedInputStream trackedInputStream;

    @Value("${reader.buffer.MB}")
    int bufferSize;

    public Bz2Reader(File inputFile) {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("bzip2", "-dc", inputFile.getAbsolutePath());
        this.builder = builder;
    }

    public String readLine() throws IOException {
        return reader.readLine();
    }

    public void start() throws IOException {
        process = builder.start();
        trackedInputStream = new TrackedInputStream(process.getInputStream());
        reader = new BufferedReader(new InputStreamReader(trackedInputStream), bufferSize * 1024 * 1024);
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        return reader.read(cbuf, off, len);
    }

    @Override
    public boolean ready() throws IOException {
        return reader.ready();
    }

    @Override
    public int read(char[] cbuf) throws IOException {
        return reader.read(cbuf);
    }

    @Override
    public long skip(long n) throws IOException {
        return reader.skip(n);
    }

    @Override
    public int read() throws IOException {
        return reader.read();
    }

    @Override
    public int read(CharBuffer target) throws IOException {
        return reader.read(target);
    }


    @Override
    public void close() throws IOException {
        reader.close();
        process.destroy();
    }

    public double getReadGB(){
        long read = trackedInputStream.getRead();
        double readGB = read / Math.pow(1000, 3);
        return ((int)(readGB * 10) / 10d);
    }
}
