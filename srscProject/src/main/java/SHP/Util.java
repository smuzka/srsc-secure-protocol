package SHP;

import java.security.SecureRandom;

public class Util {

    public static byte[] createNonce() {
        byte[] nonce = new byte[16];
        try {
            SecureRandom prng = SecureRandom.getInstance("SHA1PRNG");
            prng.nextBytes(nonce);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nonce;
    }
}
