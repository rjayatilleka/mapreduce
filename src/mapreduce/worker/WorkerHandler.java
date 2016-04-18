package mapreduce.worker;

import mapreduce.Data;
import mapreduce.thrift.WorkerInfo;
import mapreduce.thrift.WorkerService;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;

public class WorkerHandler implements WorkerService.Iface {

    private static final Logger log = LoggerFactory.getLogger(WorkerHandler.class);

    private final WorkerParameters params;

    public WorkerHandler(WorkerParameters params) {
        this.params = params;
    }

    @Override
    public WorkerInfo info() throws TException {
        return new WorkerInfo(params.name);
    }

    @Override
    public String runSort(String inputId) throws TException {
        // read file
        try (InputStream input = Data.readIntermediate(inputId)) {
            Scanner s = new Scanner(input);
            int[] counts = new int[10000];

            while (s.hasNextInt()) {
                int n = s.nextInt();
                counts[n] += 1;
            }

            String outputId = Data.newIntermediate();

            try (OutputStream output = Data.writeIntermediate(outputId)) {
                PrintWriter writer = new PrintWriter(output);

                for (int n : counts) {
                    System.out.println(n);
                    writer.println(n);
                }
                writer.flush();
            }

            return outputId;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String runMerge(List<String> dataIds) throws TException {
        return null;
    }
}
