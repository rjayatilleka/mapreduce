package mapreduce;

import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.MetricRegistry;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Metrics {

    public static final MetricRegistry METRICS = new MetricRegistry();

    private static CsvReporter reporter = null;

    public static void setup(String name) throws IOException {
        File metricsDir = new File("log/metrics/" + name + "/");
        Files.createDirectories(metricsDir.toPath());

        reporter = CsvReporter.forRegistry(METRICS)
                .formatFor(Locale.US)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build(metricsDir);
    }

    public static void startReportingThread() {
        reporter.start(1, TimeUnit.SECONDS);
    }

    public static void report() {
        reporter.report();
    }
}
