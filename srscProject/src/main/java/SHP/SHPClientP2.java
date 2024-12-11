package SHP;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.util.Arrays;

import DSTP.DSTP;
import SHP.messages.MessageType1;
import SHP.messages.MessageType2;
import SHP.messages.MessageType3P2;
import SHP.messages.MessageType4P2;
import SHP.messages.MessageType5;

import java.security.*;

import javax.crypto.KeyAgreement;
import javax.crypto.spec.DHParameterSpec;

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

            // Parametro para o gerador do Grupo de Cobertura de P
            BigInteger g512 = new BigInteger(
                    "153d5d6172adb43045b68ae8e1de1070b6137005686d29d3d73a7"
                            + "749199681ee5b212c9b96bfdcfa5b20cd5e3fd2044895d609cf9b"
                            + "410b7a0f12ca1cb9a428cc",
                    16);
            // Um grande numero primo P
            BigInteger p512 = new BigInteger(
                    "9494fec095f3b85ee286542b3836fc81a5dd0a0349b4c239dd387"
                            + "44d488cf8e31db8bcb7d33b41abb9e5a33cca9144b1cef332c94b"
                            + "f0573bf047a3aca98cdf3b",
                    16);
            DHParameterSpec dhParams = new DHParameterSpec(p512, g512);

            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DH", "BC");
            // keyGen.initialize(2048);
            keyGen.initialize(dhParams);
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

            MessageType4P2 messageType4 = new MessageType4P2(userPassword, userId, messageType3.getNonce4());
            messageType4.receive(in);

            cKeyAgree.doPhase(Util.getPublicKeyFromBytes(messageType4.getYdhServer()), true);
            byte[] cShared = hash.digest(cKeyAgree.generateSecret());
            System.out.println("Client generated\n" +
                    Arrays.toString(cShared));

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