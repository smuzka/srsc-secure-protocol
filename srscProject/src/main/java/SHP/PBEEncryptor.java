package SHP;

import java.security.Key;
import java.security.MessageDigest;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import static SHP.User.getUserPasswordFromFile;

public class PBEEncryptor {
    private final static String ALGORITHM = "PBEWITHSHA256AND192BITAES-CBC-BC";
    private final static String PROVIDER = "BC";


    public static byte[] PBEEncrypt(byte[] data, String passwordString, byte[] salt, byte[] counterBytes) {
        char[] password = getSHA256Password(passwordString);
        int counter = Util.bytesToInt(counterBytes);

        try {
            PBEKeySpec pbeSpec = new PBEKeySpec(password);
            SecretKeyFactory keyFact = SecretKeyFactory.getInstance(ALGORITHM, PROVIDER);
            Key sKey = keyFact.generateSecret(pbeSpec);

            Cipher cEnc = Cipher.getInstance(ALGORITHM, PROVIDER);
//            Sometimes it gets stuck and I don't know why. What I tried:
//            - running in different nodes
//            - adding -Djava.security.egd=file:/dev/urandom to ensure the secure random generator does not block
//            - adding bouncy castle as direct dependency
//            Good news is running program again solves the problem :')
            cEnc.init(Cipher.ENCRYPT_MODE, sKey, new PBEParameterSpec(salt, counter));

            return cEnc.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] PBEDecrypt(byte[] data, String userId, byte[] salt, byte[] counterBytes) {
        String passwordFromFile = getUserPasswordFromFile(userId);
        if (passwordFromFile == null) {
            throw new RuntimeException("User not found");
        }
        char[] password = passwordFromFile.toCharArray();
        int counter = Util.bytesToInt(counterBytes);

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

    private static char[] getSHA256Password(String passwordString) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] passwordSHA256 = digest.digest(passwordString.getBytes());
            return Base64.getEncoder().encodeToString(passwordSHA256).toCharArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
