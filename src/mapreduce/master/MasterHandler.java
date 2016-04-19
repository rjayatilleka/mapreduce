package mapreduce.master;

import mapreduce.Data;
import mapreduce.thrift.MasterInfo;
import mapreduce.thrift.MasterService;
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

public class MasterHandler implements MasterService.Iface {

    private static final Logger log = LoggerFactory.getLogger(MasterHandler.class);

    private final MasterParameters params;
    private final WorkerPool pool;
    private final ExecutorService executor = Executors.newFixedThreadPool(30);

    public MasterHandler(MasterParameters params, WorkerPool pool) {
        this.params = params;
        this.pool = pool;
    }

    @Override
    public MasterInfo info() throws TException {
        log.info("info, received");
        return new MasterInfo(params.chunkSize, params.redundancy, params.servers);
    }

    @Override
    public String mergesort(String inputFilename) throws TException {
        log.info("mergesort, received, inputFilename = {}", inputFilename);

        List<String> splits = split(inputFilename);
        int count = splits.size();
        log.info("mergesort, split, count = {}", count);

        Observable<String> sortedIds = Observable.from(splits)
                .map(this::sortRequest)
                .flatMap(this::retryAndRedundant);

        int mergeRounds = (int) Math.ceil(Math.log(count) / Math.log(params.chunksPerMerge)) + 1;

        String expandedId = Observable.just(1)
                .repeat(mergeRounds)
                .reduce(sortedIds, (unmergedIds, i) -> unmergedIds
                        .buffer(params.chunksPerMerge)
                        .map(this::mergeRequest)
                        .flatMap(this::retryAndRedundant))
                .flatMap(x -> x)
                .map(this::expand)
                .toBlocking()
                .first();

        log.info("mergesort, done, outputId = {}", expandedId);

        return expandedId;
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

    private Observable<String> retryAndRedundant(Observable<String> unreliable) {
        return Observable.just(1)
                .repeat(params.redundancy)
                .flatMap(i -> unreliable.retry())
                .take(1);
    }

    private Observable<String> sortRequest(final String inputId) {
        return Observable.just(1)
                .map(i -> pool.getWorker(0))
                .map(c -> (Callable<String>) () -> c.with(client -> client.runSort(inputId)))
                .map(executor::submit)
                .flatMap(Observable::from);
    }

    private Observable<String> mergeRequest(List<String> inputIds) {
        return Observable.just(1)
                .map(i -> pool.getWorker(0))
                .map(c -> (Callable<String>) () -> c.with(client -> client.runMerge(inputIds)))
                .map(executor::submit)
                .flatMap(Observable::from);
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
}
