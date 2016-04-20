package mapreduce.worker;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import mapreduce.Data;
import mapreduce.Metrics;
import mapreduce.thrift.TaskFailException;
import mapreduce.thrift.WorkerInfo;
import mapreduce.thrift.WorkerService;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class WorkerHandler implements WorkerService.Iface {

    private static final Logger log = LoggerFactory.getLogger(WorkerHandler.class);
    public static final int MAX_DATA = 10000;

    private final WorkerParameters params;
    private final Random random;

    private final Timer sortTimer, mergeTimer;

    public WorkerHandler(WorkerParameters params) {
        this.params = params;
        this.random = new Random();

        this.sortTimer = Metrics.METRICS.timer(
                MetricRegistry.name(WorkerHandler.class, "sort-time"));
        this.mergeTimer = Metrics.METRICS.timer(
                MetricRegistry.name(WorkerHandler.class, "merge-time"));
    }

    @Override
    public void heartbeat() throws TException {
        log.info("heartbeat, received");
    }

    @Override
    public WorkerInfo info() throws TException {
        log.info("info, received");
        return new WorkerInfo(params.name);
    }

    @Override
    public String runSort(String inputId) throws TException {
        log.info("runSort, received");
        checkTaskFailure("runSort");

        try (
                Timer.Context ignored = sortTimer.time();
                InputStream input = Data.readIntermediate(inputId)
        ) {
            Scanner s = new Scanner(input);
            int[] counts = new int[MAX_DATA];

            while (s.hasNextInt()) {
                int n = s.nextInt();
                counts[n] += 1;
            }

            log.info("runSort, writing");
            return writeData(counts);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String runMerge(List<String> dataIds) throws TException {
        log.info("runMerge, received");
        checkTaskFailure("runMerge");

        try (Timer.Context ignored = mergeTimer.time()) {
            int[] counts = new int[MAX_DATA];

            for (String inputId : dataIds) {
                log.info("runMerge, merging, inputId = {}", inputId);
                try (InputStream input = Data.readIntermediate(inputId)) {
                    Scanner s = new Scanner(input);

                    for (int i = 0; i < MAX_DATA; i++) counts[i] += Integer.parseInt(s.nextLine());
                }
            }

            log.info("runMerge, writing");
            return writeData(counts);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkTaskFailure(String method) throws TaskFailException {
        if (random.nextInt(100) < params.failPercent) {
            log.info("{}, task failure", method);
            throw new TaskFailException();
        }
    }

    private static String writeData(int[] counts) throws IOException{
        String outputId = Data.newID();

        try (OutputStream output = Data.writeIntermediate(outputId)) {
            PrintWriter writer = new PrintWriter(output);
            for (int n : counts) writer.println(n);
            writer.flush();
        }

        return outputId;
    }
}
