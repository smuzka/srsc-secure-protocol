/* hjUDPproxy, for use in 2024
 */

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import DSTP.EncryptedDatagramPacket;
import DSTP.EncryptedDatagramSocket;
import DSTP.EncryptedMulticastSocket;
import DSTP.EncryptedSocket;
import SHP.SHPClient;

class hjUDPproxy {
    public static void main(String[] args) throws Exception {
        if (args.length != 7) {
            System.out.println("Use: hjUDPproxy <username> <password> <server> <tcp_port> <movie> <udp_port> <player_port>");
            System.out.println("username: username as registered in the user database, server side password: user password\n" +
                    "server: the server host machine (DNS name) or IP addres\n" +
                    "tcp_port: the tcp port where the server is waiting\n" +
                    "movie: the requested movie\n" +
                    "udp_port: the udp_port where the client will receive the movie for real-time playing player_port: the udp_port of the player that will play the streamed movie");

            System.exit(0);
        }

        String remote = args[2] + ":" + args[5]; // receive mediastream from this rmote endpoint
        String destinations = args[2] + ":" + args[6]; // resend mediastream to this destination endpoint

        InetSocketAddress inSocketAddress = parseSocketAddress(remote);
        Set<SocketAddress> outSocketAddressSet = Arrays.stream(destinations.split(",")).map(s -> parseSocketAddress(s))
                .collect(Collectors.toSet());

        EncryptedSocket inSocket = null;
        if (inSocketAddress.getAddress().isMulticastAddress()) {
            inSocket = new EncryptedMulticastSocket(inSocketAddress);
        } else {
            inSocket = new EncryptedDatagramSocket(inSocketAddress);
        }

        int countframes = 0;
        DatagramSocket outSocket = new DatagramSocket();
        byte[] buffer = null;

        SHPClient.initConnection(
                "srscProject/src/main/resources/",
                args[2],
                Integer.parseInt(args[3]),
                Integer.parseInt(args[5]),
                args[0],
                args[1],
                args[4]
        );

        while (true) {
            buffer = new byte[4 * 1024];

            EncryptedDatagramPacket inPacket = new EncryptedDatagramPacket(buffer, buffer.length);
            inSocket.receive(inPacket);
            buffer = inPacket.getData();

            for (SocketAddress outSocketAddress : outSocketAddressSet) {
                outSocket.send(new DatagramPacket(buffer, buffer.length, outSocketAddress));
            }
        }
    }

    private static InetSocketAddress parseSocketAddress(String socketAddress) {
        String[] split = socketAddress.split(":");
        String host = split[0];
        int port = Integer.parseInt(split[1]);
        return new InetSocketAddress(host, port);
    }
}
