package mapreduce.master;

import mapreduce.thrift.MasterInfo;
import mapreduce.thrift.MasterService;
import org.apache.thrift.TException;

public class MasterHandler implements MasterService.Iface {

    private final MasterParameters params;

    public MasterHandler(MasterParameters params) {
        this.params = params;
    }

    @Override
    public MasterInfo info() throws TException {
        return new MasterInfo(params.chunkSize, params.redundancy, params.servers);
    }

    @Override
    public void mergesort(String inputFilename) throws TException {
        
    }
}
