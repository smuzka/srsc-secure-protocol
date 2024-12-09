package SHP;


import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class ECDSADigitalSignature {
    private final static String ALGORITHM = "SHA256withECDSA";
    private final static String PROVIDER = "BC";


    public static byte[] sign(byte[] data, String privateKeyString) {
        try {
            Signature signature = Signature.getInstance(ALGORITHM, PROVIDER);
            signature.initSign(stringToPrivateKey(privateKeyString), new SecureRandom());
            signature.update(data);
            return signature.sign();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean verifySignature(byte[] signatureBytes, byte[] data, String publicKeyString) {
        try {
            Signature signature = Signature.getInstance(ALGORITHM, PROVIDER);
            signature.initVerify(stringToPublicKey(publicKeyString));
            signature.update(data);

            return signature.verify(signatureBytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static PrivateKey stringToPrivateKey(String privateKeyString) {
        try {
            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyString);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("ECDSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static PublicKey stringToPublicKey(String publicKeyString) {
        try {
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyString);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("ECDSA");
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
