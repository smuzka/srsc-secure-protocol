package SHP.messages;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.ByteBuffer;
import java.security.Key;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import SHP.Util;

public class MessageType3 extends Message {
    private final String ALGORITHM = "PBEWITHSHA256AND192BITAES-CBC-BC";
    private final String PROVIDER = "BC";

    private String password;
    private byte[] salt;
    private byte[] counter;

    private String request;
    private String userId;
    private byte[] nonce1;
    private byte[] nonce2;
    private int udpPort;


    public MessageType3(byte[] salt, byte[] counter, String userId) {
        this.salt = salt;
        this.counter = counter;
        this.userId = userId;
    }

    public MessageType3(String password, byte[] salt, byte[] counter, String request, String userId, byte[] nonce1, byte[] nonce2, int udpPort) {
        this.password = password;
        this.salt = salt;
        this.counter = counter;
        this.request = request;
        this.userId = userId;
        this.nonce1 = nonce1;
        this.nonce2 = nonce2;
        this.udpPort = udpPort;
    }

    @Override
    public byte[] toByteArray() {

        byte[] requestSerialized = Util.serializeString(request);
        byte[] userIdSerialized = Util.serializeString(userId);
        byte[] nonce1Serialized = Util.serializeBytes(nonce1);
        byte[] nonce2Serialized = Util.serializeBytes(nonce2);
        byte[] udpPortSerialized = Util.serializeInt(udpPort);

        byte[] payload = new byte[requestSerialized.length + userIdSerialized.length + nonce1Serialized.length + nonce2Serialized.length + udpPortSerialized.length];

        System.arraycopy(requestSerialized, 0, payload, 0, requestSerialized.length);
        System.arraycopy(userIdSerialized, 0, payload, requestSerialized.length, userIdSerialized.length);
        System.arraycopy(nonce1Serialized, 0, payload, requestSerialized.length + userIdSerialized.length, nonce1Serialized.length);
        System.arraycopy(nonce2Serialized, 0, payload, requestSerialized.length + userIdSerialized.length + nonce1Serialized.length, nonce2Serialized.length);
        System.arraycopy(udpPortSerialized, 0, payload, requestSerialized.length + userIdSerialized.length + nonce1Serialized.length + nonce2Serialized.length, udpPortSerialized.length);

        return PBEEncrypt(payload);
    }

    @Override
    public void fromByteArray(byte[] data) {
        data = PBEDecrypt(data);

        int intSizeInBytes = 4;

        byte[] requestDeserialized = Util.deserializeFirstStringInArray(data);
        this.request = new String(requestDeserialized);

        data = Arrays.copyOfRange(data, requestDeserialized.length + intSizeInBytes, data.length);
        byte[] userIdDeserialized = Util.deserializeFirstStringInArray(data);
        this.userId = new String(userIdDeserialized);

        data = Arrays.copyOfRange(data, userIdDeserialized.length + intSizeInBytes, data.length);
        byte[] nonce1Deserialized = Util.deserializeFirstBytesInArray(data);
        this.nonce1 = nonce1Deserialized;

        data = Arrays.copyOfRange(data, nonce1Deserialized.length + intSizeInBytes, data.length);
        byte[] nonce2Deserialized = Util.deserializeFirstBytesInArray(data);
        this.nonce2 = nonce2Deserialized;

        data = Arrays.copyOfRange(data, nonce2Deserialized.length + intSizeInBytes, data.length);
        byte[] udpPortDeserialized = Util.deserializeFirstIntInArray(data);
        this.udpPort = ByteBuffer.wrap(udpPortDeserialized).getInt();
    }

    @Override
    public MessageType getType() {
        return MessageType.TYPE3;
    }

    @Override
    public String toString() {
        return "MessageType3: " + "request=" + request + ", userId=" + userId + ", nonce1=" + Arrays.toString(nonce1) + ", nonce2=" + Arrays.toString(nonce2) + ", udpPort=" + udpPort;
    }

    private byte[] PBEEncrypt(byte[] data) {
        char[] password;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] passwordSHA256 = digest.digest(this.password.getBytes());
            password = Base64.getEncoder().encodeToString(passwordSHA256).toCharArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        byte[] salt = this.salt;
        int counter = ByteBuffer.wrap(this.counter).getInt();

        System.out.println("============= PBEEncrypt =============");
        System.out.println("password: " + new String(password));
        System.out.println("salt: " + Arrays.toString(salt));
        System.out.println("counter: " + counter);

        try {
            PBEKeySpec pbeSpec = new PBEKeySpec(password);
            SecretKeyFactory keyFact = SecretKeyFactory.getInstance(ALGORITHM, PROVIDER);
            Key sKey = keyFact.generateSecret(pbeSpec);

            Cipher cEnc = Cipher.getInstance(ALGORITHM, PROVIDER);
            cEnc.init(Cipher.ENCRYPT_MODE, sKey, new PBEParameterSpec(salt, counter));

            return cEnc.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] PBEDecrypt(byte[] data) {
        String passwordFromFile = getUserPasswordFromFile(this.userId);
        if (passwordFromFile == null) {
            throw new RuntimeException("User not found");
        }
        char[] password = passwordFromFile.toCharArray();
        byte[] salt = this.salt;
        int counter = ByteBuffer.wrap(this.counter).getInt();

        System.out.println("============= PBEDecrypt =============");
        System.out.println("password: " + new String(password));
        System.out.println("salt: " + Arrays.toString(salt));
        System.out.println("counter: " + counter);

        try {
            PBEKeySpec pbeSpec = new PBEKeySpec(password);
            SecretKeyFactory keyFact = SecretKeyFactory.getInstance(ALGORITHM, PROVIDER);
            Key sKey = keyFact.generateSecret(pbeSpec);

            Cipher cDec = Cipher.getInstance(ALGORITHM, PROVIDER);
            cDec.init(Cipher.DECRYPT_MODE, sKey, new PBEParameterSpec(salt, counter));

            return cDec.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//    public void testPBE(byte[] test) {
//        System.out.println("============= testPBE =============");
//        System.out.println("Before: " + Arrays.toString(test));
//
//        byte[] encrypted = PBEEncrypt(test);
//        byte[] decrypted = PBEDecrypt(encrypted);
//
//        System.out.println("Middle: " + Arrays.toString(encrypted));
//        System.out.println("After: " + Arrays.toString(decrypted));
//        System.out.println("===================================");
//    }

    private String getUserPasswordFromFile(String userId) {

        String filePath = "srscProject/src/main/resources/userDatabase.txt";

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("UserId,")) {
                    continue;
                }

                String[] columns = line.split(",");

                if (columns[0].equals(userId)) {
                    return columns[1];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
