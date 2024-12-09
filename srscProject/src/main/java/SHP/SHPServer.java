package SHP;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import SHP.messages.Message;
import SHP.messages.MessageType1;
import SHP.messages.MessageType2;

public class SHPServer {

    public static void initConnection(int serverPort) {
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

            Message messageToReceive = new MessageType1();
            messageToReceive.receive(in);
            System.out.println("Received MessageType1: " + messageToReceive);

            Message messageToSend = new MessageType2(
                    util.createNonce(),
                    util.createNonce(),
                    util.createNonce()
            );
            messageToSend.send(out);
            System.out.println("Sent MessageType2: " + messageToReceive);


        } catch (IOException e) {
            System.out.println("Client disconnected.");
        }
    }

}
