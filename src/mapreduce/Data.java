package mapreduce;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

public class Data {

    private static final Path INPUTS = Paths.get("work/input");
    private static final Path INTERMEDIATES = Paths.get("work/intermediate");
    private static final Path OUTPUTS = Paths.get("work/output");

    public static String newID() throws IOException {
        return UUID.randomUUID().toString();
    }

    public static InputStream readInput(String inputFilename) throws IOException {
        Path p = INPUTS.resolve(inputFilename);
        return Files.newInputStream(p, StandardOpenOption.READ);
    }

    public static InputStream readIntermediate(String filename) throws IOException {
        Path p = INTERMEDIATES.resolve(filename);
        return Files.newInputStream(p, StandardOpenOption.READ);
    }

    public static OutputStream writeIntermediate(String dataId) throws IOException {
        Path p = INTERMEDIATES.resolve(dataId);
        return Files.newOutputStream(p, StandardOpenOption.CREATE_NEW);
    }

    public static OutputStream writeOutput(String dataId) throws IOException {
        Path p = OUTPUTS.resolve(dataId);
        return Files.newOutputStream(p, StandardOpenOption.CREATE_NEW);
    }

    public static void truncateEndSpace(String dataId) throws IOException {
        Path p = OUTPUTS.resolve(dataId);
        long size = Files.size(p);
        FileChannel.open(p, StandardOpenOption.WRITE).truncate(size - 1);
    }
}
