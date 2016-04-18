package mapreduce.master;

public class Master {

    public static void main(String[] args) {
        MasterParameters params = MasterParameters.parse(args);

        new Thread(new MasterServer(params)).run();
    }
}
