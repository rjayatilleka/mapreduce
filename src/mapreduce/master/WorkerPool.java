package mapreduce.master;

import mapreduce.thrift.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class WorkerPool {

    private static final Logger log = LoggerFactory.getLogger(WorkerPool.class);

    private final int count;
    private final List<Address> workers;
    private final ConcurrentHashMap<Address, Boolean> accessible = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public WorkerPool(List<Address> workers) {
        this.count = workers.size();
        this.workers = workers;

        for (Address w : workers) {
            this.accessible.put(w, Boolean.TRUE);
        }
    }

    public Address getWorker() {
        while (true) {
            Address w = workers.get(random.nextInt(count));

            if (accessible.get(w)) {
                log.info("getWorker, found worker, w = {}", w);
                return w;
            } else {
                log.info("getWorker, bad worker, w = {}", w);
            }
        }
    }

    public void setWorker(Address w, boolean status) {
        accessible.put(w, status);
    }
}
