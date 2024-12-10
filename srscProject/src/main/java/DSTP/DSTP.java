package DSTP;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import DSTP.utils.DSTPHeader;
import DSTP.utils.RandomBytesGenerator;
import DSTP.utils.ReadFile;
import DSTP.utils.ToHex;

import static DSTP.utils.ToHex.concatArrays;

public class DSTP {
    static Cipher cipher;
    static SecretKeySpec key;
    static IvParameterSpec ivSpec;
    static byte[] ivBytes;
    static MessageDigest digest;
    static boolean integrityModeHash; // true for hash, false for mac

    static Mac hMac;
    static Key hMacKey;

    public static void init(String configData)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
            InvalidKeyException, ShortBufferException, IllegalBlockSizeException, BadPaddingException {

        Map<String, String> variables = ReadFile.getVariables(configData);

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

            String configMACAlg = variables.get("MAC");
            String configMACKey = variables.get("MACKEY");
            int configMACKeySize = Integer.parseInt(variables.get("MACKEY_SIZE"));
            if (configMACKey.length() * 4 != configMACKeySize) {
                throw new IllegalArgumentException("MAC key size mismatch");
            }

            hMac = Mac.getInstance(configMACAlg);
            hMacKey = new SecretKeySpec(configMACKey.getBytes(StandardCharsets.UTF_8), configMACAlg);
        } else {
            System.out.println("Integrity mode not defined");
            System.exit(0);
        }
    }

    public static byte[] encryptBytes(byte[] plainText)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
            InvalidKeyException, ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        System.out.println(
                "plain text: " + new String(plainText, StandardCharsets.UTF_8) + " bytes: " + plainText.length);
        byte[] inputWithH = plainText;
        // calculate hash or hmac
        if (integrityModeHash) {
            // hash
            byte[] hashByte = digest.digest(Arrays.copyOfRange(plainText, 2, plainText.length));
            // String hashString = ToHex.toHex(hashByte, hashByte.length).toString();
            // System.out.println("hashString: " + hashString);
            inputWithH = concatArrays(plainText, hashByte);
            // System.out.println(
            // "hash: " + ToHex.toHex(hashByte, hashByte.length));

        } else {
            // hmac
            hMac.init(hMacKey);
            hMac.update(Arrays.copyOfRange(plainText, 2, plainText.length));
            byte[] hMacByte = hMac.doFinal();
            // String hMacString = ToHex.toHex(hMacByte, hMac.getMacLength()).toString();
            // System.out.println("hMacString: " + hMacString);
            inputWithH = concatArrays(plainText, hMacByte);
        }
        // encryption
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        byte[] inputBytes = inputWithH;
        byte[] cipherText = new byte[cipher.getOutputSize(ivBytes.length + inputBytes.length)];
        int ctLength = cipher.update(ivBytes, 0, ivBytes.length, cipherText, 0);
        ctLength += cipher.update(inputBytes, 0, inputBytes.length, cipherText, ctLength);
        ctLength += cipher.doFinal(cipherText, ctLength);

        // Create header
        DSTPHeader header = new DSTPHeader((short) 2, (byte) 1, (short) ctLength);
        // System.out.println("header: " + header.encode() + " bytes: " + 5);
        // System.out.println("header decc: " + header.toString() + " bytes: " +
        // ctLength);
        System.out.println("cipher: " + ToHex.toHex(cipherText, ctLength) + " bytes: " + ctLength);

        return concatArrays(header.encode(), cipherText);
    }

    public static byte[] decryptBytes(byte[] cipherTextHex)
            throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException,
            InvalidKeyException, ShortBufferException, IllegalBlockSizeException,
            BadPaddingException {

        // Decode the header
        DSTPHeader header = DSTPHeader.fromEncodedHeader(Arrays.copyOfRange(cipherTextHex, 0, 5));
        System.out.println("decoded header: " + header.toString());

        // Cut header off
        byte[] cipherText = Arrays.copyOfRange(cipherTextHex, 5, cipherTextHex.length);
        // decryption
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
        byte[] decryptedText = new byte[cipher.getOutputSize(cipherText.length)];
        int ptLength = cipher.update(cipherText, 0, cipherText.length, decryptedText, 0);
        ptLength += cipher.doFinal(decryptedText, ptLength);

        // System.out.println("plain : " + new String(decryptedText,
        // StandardCharsets.UTF_8) + " bytes: " + ptLength);
        // remove the iv from the start of the message
        byte[] plainText = new byte[ptLength - ivBytes.length];
        System.arraycopy(decryptedText, ivBytes.length, plainText, 0, plainText.length);

        // System.out.println("plain : " + new String(plainText, StandardCharsets.UTF_8)
        // + " bytes: " + plainText.length);

        // check integrity
        // Remove the sequence number from the start
        byte[] plainTextNoSeq = Arrays.copyOfRange(plainText, 2, plainText.length);
        boolean isVerified = false;
        if (integrityModeHash) {
            int messageLength = plainTextNoSeq.length - digest.getDigestLength(); // Remove hash length
            // System.out.println("messageLength: " + messageLength);

            // Extract the message and hash parts
            byte[] message = Arrays.copyOfRange(plainTextNoSeq, 0, messageLength);
            byte[] receivedHashString = Arrays.copyOfRange(
                    plainTextNoSeq, messageLength,
                    plainTextNoSeq.length);

            // System.out.println("message: " + "'" + new String(message,
            // StandardCharsets.UTF_8) + "'");
            // System.out.println("received hash: " + ToHex.toHex(receivedHashString,
            // receivedHashString.length));

            // Verify by comparing the recalculated hash with the received hash
            byte[] hashByte = digest.digest(message);
            // String hashString = ToHex.toHex(hashByte, hashByte.length).toString();
            // System.out.println("calculated hash: " + ToHex.toHex(hashByte,
            // hashByte.length));

            isVerified = MessageDigest.isEqual(receivedHashString, hashByte);
            plainText = Arrays.copyOfRange(plainText, 0, messageLength + 2); // remove hash
            // from end of the message
        } else {
            // hMac
            // Verify by comparing the recalculated hMac with the received hMac
            int messageLength = plainTextNoSeq.length - (hMac.getMacLength()); // Remove hMac length

            // Extract the message and hmac parts
            byte[] message = Arrays.copyOfRange(plainTextNoSeq, 0, messageLength);
            byte[] receivedhMacString = Arrays.copyOfRange(
                    plainTextNoSeq, messageLength,
                    plainTextNoSeq.length);
            // System.out.println("message: " + "'" + message + "'");
            // System.out.println("received hmac: " + receivedhMacString);

            // Verify by comparing the recalculated hmac with the received hmac
            hMac.init(hMacKey);
            hMac.update(message);
            byte[] hMacByte = hMac.doFinal();
            String hMacString = ToHex.toHex(hMacByte, hMac.getMacLength()).toString();
            System.out.println("calculated hmac: " + hMacString);

            isVerified = MessageDigest.isEqual(receivedhMacString, hMacByte);

            plainText = Arrays.copyOfRange(plainText, 0, messageLength + 2); // remove hmac
        }
        if (isVerified) {
            System.out.println(
                    "Integrity verified with " + (integrityModeHash ? "hash" : "hmac") + ": \033[1;32m" + isVerified
                            + "\033[0m"); // Green
        } else {
            System.out.println("Integrity verified with " + (integrityModeHash ? "hash" : "hmac") + ": \033[1;31m"
                    + isVerified + "\033[0m"); // Red
        }
        System.out.println("plain : " + new String(plainText, StandardCharsets.UTF_8) + " bytes: " + plainText.length);
        return plainText;

    }
}
