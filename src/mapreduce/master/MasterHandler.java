package mapreduce.master;

import mapreduce.Data;
import mapreduce.thrift.MasterInfo;
import mapreduce.thrift.MasterService;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MasterHandler implements MasterService.Iface {

    private static final Logger log = LoggerFactory.getLogger(MasterHandler.class);

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
        for (String dataId : split(inputFilename)) {
            log.info("split dataid = {}", dataId);
        }
    }

    public List<String> split(String inputFilename) throws TException {
        List<String> dataIds = new ArrayList<>();

        try {
            InputStream input = Data.readInput(inputFilename);
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
                dataIds.add(Data.writeIntermediate(buf, bytesBuffered));
            }

            return dataIds;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
