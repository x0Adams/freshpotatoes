package hu.notulonme.MovieMiner.util;

import org.springframework.data.mongodb.core.aggregation.ArrayOperators;

import java.io.*;

public class TrackedInputStream extends InputStream{
    private long read;
    private InputStream inputStream;

    public TrackedInputStream(InputStream input) {

        inputStream = input;
    }

    @Override
    public int read() throws IOException {
        int read = inputStream.read();
        if (read == -1)
            return -1;

        this.read++;
        return read;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int read = inputStream.read(b);
        if (read != -1)
            this.read++;
        return read;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int read = inputStream.read(b, off, len);
        if (read == -1)
            return -1;

        this.read += read;
        return read;

    }

    @Override
    public byte[] readAllBytes() throws IOException {
        var buffer = super.readAllBytes();
        read = buffer.length;
        return buffer;
    }

    @Override
    public byte[] readNBytes(int len) throws IOException {
        byte[] buffer = inputStream.readNBytes(len);
        read += buffer.length;
        return buffer;
    }

    public long getRead() {
        return read;
    }
}
