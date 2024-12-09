package SHP;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import SHP.messages.Message;
import SHP.messages.MessageType1;
import SHP.messages.MessageType2;

public class SHPClient {

    public static void initConnection(String serverAddress, int serverPort, byte[] userId) {

        try (Socket socket = new Socket(serverAddress, serverPort);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {

            Message messageToSend = new MessageType1(userId);
            messageToSend.send(out);
            System.out.println("Sent MessageType1: " + messageToSend);

            Message messageToReceive = new MessageType2();
            messageToReceive.receive(in);
            System.out.println("Received MessageType2: " + messageToReceive);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}