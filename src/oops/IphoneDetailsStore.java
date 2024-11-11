package oops;

import javax.swing.*;
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
        // Create an instance of Iphone class instead of JFileChooser
    	IphoneDetailsStore iphone = new IphoneDetailsStore();
        
        // Use JFileChooser to let the user select a file
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select the iPhone details file");
        int result = fileChooser.showOpenDialog(null);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String filePath = selectedFile.getAbsolutePath();
            
            // Parse the file and load the data using the Iphone instance
            iphone.parseFile(filePath);
            
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("Enter the iPhone model to search (or type 'exit' to stop): ");
                String modelName = scanner.nextLine();
                
                // Check if user wants to exit
                if (modelName.equalsIgnoreCase("exit")) {
                    System.out.println("Exiting the program.");
                    break;
                }
                
                // Display the details for the searched model using the Iphone instance
                String details = iphone.searchByModel(modelName);
                System.out.println(details);
                
                // Prompt the user again if the model was not found
                if (details.equals("Model not found")) {
                    System.out.println("The model you entered was not found. Please try another model or type 'exit' to stop.");
                }
            }
            scanner.close();
        } else {
            System.out.println("No file selected. Exiting the program.");
        }
    }
}