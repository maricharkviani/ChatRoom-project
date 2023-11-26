import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ChatUser extends ChatRoom implements Runnable {
    private Socket socket;
    private ChatRoom chatRoom;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username;

    public ChatUser(Socket socket, ChatRoom chatRoom) {
        super();
        this.socket = socket;
        this.chatRoom = chatRoom;
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void sendMessage(ChatMessage message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processCommand(String command) {
        String[] parts = command.substring(1).split(" ", 2);
        String cmd = parts[0].toLowerCase();

        switch (cmd) {
            case "changename":
                if (parts.length == 2) {
                    String newUsername = parts[1];
                    chatRoom.changeUsername(username, newUsername);
                    setUsername(newUsername);
                } else {
                    sendMessage(new ChatMessage("SERVER", "Invalid /changename command. Usage: /changename newUsername"));
                }
                break;

            case "private":
                if (parts.length == 2) {
                    String[] privateParts = parts[1].split(" ", 2);
                    String recipient = privateParts[0];
                    String message = privateParts[1];
                    chatRoom.sendPrivateMessage(username, recipient, message);
                } else {
                    sendMessage(new ChatMessage("SERVER", "Invalid /private command. Usage: /private recipient message"));
                }
                break;

            default:
                sendMessage(new ChatMessage("SERVER", "Unknown command: " + command));
                break;
        }
    }

    public void run() {
        try {
            sendMessage(new ChatMessage("server", "Welcome to the ChatRoom!"));
            while (true) {
                ChatMessage receivedMessage = (ChatMessage) in.readObject();

                if (receivedMessage != null & receivedMessage.getContent().startsWith("/")) {
                    processCommand(receivedMessage.getContent());
                } else {
                    chatRoom.broadcastMessage(receivedMessage.getContent(), receivedMessage.getSender());
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            chatRoom.removeClient(username);
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the server address: ");
        String serverAddress = scanner.nextLine();

        System.out.print("Enter the server port: ");
        int serverPort = scanner.nextInt();

        try {
            Socket socket = new Socket(serverAddress, serverPort);
            ChatUser chatUser = new ChatUser(socket, null);
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

            System.out.print("Enter your username: ");
            String username = userInput.readLine();
            chatUser.setUsername(username);

            new Thread(chatUser).start();

            String userInputMessage;
            while ((userInputMessage = userInput.readLine()) != null) {
                chatUser.sendMessage(new ChatMessage(username, userInputMessage));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}
