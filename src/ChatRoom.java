import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class ChatRoom {
    private ServerSocket serverSocket;
    private Map<String, ObjectOutputStream> clientStreams;

    public ChatRoom() {
        this.serverSocket = serverSocket;
        this.clientStreams = clientStreams;
    }

    public ChatRoom(int port) {
        try {
            serverSocket = new ServerSocket(port);
            clientStreams = new ConcurrentHashMap<>();
            System.out.println("Chat Room server started on port " + port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void acceptClients() {
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ChatUser newUser = new ChatUser(clientSocket, this);
                new Thread(newUser).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addClient(String username, ObjectOutputStream outputStream) {
        clientStreams.put(username, outputStream);
        broadcastMessage(username + " has joined the chat.", "SERVER");
    }

    public void broadcastMessage(String message, String senderUsername) {
        for (Map.Entry<String, ObjectOutputStream> entry : clientStreams.entrySet()) {
            String username = entry.getKey();
            if (!username.equals(senderUsername)) {
                sendMessageToClient(message, entry.getValue());
            }
        }
    }

    private void sendMessageToClient(String message, ObjectOutputStream outputStream) {
        try {
            outputStream.writeObject(new ChatMessage("SERVER", message));
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeClient(String username) {
        clientStreams.remove(username);
        broadcastMessage(username + " has left the chat.", "SERVER");
    }

    public void changeUsername(String oldUsername, String newUsername) {
        ObjectOutputStream outputStream = clientStreams.remove(oldUsername);
        clientStreams.put(newUsername, outputStream);
        broadcastMessage(oldUsername + " changed their name to " + newUsername, "SERVER");
    }

    public void sendPrivateMessage(String sender, String recipient, String message) {
        ObjectOutputStream recipientStream = clientStreams.get(recipient);
        if (recipientStream != null) {
            sendMessageToClient("Private message from " + sender + ": " + message, recipientStream);
            sendMessageToClient("Private message to " + recipient + ": " + message, clientStreams.get(sender));
        } else {
            sendMessageToClient("User " + recipient + " not found.", clientStreams.get(sender));
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the port for the Chat Room server: ");
        int port = scanner.nextInt();

        ChatRoom chatRoom = new ChatRoom(port);
        chatRoom.acceptClients();
        scanner.close();
    }
}
