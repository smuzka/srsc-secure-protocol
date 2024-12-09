package SHP;

import java.io.BufferedReader;
import java.io.FileReader;

public class User {
    private final static String filePath = "srscProject/src/main/resources/userDatabase.txt";

    public static String getUserPasswordFromFile(String userId) {
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
