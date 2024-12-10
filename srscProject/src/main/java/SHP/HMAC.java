package SHP;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HMAC {
    private static String hmacAlgorithm = "HMacSHA3-512";

    public static int getHMACLength() {
        try {
            return Mac.getInstance(hmacAlgorithm).getMacLength();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Selected hmac algorithm not found", e);
        }
    }

    public static void verifyHMAC(byte[] hMac, byte[] data, byte[] passwordHash) {
        byte[] generatedHMAC = generateHMAC(data, passwordHash);
        if (!Arrays.equals(hMac, generatedHMAC)) {
            throw new RuntimeException("HMAC verification failed");
        }
    }

    public static byte[] generateHMAC(byte[] data, byte[] passwordHash) {
        Mac hMac;
        try {
            hMac = Mac.getInstance(hmacAlgorithm);
            Key hMacKey = new SecretKeySpec(passwordHash, hmacAlgorithm);
            hMac.init(hMacKey);
            hMac.update(data);
            return hMac.doFinal();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Selected hmac algorithm not found", e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException("Invalid key for hmac algorithm", e);
        }
    }

}