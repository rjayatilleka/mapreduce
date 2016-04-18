package mapreduce.worker;

import mapreduce.thrift.WorkerInfo;
import mapreduce.thrift.WorkerService;
import org.apache.thrift.TException;

import java.util.List;

public class WorkerHandler implements WorkerService.Iface {

    public WorkerHandler(WorkerParameters params) {
    }

    @Override
    public WorkerInfo info() throws TException {
        return null;
    }

    @Override
    public String runSort(String dataId) throws TException {
        return null;
    }

    @Override
    public String runMerge(List<String> dataIds) throws TException {
        return null;
    }
}
