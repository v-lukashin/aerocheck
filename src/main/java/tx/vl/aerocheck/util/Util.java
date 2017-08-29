package tx.vl.aerocheck.util;

import com.aerospike.client.Host;

public class Util {
    public static Host[] parseAerospikeHosts(String rawHosts) {
        Host[] hosts;

        if (rawHosts == null || rawHosts.isEmpty()) hosts = new Host[0];
        else {
            String[] hostPorts = rawHosts.split(",");
            hosts = new Host[hostPorts.length];

            for (int i = 0; i < hostPorts.length; i++) {
                String hostPort = hostPorts[i];
                String[] split = hostPort.split(":");
                Host host;
                if (split.length == 1) host = new Host(split[0], 3000);
                else if (split.length == 2) host = new Host(split[0], Integer.parseInt(split[1]));
                else {
                    System.err.println("Not parced : " + rawHosts);
                    host = null;
                }
                hosts[i] = host;
            }
        }

        return hosts;
    }
}
