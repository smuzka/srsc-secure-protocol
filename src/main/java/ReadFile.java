package srscProject.src.main.java;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ReadFile {
    public static Map<String, String> getVariables(String[] args) {
        // Path to the file
        String filePath = "srscProject/src/main/resources/cryptoconfig.txt";

        // HashMap to store the variables and their values
        Map<String, String> variables = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;

            // Read each line of the file
            while ((line = br.readLine()) != null) {
                // Split the line into two parts: variable name and value
                String[] parts = line.split(":");

                // Ensure that there are exactly two parts after splitting
                if (parts.length == 2) {
                    String variableName = parts[0].trim(); // Trim any extra spaces
                    String value = parts[1].trim();        // Trim any extra spaces

                    // Add to the map
                    variables.put(variableName, value);
                } else {
                    System.out.println("Skipping invalid line: " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Configuration:");
        // Output the variables and values
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }

        return variables;
    }
}