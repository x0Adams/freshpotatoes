package hu.notulonme.MovieMiner.util;

import java.io.*;

public class TrackedFileInputStream extends FileInputStream {
    final private long size;
    private long read;

    public TrackedFileInputStream(String name) throws IOException {
        super(name);
        size = getChannel().size();
    }

    public TrackedFileInputStream(File file) throws IOException {
        super(file);
        size = getChannel().size();
    }

    public TrackedFileInputStream(FileDescriptor fdObj) throws IOException {
        super(fdObj);
        size = getChannel().size();
    }

    @Override
    public int read() throws IOException {
        int read = super.read();
        if (read == -1)
            return -1;

        this.read++;
        return read;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int read = super.read(b);
        if (read != -1)
            this.read++;
        return read;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int read = super.read(b, off, len);
        if (read == -1)
            return -1;

        this.read += read;
        return read;

    }

    @Override
    public byte[] readAllBytes() throws IOException {
        read = size;
        return super.readAllBytes();
    }

    @Override
    public byte[] readNBytes(int len) throws IOException {
        byte[] buffer = super.readNBytes(len);
        read += buffer.length;
        return buffer;
    }

    public int getPercentage() {
        return (int)((double) read / size * 100);
    }

    public long getSize() {
        return size;
    }

    public long getRead() {
        return read;
    }
}
