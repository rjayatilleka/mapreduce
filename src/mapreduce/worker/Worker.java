package mapreduce.worker;

import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.MetricRegistry;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Worker {

    private static final MetricRegistry METRICS = new MetricRegistry();

    public static void main(String[] args) throws IOException {
        WorkerParameters params = WorkerParameters.parse(args);

        new Thread(new WorkerServer(params)).run();

        setupMetrics(params.name);
    }

    private static void setupMetrics(String name) throws IOException {
        File metricsDir = new File("log/metrics/" + name + "/");
        Files.createDirectory(metricsDir.toPath());

        CsvReporter reporter = CsvReporter.forRegistry(METRICS)
                .formatFor(Locale.US)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build(metricsDir);

        reporter.start(1, TimeUnit.SECONDS);
    }
}
