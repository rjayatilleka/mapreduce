package mapreduce.master;

import mapreduce.Data;
import mapreduce.ThriftClient;
import mapreduce.thrift.MasterInfo;
import mapreduce.thrift.MasterService;
import mapreduce.thrift.WorkerService;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
        log.info("mergesort, split, count = {}", splits.size());

        List<String> sortedIds = Observable.merge(
                Observable.from(splits)
                        .map(this::sortRequest)
                        .map(this::retryAndRedundant))
                .toList()
                .toBlocking()
                .first();

        log.info("mergesort, sorted, count = {}", sortedIds.size());
        for (String sortedId : sortedIds)
            log.info("mergesort, sorted, id = {}", sortedId);

        return "";
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
                String dataId = Data.newIntermediate();
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
