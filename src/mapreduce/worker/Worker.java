package mapreduce.worker;

public class Worker {

    public static void main(String[] args) {
        WorkerParameters params = WorkerParameters.parse(args);

        new Thread(new WorkerServer(params)).run();
    }
}
