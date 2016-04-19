package mapreduce.master;

public class Master {

    public static void main(String[] args) {
        MasterParameters params = MasterParameters.parse(args);

        WorkerPool pool = new WorkerPool(params.servers);
        new Thread(new MasterServer(params, pool)).run();
    }
}
