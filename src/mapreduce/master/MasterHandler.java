package mapreduce.master;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import mapreduce.Data;
import mapreduce.Metrics;
import mapreduce.ThriftClient;
import mapreduce.thrift.*;
import mapreduce.worker.WorkerHandler;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class MasterHandler implements MasterService.Iface {

    private static final Logger log = LoggerFactory.getLogger(MasterHandler.class);

    private final MasterParameters params;
    private final WorkerPool pool;
    private final ExecutorService executor = Executors.newFixedThreadPool(30);

    private int jobNumber = 0;
    private Meters meters = null;

    public MasterHandler(MasterParameters params, WorkerPool pool) {
        this.params = params;
        this.pool = pool;

    }

    private static class Meters {
        public final Timer mergesortTimer;
        public final Counter totalTasksCounter, failedTasksCounter;

        public Meters(int job) {
            this.mergesortTimer = Metrics.METRICS.timer(
                    MetricRegistry.name(MasterHandler.class, "mergesort-time-" + job));
            this.totalTasksCounter = Metrics.METRICS.counter(
                    MetricRegistry.name(MasterHandler.class, "total-tasks-counter-" + job));
            this.failedTasksCounter = Metrics.METRICS.counter(
                    MetricRegistry.name(MasterHandler.class, "failed-tasks-counter-" + job));
        }
    }

    @Override
    public MasterInfo info() throws TException {
        log.info("info, received");
        return new MasterInfo(params.chunkSize, params.redundancy, params.servers);
    }

    @Override
    public MergesortResult mergesort(String inputFilename) throws TException {
        log.info("mergesort, received, inputFilename = {}", inputFilename);
        meters = new Meters(jobNumber++);

        Timer.Context timer = meters.mergesortTimer.time();

        List<String> splits = split(inputFilename);
        int count = splits.size();
        log.info("mergesort, split, count = {}", count);

        Observable<String> sortedIds = Observable.from(splits)
                .map(this::sortTask)
                .map(task -> request(meters, task))
                .flatMap(this::retryAndRedundant);

        int mergeRounds = (int) Math.ceil(Math.log(count) / Math.log(params.chunksPerMerge)) + 1;

        String expandedId = Observable.just(1)
                .repeat(mergeRounds)
                .reduce(sortedIds, (unmergedIds, i) -> unmergedIds
                        .buffer(params.chunksPerMerge)
                        .map(this::mergeTask)
                        .map(task -> request(meters, task))
                        .flatMap(this::retryAndRedundant))
                .flatMap(x -> x)
                .map(this::expand)
                .toBlocking()
                .first();


        MergesortResult r = new MergesortResult(
                expandedId,
                timer.stop(),
                meters.totalTasksCounter.getCount(),
                meters.failedTasksCounter.getCount());

        Metrics.report();

        log.info("mergesort, done, outputId = {}", expandedId);
        return r;
    }

    private Observable<String> retryAndRedundant(Observable<String> unreliable) {
        return Observable.just(1)
                .repeat(params.redundancy)
                .flatMap(i -> unreliable
                        .doOnError(e -> log.error("request, error = {}", e.getMessage()))
                        .retry())
                .doOnNext(n -> log.info("request, worked, id = {}", n))
                .take(1);
    }

    private Function<ThriftClient<WorkerService.Client>, Callable<String>> sortTask(
            String inputId) {
        return c -> () -> c.with(client -> client.runSort(inputId));
    }

    private Function<ThriftClient<WorkerService.Client>, Callable<String>> mergeTask(
            List<String> inputIds) {
        return c -> () -> c.with(client -> client.runMerge(inputIds));
    }

    private Observable<String> request(
            Meters meters,
            Function<ThriftClient<WorkerService.Client>, Callable<String>> makeTask) {
        return Observable.just(1)
                .map(i -> pool.getWorker(10000))
                .map(makeTask::apply)
                .map(executor::submit)
                .doOnNext(c -> meters.totalTasksCounter.inc())
                .flatMap(Observable::from)
                .doOnError(e -> meters.failedTasksCounter.inc());
    }

    private List<String> split(String inputFilename) throws TException {
        List<String> dataIds = new ArrayList<>();

        try (InputStream input = Data.readInput(inputFilename)) {
            byte[] buf = new byte[params.chunkSize + 1024];

            while (true) {
                int bytesBuffered = 0;
                int bytesJustRead = input.read(buf, bytesBuffered, params.chunkSize);

                if (bytesJustRead == -1) { // eof
                    break;
                }
                bytesBuffered += bytesJustRead;

                while (buf[bytesBuffered - 1] != 32) { // didn't finish with space
                    bytesJustRead = input.read(buf, bytesBuffered, 1);

                    if (bytesJustRead == -1) { // eof
                        break;
                    } else {
                        bytesBuffered += bytesJustRead;
                    }
                }

                // should have read space or be at eof now
                String dataId = Data.newID();
                try (OutputStream o = Data.writeIntermediate(dataId)) {
                    o.write(buf, 0, bytesBuffered);
                }
                dataIds.add(dataId);
            }

            return dataIds;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String expand(String inputId) {
        try {
            String outputId = Data.newID();

            try (
                    InputStream input = Data.readIntermediate(inputId);
                    OutputStream output = Data.writeOutput(outputId)
            ) {
                Scanner s = new Scanner(input);
                PrintWriter w = new PrintWriter(output);

                for (int i = 0; i < WorkerHandler.MAX_DATA; i++) {
                    int count = Integer.parseInt(s.nextLine());

                    for (int j = 0; j < count; j++) {
                        w.print(i);
                        w.print(" ");
                    }

                    w.flush();
                }
            }

            Data.truncateEndSpace(outputId);
            return outputId;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
