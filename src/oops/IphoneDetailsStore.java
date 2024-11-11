package oops;

import java.io.*;
import java.util.*;

public class IphoneDetailsStore {

    // Map to store the model name as the key and details as the value
    private Map<String, String> iPhoneDetails = new HashMap<>();

    // Method to parse the file and store model details
    public void parseFile(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            String currentModel = null;
            String details = "";

            while ((line = br.readLine()) != null) {
                if (line.startsWith("Extracted Data from:")) {
                    // Store the previous model's details before starting a new one
                    if (currentModel != null) {
                        iPhoneDetails.put(currentModel, details.trim());
                    }
                    // Extract model name and reset details for the new model
                    currentModel = line.substring(line.lastIndexOf('/') + 1).replace('_', ' ');
                    details = "";
                } else if (currentModel != null) {
                    // Accumulate details for the current model
                    details += line + "\n";
                }
            }

            // Store the last model's details
            if (currentModel != null) {
                iPhoneDetails.put(currentModel, details.trim());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to search for details by model name
    public String searchByModel(String modelName) {
        return iPhoneDetails.getOrDefault(modelName, "Model not found");
    }

    public static void main(String[] args) {
        IphoneDetailsStore store = new IphoneDetailsStore();
        String filePath = "Output_DFS.txt"; // Replace with the actual file path

        // Parse the file and load the data
        store.parseFile(filePath);

        // Search for a specific iPhone model
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the iPhone model to search: ");
        String modelName = scanner.nextLine();

        // Display the details for the searched model
        System.out.println(store.searchByModel(modelName));
        
        scanner.close();
    }
}
