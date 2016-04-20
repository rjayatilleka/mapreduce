package mapreduce.master;

import mapreduce.thrift.Address;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Usage: <MB chunk size> <chunks per merge> <redundancy> [<server>]...
 */
public class MasterParameters {

    public final int chunkSize;
    public final int chunksPerMerge;
    public final int redundancy;
    public final List<Address> servers;
    public final String name = "master";

    public MasterParameters(
            int chunkSize, int chunksPerMerge, int redundancy, List<Address> servers) {
        this.chunkSize = chunkSize;
        this.chunksPerMerge = chunksPerMerge;
        this.redundancy = redundancy;
        this.servers = servers;
    }

    public static MasterParameters parse(String[] args) {
        int chunkSize = Integer.parseInt(args[0]);
        int chunksPerMerge = Integer.parseInt(args[1]);
        int redundancy = Integer.parseInt(args[2]);
        List<Address> servers = new ArrayList<>();

        try {
            for (int i = 3; i < args.length; i++) {
                URI uri = new URI("mapreduce://" + args[i]);
                servers.add(new Address(uri.getHost(), uri.getPort()));
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        return new MasterParameters(chunkSize, chunksPerMerge, redundancy, servers);
    }
}
