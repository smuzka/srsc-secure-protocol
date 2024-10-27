package DSTP;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import DSTP.utils.RandomBytesGenerator;
import DSTP.utils.ReadFile;
import DSTP.utils.ToHex;
import DSTP.utils.DSTPHeader;

public class DSTP {
    static Cipher cipher;
    static SecretKeySpec key;
    static IvParameterSpec ivSpec;
    static byte[] ivBytes;
    static MessageDigest digest;
    static boolean integrityModeHash; // true for hash, false for mac

    public static void init()
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
            InvalidKeyException, ShortBufferException, IllegalBlockSizeException, BadPaddingException {

        Map<String, String> variables = ReadFile.getVariables(new String[0]);

        String confidentiality = variables.get("CONFIDENTIALITY");
        String[] confidentialityParts = confidentiality.split("/");

        // Generate Key
        byte[] keyBytes = RandomBytesGenerator.getRandomBytes(variables.get("SYMMETRIC_KEY_SIZE"));
        key = new SecretKeySpec(keyBytes, confidentialityParts[0]);
        String configSymmetricKey = variables.get("SYMMETRIC_KEY");
        if (configSymmetricKey != null) {
            key = new SecretKeySpec(configSymmetricKey.getBytes(StandardCharsets.UTF_8), confidentialityParts[0]);
        }

        // Generate IV
        String configIVSize = variables.get("IV Size");
        ivBytes = new byte[Integer.parseInt(configIVSize)];
        ivSpec = new IvParameterSpec(ivBytes);
        String configIV = variables.get("IV");
        if (configIV != null) {
            ivSpec = new IvParameterSpec(configIV.getBytes(StandardCharsets.UTF_8));
        }

        cipher = Cipher.getInstance(confidentiality);

        // Select Hash or MAC mode
        String configIntegrityMode = variables.get("INTEGRITY");
        if (configIntegrityMode.equals("H")) {
            // Select the hash algorithm
            integrityModeHash = true;
            String configHAlg = variables.get("H");
            digest = MessageDigest.getInstance(configHAlg);
            System.out.println("MAC mode selected");
        } else if (configIntegrityMode.equals("HMAC")) {
            integrityModeHash = false;

            System.out.println("HMAC mode selected");

            // String configMACAlg = variables.get("MAC");
            // String configMACKey = variables.get("MACKEY");
            // String configMACKeySize = variables.get("MACKEY_SIZE");
            // digest = MessageDigest.getInstance(configHAlg);
        } else {
            System.out.println("Integrity mode not defined");
            System.exit(0);
        }

    }

    public static String encryptString(String plainText)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
            InvalidKeyException, ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        String inputWithH = plainText;
        // calculate hash or hmac
        if (integrityModeHash) {
            // hash
            byte[] hashByte = digest.digest(plainText.substring(4).getBytes());
            String hashString = ToHex.toHex(hashByte, hashByte.length).toString();
            System.out.println("hashString: " + hashString);
            inputWithH = plainText + hashString;

        } else {
            // hmac
        }
        // encryption
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        byte[] inputBytes = inputWithH.getBytes();
        System.out.println("Input: " + "'" + plainText + "'");
        byte[] cipherText = new byte[cipher.getOutputSize(ivBytes.length + inputBytes.length)];
        int ctLength = cipher.update(ivBytes, 0, ivBytes.length, cipherText, 0);
        ctLength += cipher.update(inputBytes, 0, inputBytes.length, cipherText, ctLength);
        ctLength += cipher.doFinal(cipherText, ctLength);

        // Create header
        DSTPHeader header = new DSTPHeader((short) 2, (byte) 1, (short) ctLength);
        System.out.println("header: " + header.encode() + " bytes: " + 5);
        System.out.println("header decc: " + header.toString() + " bytes: " + ctLength);

