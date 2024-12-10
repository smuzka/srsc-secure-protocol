package SHP;

import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;

public class EEncryptor {
    private final static String ALGORITHM = "ECIES";
    private final static String PROVIDER = "BC";

    public static byte[] EEncrypt(byte[] data, String publicKeyString) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM, PROVIDER);
            PublicKey publicKey = Util.getPublicKeyFromString(publicKeyString);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] EDecrypt(byte[] data, String privateKeyString) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM, PROVIDER);
            PrivateKey privateKey = Util.getPrivateKeyFromString(privateKeyString);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return cipher.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
