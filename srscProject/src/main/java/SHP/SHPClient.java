package SHP;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.security.Security;

import DSTP.DSTP;
import SHP.messages.MessageType1;
import SHP.messages.MessageType2;
import SHP.messages.MessageType3;
import SHP.messages.MessageType4;
import SHP.messages.MessageType5;

public class SHPClient {

    public static void initConnection(
            String filesPath,
            String serverAddress,
            int serverPort,
            int udpPort,
            String userId,
            String userPassword,
            String request) {
        Security.addProvider(new BouncyCastleProvider());
        User.setFilePath(filesPath);

        try (Socket socket = new Socket(serverAddress, serverPort);
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                DataInputStream in = new DataInputStream(socket.getInputStream())) {

            MessageType1 messageType1 = new MessageType1(userId.getBytes());
            messageType1.send(out);

            MessageType2 messageType2 = new MessageType2();
            messageType2.receive(in);

            MessageType3 messageType3 = new MessageType3(
                    userPassword,
                    messageType2.getNonce1(),
                    messageType2.getNonce2(),
                    request,
                    userId,
                    Util.incrementNonce(messageType2.getNonce3()),
                    Util.createNonce(),
                    udpPort);
            messageType3.send(out);

            MessageType4 messageType4 = new MessageType4(userPassword, userId, messageType3.getNonce4());
            messageType4.receive(in);

            DSTP.init(messageType4.getCryptoConfig());

            MessageType5 messageType5 = new MessageType5(
                    Util.incrementNonce(messageType4.getNonce5()),
                    userPassword);

            messageType5.send(out);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}