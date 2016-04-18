package mapreduce.master;

import mapreduce.thrift.Address;
import mapreduce.thrift.MasterInfo;
import mapreduce.thrift.MasterService;
import org.apache.thrift.TException;

public class MasterHandler implements MasterService.Iface {

    public MasterHandler(MasterParameters parameters) {

    }

    @Override
    public MasterInfo info() throws TException {
        return null;
    }

    @Override
    public void mergesort(String inputFilename) throws TException {

    }

    @Override
    public void finishSort(String taskId, String dataId) throws TException {

    }

    @Override
    public void finishMerge(String taskId, String dataId) throws TException {

    }
}
