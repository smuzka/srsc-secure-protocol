package SHP;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Security;

import SHP.messages.MessageType1;
import SHP.messages.MessageType2;
import SHP.messages.MessageType3;
import SHP.messages.MessageType4;

public class SHPServer {

    public static void initConnection(int serverPort) {
        Security.addProvider(new BouncyCastleProvider());

        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            System.out.println("Server is running...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) {
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
                    messageType3.getNonce4(),
                    Util.createNonce(), // nonce5
                    new String(messageType1.getUserId()),
                    "ToDo - change confirmation request",
                    "cryptoconfig");
            messageType4.send(out);
        } catch (IOException e) {
            System.out.println("Client disconnected.");
        }
    }

}
