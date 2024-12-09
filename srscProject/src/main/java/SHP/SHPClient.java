package SHP;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.security.Security;

import SHP.messages.MessageType1;
import SHP.messages.MessageType2;
import SHP.messages.MessageType3;

public class SHPClient {

    public static void initConnection(
            String serverAddress,
            int serverPort,
            String userId,
            String userPassword
    ) {
        Security.addProvider(new BouncyCastleProvider());

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
//                    ToDo
                    "ToDo - change request",
                    userId,
                    messageType2.getNonce3(),
                    Util.createNonce(),
                    serverPort
            );
            messageType3.send(out);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}