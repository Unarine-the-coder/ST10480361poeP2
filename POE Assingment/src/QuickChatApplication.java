import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.JOptionPane;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class QuickChatApplication {
    private static String savedUsername = "";
    private static String savedPassword = "";
    private static String savedPhone = "";
    private static String userFirstName = "User ";
    private static String userLastName = "Name";

    private static ArrayList<Message> messages = new ArrayList<>();
    private static int totalMessagesSent = 0;
    private static final String MESSAGES_FILE = "messages.json";

    public static void main(String[] args) throws JSONException {
        Scanner input = new Scanner(System.in);
        System.out.println("=== Welcome to QuickChat ===");
        System.out.println("1. Create a new account");
        System.out.println("2. Login to an existing account");
        System.out.print("Choose an option (1 or 2): ");

        int choice = input.nextInt();
        input.nextLine();

        switch (choice) {
            case 1:
                createAccount(input);
                break;
            case 2:
                login(input);
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }

        input.close();
    }

    static void createAccount(Scanner scanner) throws JSONException {
        System.out.println("=== Create Your Account ===");
        String username = getValidUsername(scanner);
        String password = getValidPassword(scanner);
        String phone = getValidPhone(scanner);
        savedUsername = username;
        savedPassword = password;
        savedPhone = phone;
        System.out.println("Account created successfully!");
        System.out.println("You can now log in with your new credentials.");
        showMessagingMenu(scanner);
    }

    static String getValidUsername(Scanner scanner) {
        String username;
        while (true) {
            System.out.print("Choose a username (must contain '_' and be 5 characters or less): ");
            username = scanner.nextLine();
            if (username.length() <= 5 && username.contains("_")) {
                System.out.println("Username looks good!");
                break;
            } else {
                System.out.println("Oops! Username needs an underscore and must be 5 characters or less.");
            }
        }
        return username;
    }

    static String getValidPassword(Scanner scanner) {
        String password;
        while (true) {
            System.out.print("Create a password (8+ chars, with capital, number, special char): ");
            password = scanner.nextLine();
            if (isPasswordStrong(password)) {
                System.out.println("Great password!");
                break;
            } else {
                System.out.println("Password needs to be stronger. Make sure it has:");
                System.out.println("- At least 8 characters");
                System.out.println("- One capital letter");
                System.out.println("- One number");
                System.out.println("- One special character (!@#$ etc.)");
            }
        }
        return password;
    }

    static String getValidPhone(Scanner scanner) {
        String phone;
        while (true) {
            System.out.print("Enter your South African phone number (with country code): ");
            phone = scanner.nextLine();
            if (isValidPhone(phone)) {
                System.out.println("Phone number accepted!");
                break;
            } else {
                System.out.println("Please enter a valid SA number with country code (like +27821234567)");
            }
        }
        return phone;
    }

    static void login(Scanner scanner) throws JSONException {
        if (savedUsername.isEmpty()) {
            System.out.println("No accounts exist yet. Please create one first.");
            return;
        }

        System.out.println("=== Login to Your Account ===");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        if (username.equals(savedUsername) && password.equals(savedPassword)) {
            System.out.printf("Welcome back %s %s! Great to see you again.%n", userFirstName, userLastName);
            showMessagingMenu(scanner);
        } else {
            System.out.println("Login failed. Incorrect username or password.");
        }
    }

    static void showMessagingMenu(Scanner scanner) throws JSONException {
        System.out.println("Welcome to QuickChat");

        while (true) {
            System.out.println("Please choose an option:");
            System.out.println("1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Quit");
            System.out.print("Enter your choice (1-3): ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    sendMessages(scanner);
                    break;
                case 2:
                    System.out.println("Coming Soon.");
                    break;
                case 3:
                    System.out.println("Thank you for using QuickChat. Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice. Please enter 1, 2, or 3.");
            }
        }
    }

    static void sendMessages(Scanner scanner) throws JSONException {
        System.out.print("How many messages would you like to send? ");
        int numMessages = scanner.nextInt();
        scanner.nextLine();

        for (int i = 0; i < numMessages; i++) {
            System.out.println("Message " + (i + 1) + " of " + numMessages);
            String messageId = generateMessageId();
            String recipient = getValidRecipient(scanner);
            String content = getValidMessageContent(scanner);
            String messageHash = createMessageHash(messageId, i + 1, content);
            Message message = new Message(messageId, recipient, content, messageHash, i + 1);
            int action = getMessageAction(scanner);

            switch (action) {
                case 1:
                    message.setStatus("Sent");
                    messages.add(message);
                    totalMessagesSent++;
                    System.out.println("Message successfully sent.");
                    displayMessageDetails(messageId, messageHash, recipient, content);
                    break;
                case 2:
                    System.out.println("Message disregarded.");
                    break;
                case 3:
                    message.setStatus("Stored");
                    messages.add(message);
                    storeMessageToJson(message);
                    System.out.println("Message successfully stored.");
                    break;
                default:
                    System.out.println("Invalid choice. Message not sent.");
            }
        }

        System.out.println("Total messages sent in this session: " + totalMessagesSent);
    }

    static String getValidRecipient(Scanner scanner) {
        String recipient;
        while (true) {
            System.out.print("Recipient's phone number (with country code): ");
            recipient = scanner.nextLine();
            if (isValidPhone(recipient)) {
                break;
            } else {
                System.out.println("Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.");
            }
        }
        return recipient;
    }

    static String getValidMessageContent(Scanner scanner) {
        String content;
        while (true) {
            System.out.print("Message (max 250 characters): ");
            content = scanner.nextLine();
            if (content.length() <= 250) {
                break;
            } else {
                int excess = content.length() - 250;
                System.out.printf("Message exceeds 250 characters by %d, please reduce size.%n", excess);
            }
        }
        return content;
    }

    static int getMessageAction(Scanner scanner) {
        System.out.println("What would you like to do with this message?");
        System.out.println("1) Send Message");
        System.out.println("2) Disregard Message");
        System.out.println("3) Store Message to send later");
        System.out.print("Enter your choice (1-3): ");
        return scanner.nextInt();
    }

    static void displayMessageDetails(String messageId, String messageHash, String recipient, String content) {
        String details = String.format("Message ID: %s%nMessage Hash: %s%nRecipient: %s%nMessage: %s", messageId, messageHash, recipient, content);
        JOptionPane.showMessageDialog(null, details, "Message Sent", JOptionPane.INFORMATION_MESSAGE);
    }

    static String generateMessageId() {
        Random rand = new Random();
        long id = 1000000000L + rand.nextInt(900000000);
        return String.valueOf(id);
    }

    static String createMessageHash(String messageId, int messageNumber, String content) {
        String[] words = content.split("\\s+");
        String firstWord = words.length > 0 ? words[0] : "";
        String lastWord = words.length > 0 ? words[words.length - 1] : "";
        return String.format("%s:%d:%s%s", messageId.substring(0, 2), messageNumber, firstWord.toUpperCase(), lastWord.toUpperCase());
    }

    static void storeMessageToJson(Message message) throws JSONException {
        try {
            JSONArray jsonArray;

            // Read existing JSON file if it exists
            if (Files.exists(Paths.get(MESSAGES_FILE))) {
                String content = new String(Files.readAllBytes(Paths.get(MESSAGES_FILE)));
                try {
                    jsonArray = new JSONArray(content);
                } catch (JSONException e) {
                    // File exists but contents are invalid, start fresh
                    jsonArray = new JSONArray();
                }
            } else {
                jsonArray = new JSONArray();
            }

            // Create new message JSON object
            JSONObject jsonMessage = new JSONObject();
            jsonMessage.put("messageId", message.getMessageId());
            jsonMessage.put("content", message.getContent());
            jsonMessage.put("recipient", message.getRecipient());
            jsonMessage.put("messageHash", message.getMessageHash());
            jsonMessage.put("status", message.getStatus());
            jsonMessage.put("messageNumber", message.getMessageNumber());

            // Add message to array
            jsonArray.put(jsonMessage);

            // Write updated array back to file
            try (FileWriter file = new FileWriter(MESSAGES_FILE)) {
                file.write(jsonArray.toString(4)); // Pretty-print with indent
            }

        } catch (IOException e) {
            System.out.println("Error storing message: " + e.getMessage());
        }
    }

    static boolean isPasswordStrong(String password) {
        if (password.length() < 8) return false;
        boolean hasUpper = false;
        boolean hasNumber = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            if (Character.isDigit(c)) hasNumber = true;
            if (!Character.isLetterOrDigit(c)) hasSpecial = true;
        }

        return hasUpper && hasNumber && hasSpecial;
    }

    static boolean isValidPhone(String phone) {
        String regex = "^(\\+?27|0)[0-9]{9}$";
        return Pattern.matches(regex, phone);
    }
}

class Message {
    private String messageId;
    private String recipient;
    private String content;
    private String messageHash;
    private String status;
    private int messageNumber;

    public Message(String messageId, String recipient, String content, String messageHash, int messageNumber) {
        this.messageId = messageId;
        this.recipient = recipient;
        this.content = content;
        this.messageHash = messageHash;
        this.messageNumber = messageNumber;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessageId() { return messageId; }
    public String getRecipient() { return recipient; }
    public String getContent() { return content; }
    public String getMessageHash() { return messageHash; }
    public String getStatus() { return status; }
    public int getMessageNumber() { return messageNumber; }
}
