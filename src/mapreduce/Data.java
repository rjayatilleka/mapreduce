package mapreduce;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    public static String writeIntermediate(byte[] buf, int bytesBuffered) throws IOException {
        String uuid = UUID.randomUUID().toString();
        Path p = INTERMEDIATES.resolve(uuid);

        try (OutputStream s = Files.newOutputStream(p, StandardOpenOption.CREATE_NEW)) {
            s.write(buf, 0, bytesBuffered);
        }

        return uuid;
    }
}
