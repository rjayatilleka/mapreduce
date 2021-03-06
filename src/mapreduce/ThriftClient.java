package mapreduce;

import mapreduce.thrift.MasterService;
import mapreduce.thrift.WorkerService;
import org.apache.thrift.TException;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

/**
 * Encapsulates opening/closing Thrift transports, protocols, and clients.
 * Enables users to simply pass a lambda of work that needs to be done with
 * a client.
 *
 * Also handles retry logic.
 *
 * @param <T> Client to use
 */
public class ThriftClient<T extends TServiceClient> {

    private static final Logger log = LoggerFactory.getLogger(ThriftClient.class);

    private final int timeoutMs;
    private final String hostname;
    private final int port;
    private final Function<TBinaryProtocol, T> constructor;

    public ThriftClient(
            int timeoutMs, String hostname, int port, Function<TBinaryProtocol, T> constructor) {
        this.timeoutMs = timeoutMs;
        this.hostname = hostname;
        this.port = port;
        this.constructor = constructor;
    }

    public static ThriftClient<MasterService.Client> makeMasterClient(String hostname) {
        return new ThriftClient<>(0, hostname, 50000, MasterService.Client::new);
    }

    public static ThriftClient<WorkerService.Client> makeWorkerClient(
            int timeoutMs, String hostname, int port) {
        return new ThriftClient<>(timeoutMs, hostname, port, WorkerService.Client::new);
    }

    public <R> R retryWith(int retries, int delay, ThriftFunction<T, R> work) {
        for (int i = 0; i < retries; i++) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) { }

            try {
                log.info("retryWith, host = {}, port = {}, attempting {}", hostname, port, i);
                return with(work);
            } catch (Exception e) {
                log.error("retryWith, host = {}, port = {}, failed attempt {}",
                        hostname, port, i, e);
            }
        }

        log.error("retryWith, host = {}, port = {}, failed all attempts", hostname, port);
        throw new RuntimeException("All retries failed");
    }

    public <R> R with(ThriftFunction<T, R> work) {
        try (TTransport transport = new TSocket(hostname, port, timeoutMs, 0)) {
            transport.open();
            //log.info("with, starting, host = {}, port = {}", hostname, port);

            T client = constructor.apply(new TBinaryProtocol(transport));
            //log.info("with, made client");

            return work.apply(client);
        } catch (TException e) {
            //log.info("with, failed, host = {}, port = {}", hostname, port, e);
            throw new RuntimeException(e);
        }
    }

    public void run(ThriftConsumer<T> work) {
        this.with(client -> {
            work.accept(client);
            return null;
        });
    }
}