        System.out.println("ciphera: " + ToHex.toHex(cipherText, ctLength) + " bytes: " + ctLength);
        return header.encode() + ToHex.toHex(cipherText, ctLength).toString();
    }

    public static String decryptString(String cipherTextHex)
            throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException,
            InvalidKeyException, ShortBufferException, IllegalBlockSizeException,
            BadPaddingException {

        // Decode the header
        DSTPHeader header = DSTPHeader.fromEncodedHeader(cipherTextHex.substring(0, 10));
        System.out.println("decoded header: " + header.toString());

        // Convert hex string to byte array
        byte[] cipherText = ToHex.fromHex(cipherTextHex.substring(10, cipherTextHex.length()));
        // decryption
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
        byte[] buf = new byte[cipher.getOutputSize(cipherText.length)];
        int bufLength = cipher.update(cipherText, 0, cipherText.length, buf, 0);
        bufLength += cipher.doFinal(buf, bufLength);
        // remove the iv from the start of the message
        byte[] plainText = new byte[bufLength - ivBytes.length];
        System.arraycopy(buf, ivBytes.length, plainText, 0, plainText.length);
        String plainTextString = new String(plainText, StandardCharsets.UTF_8);
        // check integrity
        if (integrityModeHash) {
            // Remove the sequence number from the start
            String plainTextStringNoSeq = plainTextString.substring(4);

            int messageLength = plainTextStringNoSeq.length() - (digest.getDigestLength() * 2); // Remove hash length

            // Extract the message and hash parts
            String message = plainTextStringNoSeq.substring(0, messageLength);
            String receivedHashString = plainTextStringNoSeq.substring(messageLength);
            // System.out.println("message: " + "'" + message + "'");
            // System.out.println("received hash: " + receivedHashString);

            // Verify by comparing the recalculated hash with the received hash
            byte[] hashByte = digest.digest(message.getBytes());
            String hashString = ToHex.toHex(hashByte, hashByte.length).toString();
            // System.out.println("calculated hash: " + hashString);

            boolean isVerified = receivedHashString.equals(hashString);
            if (isVerified) {
                System.out.println("Is integrity verified: \033[1;32m" + isVerified + "\033[0m"); // Green
            } else {
                System.out.println("Is integrity verified: \033[1;31m" + isVerified + "\033[0m"); // Red
            }
            plainTextString = plainTextString.substring(0, messageLength + 4); // remove hash
            // from end of the message
        } else {
            // hmac
        }

        System.out.println("plain : " + plainTextString + " bytes: " + plainText.length);
        return plainTextString;

    }

    public static void printMessage()
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
            InvalidKeyException, ShortBufferException, IllegalBlockSizeException, BadPaddingException {

        Map<String, String> variables = ReadFile.getVariables(new String[0]);

        String confidentiality = variables.get("CONFIDENTIALITY");
        String[] confidentialityParts = confidentiality.split("/");

        // Generate Key
        byte[] keyBytes = RandomBytesGenerator.getRandomBytes(variables.get("SYMMETRIC_KEY_SIZE"));
        SecretKeySpec key = new SecretKeySpec(keyBytes, confidentialityParts[0]);
        String configSymmetricKey = variables.get("SYMMETRIC_KEY");
        if (configSymmetricKey != null) {
            key = new SecretKeySpec(configSymmetricKey.getBytes(StandardCharsets.UTF_8), confidentialityParts[0]);
        }

        // Generate IV
        String configIVSize = variables.get("IV Size");
        byte[] ivBytes = new byte[Integer.parseInt(configIVSize)];
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        String configIV = variables.get("IV");
        if (configIV != null) {
            ivSpec = new IvParameterSpec(configIV.getBytes(StandardCharsets.UTF_8));
        }

        Cipher cipher = Cipher.getInstance(confidentiality);

        // encryption
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        byte[] input = "1234567890".getBytes(StandardCharsets.UTF_8);
        System.out.println("Input: 1234567890");
        byte[] cipherText = new byte[cipher.getOutputSize(ivBytes.length + input.length)];
        int ctLength = cipher.update(ivBytes, 0, ivBytes.length, cipherText, 0);
        ctLength += cipher.update(input, 0, input.length, cipherText, ctLength);
        ctLength += cipher.doFinal(cipherText, ctLength);
        System.out.println("ciphera: " + ToHex.toHex(cipherText, ctLength) + " bytes: " + ctLength);

        // decryption
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
        byte[] buf = new byte[cipher.getOutputSize(ctLength)];
        int bufLength = cipher.update(cipherText, 0, ctLength, buf, 0);
        bufLength += cipher.doFinal(buf, bufLength);
        // remove the iv from the start of the message
        byte[] plainText = new byte[bufLength - ivBytes.length];
        System.arraycopy(buf, ivBytes.length, plainText, 0, plainText.length);
        System.out.println("plain : " + new String(plainText, StandardCharsets.UTF_8) + " bytes: " + plainText.length);

    }
}
