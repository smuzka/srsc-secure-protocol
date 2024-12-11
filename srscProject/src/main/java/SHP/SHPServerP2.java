package SHP;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.Security;
import java.util.Arrays;

import javax.crypto.KeyAgreement;
import javax.crypto.spec.DHParameterSpec;

import DSTP.DSTP;
import SHP.messages.MessageType1;
import SHP.messages.MessageType2;
import SHP.messages.MessageType3P2;
import SHP.messages.MessageType4P2;
import SHP.messages.MessageType5;

public class SHPServerP2 {

    public static ConnectionResult initConnection(String filesPath, int serverPort) {
        Security.addProvider(new BouncyCastleProvider());
        User.setFilePath(filesPath);

        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            System.out.println("Server is running...");

            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected: " + clientSocket.getInetAddress());

            return handleClient(clientSocket);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static ConnectionResult handleClient(Socket clientSocket) {
        try (DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())) {

            MessageType1 messageType1 = new MessageType1();
            messageType1.receive(in);

            MessageType2 messageType2 = new MessageType2(
                    Util.createNonce(),
                    Util.createNonce(),
                    Util.createNonce());
            messageType2.send(out);

            MessageType3P2 messageType3 = new MessageType3P2(
                    messageType2.getNonce1(),
                    messageType2.getNonce2(),
                    messageType2.getNonce3(),
                    new String(messageType1.getUserId()));

            messageType3.receive(in);
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
            KeyAgreement sKeyAgree = KeyAgreement.getInstance("DH", "BC");
            KeyPair sPair = keyGen.generateKeyPair();
            sKeyAgree.init(sPair.getPrivate());
            sKeyAgree.doPhase(Util.getPublicKeyFromBytes(messageType3.getYdhClient()), true);
            byte[] sShared = hash.digest(sKeyAgree.generateSecret());
            System.out.println("Server generated\n" +
                    Arrays.toString(sShared));

            String cryptoConfig = ReadFile.readFileContent("srscProject/src/main/resources/ciphersuite.conf");
            byte[] symetricKey = Arrays.copyOfRange(sShared, 0, 16);
            byte[] macKey = Arrays.copyOfRange(sShared, 16, 32);
            String symetricKeyHex = Util.toHex(symetricKey, 16);
            String macKeyHex = Util.toHex(symetricKey, 16);

            System.out.println("Symetric key: " + symetricKeyHex);
            System.out.println("MAC key: " + macKeyHex);
            MessageType4P2 messageType4 = new MessageType4P2(
                    Util.incrementNonce(messageType3.getNonce4()),
                    Util.createNonce(), // nonce5
                    new String(messageType1.getUserId()),
                    "ToDo - change confirmation request",
                    cryptoConfig,
                    sPair.getPublic().getEncoded());
            messageType4.send(out);

            DSTP.init(cryptoConfig);

            MessageType5 messageType5 = new MessageType5(
                    new String(messageType1.getUserId()),
                    messageType4.getNonce5());
            messageType5.receive(in);

            return new ConnectionResult(
                    messageType3.getUdpPort(),
                    messageType3.getRequest());

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Client disconnected.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

}
