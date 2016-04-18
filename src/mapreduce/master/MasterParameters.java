package mapreduce.master;

import mapreduce.thrift.Address;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class MasterParameters {

    public final List<Address> servers;

    public MasterParameters(List<Address> servers) {
        this.servers = servers;
    }

    public static MasterParameters parse(String[] args) {
        List<Address> servers = new ArrayList<>();

        try {
            for (String arg : args) {
                URI uri = new URI("mapreduce://" + arg);
                servers.add(new Address(uri.getHost(), uri.getPort()));
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        return new MasterParameters(servers);
    }
}
