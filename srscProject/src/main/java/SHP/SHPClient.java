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
            String userId,
            String userPassword) {
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
                    // ToDo
                    "ToDo - change request",
                    userId,
                    Util.intToBytes(Util.bytesToInt(messageType2.getNonce3()) + 1),
                    Util.createNonce(),
                    serverPort);
            messageType3.send(out);

            MessageType4 messageType4 = new MessageType4(userPassword, userId, messageType3.getNonce4());
            messageType4.receive(in);

            // ToDo Change configData to be read from previous message
            DSTP.init(
                    "CONFIDENTIALITY: AES/CBC/PKCS5Padding\n" +
                            "SYMMETRIC_KEY: 2b7e151628aed2a6abf7158809cf4f3c\n" +
                            "SYMMETRIC_KEY_SIZE: 128\n" +
                            "IV Size: 16\n" +
                            "INTEGRITY: H\n" +
                            "H: SHA-256\n" +
                            "MAC: HMacSHA3-512\n" +
                            "MACKEY: 1f1e1d1c1b1a19181716151413121111\n" +
                            "MACKEY_SIZE: 128");

            MessageType5 messageType5 = new MessageType5(
                    Util.intToBytes(Util.bytesToInt(messageType4.getNonce5()) + 1),
                    userPassword);

            messageType5.send(out);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}