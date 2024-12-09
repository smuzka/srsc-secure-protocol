import java.io.FileWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

// How to run:
// javac -cp .:bcprov-jdk18on-1.78.1.jar Tools.java
// java -cp .:bcprov-jdk18on-1.78.1.jar Tools

public class Tools {

    public static void main(String[] args) {
        generateUsers();
        generateServerKeys();
    }

    public static void generateUsers() {
        Security.addProvider(new BouncyCastleProvider());

        try {
//          For generating key pair
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("ECDSA", "BC");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256k1");
            kpg.initialize(ecSpec, new SecureRandom());

//          To create password hash
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            try (FileWriter userDatabaseWriter = new FileWriter("userDatabase.txt")) {
                try (FileWriter eccKeyPairsWriter = new FileWriter("ClientEccKeyPair.sec")) {
                    eccKeyPairsWriter.append("Curve,PrivateKey,PublicKey\n");
                    userDatabaseWriter.append("UserId,H(Password),Salt,KpubClient\n");

                    for (int i = 0; i < 10; i++) {
                        KeyPair keyPair = kpg.generateKeyPair();

                        eccKeyPairsWriter.append("secp256k1,");

                        String privateKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
                        String publicKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());

                        eccKeyPairsWriter.append(privateKeyBase64);
                        eccKeyPairsWriter.append(",");
                        eccKeyPairsWriter.append(publicKeyBase64);
                        eccKeyPairsWriter.append("\n");



                        userDatabaseWriter.append("user").append(String.valueOf(i)).append(",");

                        byte[] passwordHash = digest.digest(("Password!" + i).getBytes());
                        userDatabaseWriter.append(Base64.getEncoder().encodeToString(passwordHash)).append(",");

                        byte[] salt = createNonce();
                        userDatabaseWriter.append(Base64.getEncoder().encodeToString(salt)).append(",");

                        userDatabaseWriter.append(publicKeyBase64).append("\n");
                    }
                }

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void generateServerKeys() {
        Security.addProvider(new BouncyCastleProvider());

        try {
//          For generating key pair
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("ECDSA", "BC");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256k1");
            kpg.initialize(ecSpec, new SecureRandom());

            try (FileWriter eccKeyPairsWriter = new FileWriter("ServerEccKeyPair.sec")) {
                eccKeyPairsWriter.append("Curve,PrivateKey,PublicKey\n");

                for (int i = 0; i < 10; i++) {
                    KeyPair keyPair = kpg.generateKeyPair();

                    eccKeyPairsWriter.append("secp256k1,");

                    String privateKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
                    String publicKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());

                    eccKeyPairsWriter.append(privateKeyBase64);
                    eccKeyPairsWriter.append(",");
                    eccKeyPairsWriter.append(publicKeyBase64);
                    eccKeyPairsWriter.append("\n");
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

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

