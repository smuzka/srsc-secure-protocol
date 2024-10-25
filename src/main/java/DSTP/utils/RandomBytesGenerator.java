package DSTP.utils;

import java.security.SecureRandom;

public class RandomBytesGenerator {
    static public byte[] getRandomBytes(int length) {
        byte[] randomBytes = new byte[length];

        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(randomBytes);

        return randomBytes;
    }

    static public byte[] getRandomBytes(String length) {
        int len = Integer.parseInt(length.trim());
        byte[] randomBytes = new byte[len];

        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(randomBytes);

        return randomBytes;
    }

}
