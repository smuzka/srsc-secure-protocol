import java.net.InetAddress;
import java.util.Date;

import DSTP.DSTP;
import DSTP.EncryptedDatagramPacket;
import DSTP.EncryptedMulticastSocket;

public class MulticastSender {

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.err.println("usage: java MulticastSender  grupo_multicast porto time-interval");
            System.exit(0);
        }

        int more = 20; // change if needed, send 20 time a MCAST message
        int port = Integer.parseInt(args[1]);
        InetAddress group = InetAddress.getByName(args[0]);
        int timeinterval = Integer.parseInt(args[2]);
        String msg;

        if (!group.isMulticastAddress()) {
            System.err.println("Multicast address required...");
            System.exit(0);
        }

        DSTP.init();
        EncryptedMulticastSocket ms = new EncryptedMulticastSocket();
        do {
            String msgsecret = "topcsecret message, sent on: ";

            String msgdate = new Date().toString();
            msg = msgsecret + msgdate;
            ms.send(new EncryptedDatagramPacket(msg, group, port));

            // --more; // Tirar o comentario se quizer mandar apenas "more" numero de vezes

            try {
                Thread.sleep(1000 * timeinterval);
            } catch (InterruptedException e) {
            }

        } while (more > 0);
        msg = "fim!";
        ms.send(new EncryptedDatagramPacket(msg, group, port));
        ms.close();

    }
}
