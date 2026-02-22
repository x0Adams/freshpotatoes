package hu.notulonme.MovieMiner.component;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
public class BufferedReaderSupplier {
    @Value("${reader.buffer.MB}")
    int bufferSize;

    public BufferedReader supply(FileInputStream file) throws CompressorException {
        BufferedInputStream bis = new BufferedInputStream(file);
        CompressorInputStream compressor = new CompressorStreamFactory().createCompressorInputStream(bis);
        return new BufferedReader(new InputStreamReader(compressor, StandardCharsets.UTF_8), bufferSize * 1024 * 1024);
    }
}
