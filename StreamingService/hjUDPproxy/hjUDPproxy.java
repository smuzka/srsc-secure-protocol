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

class hjUDPproxy {
    public static void main(String[] args) throws Exception {
if (args.length != 2)
        {
        System.out.println("Use: hjUDPproxy <endpoint1> <endpoint2>");
        System.out.println("<endpoint1>: endpoint for receiving stream");
        System.out.println("<endpoint2>: endpoint of media player");
	
	System.out.println("Ex: hjUDPproxy 224.2.2.2:9000  127.0.0.1:8888");
	System.out.println("Ex: hjUDPproxy 127.0.0.1:10000 127.0.0.1:8888");
	System.exit(0);
	}
	
	String remote=args[0]; // receive mediastream from this rmote endpoint
	String destinations=args[1]; //resend mediastream to this destination endpoint	
	    

        InetSocketAddress inSocketAddress = parseSocketAddress(remote);
        Set<SocketAddress> outSocketAddressSet = Arrays.stream(destinations.split(",")).map(s -> parseSocketAddress(s)).collect(Collectors.toSet());


        EncryptedSocket inSocket = null;
        if(inSocketAddress.getAddress().isMulticastAddress()){
            inSocket = new EncryptedMulticastSocket(inSocketAddress);
        } else {
            inSocket = new EncryptedDatagramSocket(inSocketAddress);
        }

        int countframes=0;
        DatagramSocket outSocket = new DatagramSocket();
        byte[] buffer = new byte[4 * 1024];
        while (true) {

            EncryptedDatagramPacket inPacket = new EncryptedDatagramPacket(buffer, buffer.length);
            if(inSocketAddress.getAddress().isMulticastAddress()){
                inSocket.receive(inPacket);
                buffer = inPacket.getData();
            } else {
                inSocket.receive(inPacket);
                buffer = inPacket.getData();
            }


            for (SocketAddress outSocketAddress : outSocketAddressSet)
            {
                outSocket.send(new DatagramPacket(buffer, buffer.length, outSocketAddress));
            }
    }
}

    private static InetSocketAddress parseSocketAddress(String socketAddress) 
    {
        String[] split = socketAddress.split(":");
        String host = split[0];
        int port = Integer.parseInt(split[1]);
        return new InetSocketAddress(host, port);
    }
}
