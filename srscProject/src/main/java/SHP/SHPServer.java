package SHP;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Security;

import DSTP.DSTP;
import SHP.messages.MessageType1;
import SHP.messages.MessageType2;
import SHP.messages.MessageType3;
import SHP.messages.MessageType4;
import SHP.messages.MessageType5;

public class SHPServer {

    public static void initConnection(int serverPort) {
        Security.addProvider(new BouncyCastleProvider());

        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            System.out.println("Server is running...");

            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected: " + clientSocket.getInetAddress());

            boolean connectionResult = handleClient(clientSocket);

            if (connectionResult) {
                System.out.println("Connection successful.");
            } else {
                System.out.println("Connection failed.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean handleClient(Socket clientSocket) {
        try (DataInputStream in = new DataInputStream(clientSocket.getInputStream());
             DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())) {

            MessageType1 messageType1 = new MessageType1();
            messageType1.receive(in);

            MessageType2 messageType2 = new MessageType2(
                    Util.createNonce(),
                    Util.createNonce(),
                    Util.createNonce());
            messageType2.send(out);

            MessageType3 messageType3 = new MessageType3(
                    messageType2.getNonce1(),
                    messageType2.getNonce2(),
                    new String(messageType1.getUserId()));
            messageType3.receive(in);

            MessageType4 messageType4 = new MessageType4(
                    Util.intToBytes(Util.bytesToInt(messageType3.getNonce4()) + 1),
                    Util.createNonce(), // nonce5
                    new String(messageType1.getUserId()),
                    "ToDo - change confirmation request",
                    "cryptoconfig");
            messageType4.send(out);

//            ToDo: change configData to be read from a file
            DSTP.init(
                    "CONFIDENTIALITY: AES/CBC/PKCS5Padding\n" +
                            "SYMMETRIC_KEY: 2b7e151628aed2a6abf7158809cf4f3c\n" +
                            "SYMMETRIC_KEY_SIZE: 128\n" +
                            "IV Size: 16\n" +
                            "INTEGRITY: H\n" +
                            "H: SHA-256\n" +
                            "MAC: HMacSHA3-512\n" +
                            "MACKEY: 1f1e1d1c1b1a19181716151413121111\n" +
                            "MACKEY_SIZE: 128"
            );

            MessageType5 messageType5 = new MessageType5(
                    new String(messageType1.getUserId()),
                    messageType4.getNonce5()
            );
            messageType5.receive(in);

            return true;

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Client disconnected.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return false;
    }

}
