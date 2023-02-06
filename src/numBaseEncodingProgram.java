package src;
// This is a program that takes in a file of the users choosing and encrypts it using a number base system between 2 and 9 at random. Each encryption is also locked behind a password and can be retreived with this password for later use.

//imports
import java.io.*;
import java.util.*;
import java.security.*;

public class numBaseEncodingProgram {

    // Convert decimal to new base for encoding
     public static String toBase(int targetBase, String initialLetter) {
        StringBuilder finalNum = new StringBuilder();
        int tempValue = 0;
        int num = initialLetter.charAt(0) - 96;
        

        while (num != 0) {
            tempValue = num % targetBase;
            finalNum.insert(0, tempValue);
            num /= targetBase;
        }
        return finalNum.toString();
    }

    // Convert from a base to decimal
    public static int toDecimal(int oldBase, String encryptedLetter) {
        int sum = 0;
        int power = 1;
        String[] digits = encryptedLetter.split("");

        for (int i = 0; i < digits.length; i++) {
            sum += (Integer.parseInt(digits[digits.length - (i + 1)]))*(power);
            power = power * oldBase;
        }
        return sum + 96;
    }

    // hash username and password
    public static String hashGenerator(String input) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(input.getBytes());
        byte[] digest = messageDigest.digest();
        StringBuilder hexString = new StringBuilder();

        for (int i = 0; i < digest.length; i++) {
            hexString.append(Integer.toHexString(0xFF & digest[i]));
        }
        
        return hexString.toString();
    } 

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        Random  generator = new Random();
        Scanner keyboard = new Scanner(System.in), inputFile;
        String fileName, menuSelection, password, encryptionKey = "", passwordEntry;
        File file;
        String[] temp, oldMessage = new String[0];
        char[] decryptedMessage;
        int targetBase = 0, oldBase = 0;
        Boolean fileExistenceFlag = true, targetBaseFlag = true;
        HashMap passwordKey = new HashMap();
        PrintWriter outputFile;
        FileWriter fw;

        file = new File("passwords:keys.txt");
        inputFile = new Scanner(file);
        
        // Populate passwordKey HashMap
        while(inputFile.hasNext()) {
            temp = inputFile.nextLine().split(" : ");
            passwordKey.put(temp[0], temp[1]);
        }
        inputFile.close();

        System.out.println("Welcome to NumBase Encoder:");
        System.out.print("Menu:\n\t1) Encrypt a message\n\t2) Decrypt a message\n\t3) Exit\n");

        do {
            System.out.print("\nEnter your choice: ");
            menuSelection = keyboard.nextLine();

            // Encrypt a message
            if (menuSelection.equals("1")) {

                // Get file name and check for its existence
                fileExistenceFlag = true; 
                targetBaseFlag = true;

                do {
                    fileExistenceFlag = true;
                    targetBaseFlag = true;
                    System.out.print("\nEnter the name of the file you would like to encrypt: ");
                    fileName = keyboard.nextLine();

                   
                    file = new File(fileName);

                    if (!file.exists()) {
                        System.out.println("The necessary file " + file + " does not exist. Try again.");
                        fileExistenceFlag = false;

                    } 
                    
                    else {
                        System.out.print("Enter the number base system you would like to convert the file to: ");
                        targetBase = keyboard.nextInt();
                        keyboard.nextLine();

                        if (targetBase > 10 || targetBase < 2) {
                            System.out.println("Cannot convert to this number base. Enter another number.");
                            targetBaseFlag = false;
                        }
                    }
                } while (!fileExistenceFlag || !targetBaseFlag);

                // Get password and check for length
                do {
                    System.out.print("Set a password for this encryption. It must at least 6 characters. ");
                    password = keyboard.nextLine();

                    // Check password length
                    if (password.length() < 6) {
                        System.out.println("\nPassword is too short. Enter a new one with at least 6 characters.");

                    } 
                } while (password.length() < 6);

                // Create key
                for (int i = 0; i < 6; i++) {
                    encryptionKey = encryptionKey + (char)generator.nextInt(96, 123) + generator.nextInt(9);
                }

                // Add key to HashMap and passwords:keys file
                passwordKey.put(password, encryptionKey);

                fw = new FileWriter("passwords:keys.txt", true);
                outputFile = new PrintWriter(fw);

                outputFile.println(password + " : " + encryptionKey);
                outputFile.close();

                // Create ArrayList of Arrays to store the message
                inputFile = new Scanner(file);
                ArrayList <String[]> message = new ArrayList <>();
                while(inputFile.hasNext()) {
                    temp = inputFile.nextLine().split("");
                    message.add(temp);
                }
                inputFile.close();

                // Convert letters to given base 
                for (int i = 0; i < message.size(); i++) {
                    temp = message.get(i);
                    for (int j = 0; j < temp.length; j++) {
                        if (temp[j].equals(" ")) {
                            temp[j] = Integer.toString(generator.nextInt(88, 98));
                        } else {
                            temp[j] = toBase(targetBase, temp[j]);
                        }
                    }
                    message.set(i, temp);
                }

                fw = new FileWriter("EncryptedFile.txt", true);
                outputFile = new PrintWriter(fw);

                // Output encrypted message to file
                outputFile.print(passwordKey.get(password) + "99" + targetBase + "99");
                for (int i = 0; i < message.size(); i++) {
                    temp = message.get(i);
                    for (int j = 0; j < temp.length; j++) {
                        outputFile.print(temp[j] + "98");
                    }
                    outputFile.print(generator.nextInt(77, 88));
                }
                outputFile.println();
                outputFile.close();
                System.out.println("\nEncryption complete");
            } 

            // Decrypt message
            else if (menuSelection.equals("2")) {
                file = new File("EncryptedFile.txt");
                inputFile = new Scanner(file);
                boolean passwordFlag = false;

                do {
                System.out.println("\nEnter the password for your encrypted message. If you would like to exit to the main menu enter q.");
                passwordEntry = keyboard.nextLine();

                    // Escape back to menu
                    if (passwordEntry.equals("q")) {
                        passwordFlag = true;
                    }

                    // Invalid key
                    else if (!passwordKey.containsKey(passwordEntry)) {
                        System.out.println("Invalid password. There is no encryption with this password. Try again.");
                    }

                    // Valid key
                    else {
                        passwordFlag = true;

                        // Find line with correct key
                        while (inputFile.hasNext()) {
                            temp = inputFile.nextLine().split("99");

                            if (temp[0].equals(passwordKey.get(passwordEntry))) {
                                oldBase = Integer.parseInt(temp[1]);
                                oldMessage = temp[2].split("98");
                                break;
                            }
                        }

                        // Convert encrypted characters to letters and escape sequences and print them
                        System.out.println("\nYour message is\n");
                        decryptedMessage = new char[oldMessage.length];
                        for (int i = 0; i < oldMessage.length; i++) {

                            // Convert to space characters
                            if (Integer.parseInt(oldMessage[i]) > 87 && Integer.parseInt(oldMessage[i]) < 98) {
                                decryptedMessage[i] = ' ';
                            }
                            
                            // Convert to newline characters
                            else if (Integer.parseInt(oldMessage[i]) > 76 && Integer.parseInt(oldMessage[i]) < 88) {
                                decryptedMessage[i] = '\n';
                            }

                            // Convert to decimal base then to characters
                            else {
                                decryptedMessage[i] = (char)(toDecimal(oldBase, oldMessage[i]));
                            }
                            //System.out.print(decryptedMessage[i]);
                            System.out.print(decryptedMessage[i]);
                        }
                    }
                } while(!passwordFlag);


            }
        } while(true);

    }
}