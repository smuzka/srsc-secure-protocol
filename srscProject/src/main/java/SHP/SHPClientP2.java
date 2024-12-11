package SHP;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.security.Security;

import DSTP.DSTP;
import SHP.messages.MessageType1;
import SHP.messages.MessageType2;
import SHP.messages.MessageType3P2;
import SHP.messages.MessageType4P2;
import SHP.messages.MessageType5;

import java.security.*;

import javax.crypto.KeyAgreement;

public class SHPClientP2 {

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

            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DH", "BC");
            keyGen.initialize(2048);
            MessageDigest hash = MessageDigest.getInstance("SHA256", "BC");
            KeyAgreement cKeyAgree = KeyAgreement.getInstance("DH", "BC");
            KeyPair cPair = keyGen.generateKeyPair();
            cKeyAgree.init(cPair.getPrivate());

            MessageType3P2 messageType3 = new MessageType3P2(
                    userPassword,
                    messageType2.getNonce1(),
                    messageType2.getNonce2(),
                    request,
                    userId,
                    Util.incrementNonce(messageType2.getNonce3()),
                    Util.createNonce(),
                    udpPort,
                    cPair.getPublic().getEncoded());
            messageType3.send(out);

            // aKeyAgree.doPhase(bPair.getPublic(), true);
            // byte[] aShared = hash.digest(aKeyAgree.generateSecret());
            // System.out.println("Alice: I generated\n" +
            // Arrays.toString(aShared));
            MessageType4P2 messageType4 = new MessageType4P2(userPassword, userId, messageType3.getNonce4());
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