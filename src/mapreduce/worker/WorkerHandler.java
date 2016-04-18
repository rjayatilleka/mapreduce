package mapreduce.worker;

import mapreduce.thrift.BusyException;
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
    public String runSort(String dataId) throws BusyException, TException {
        return null;
    }

    @Override
    public String runMerge(List<String> dataIds) throws BusyException, TException {
        return null;
    }

    @Override
    public void cancel(String taskId) throws TException {

    }
}
