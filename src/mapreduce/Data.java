package mapreduce;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

public class Data {
    private static final Path INPUTS = Paths.get("work/input");
    private static final Path INTERMEDIATES = Paths.get("work/intermediate");
    private static final Path OUTPUTS = Paths.get("work/output");

    public static InputStream readInput(String inputFilename) throws IOException {
        Path p = INPUTS.resolve(inputFilename);
        return Files.newInputStream(p, StandardOpenOption.READ);
    }

    public static String writeIntermediate(byte[] buf) {
        UUID uuid = UUID.randomUUID();
        Path p = INTERMEDIATES.resolve(uuid.toString());
    }
}
