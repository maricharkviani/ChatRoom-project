import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

public class ClientHandler extends ChatRoom implements Runnable {
    public Socket socket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private String username;

    public ClientHandler(ServerSocket serverSocket) {
        super(serverSocket);
    }


    @Override
    public void run() {
        try {
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());

            outputStream.writeObject(new Message("Server", "Welcome to the chat!"));

            // Get username
            username = (String) inputStream.readObject();
            clients.put(username, outputStream);

            // Notify others abt new user
            broadcastMessage(new Message("Server", username + " joined the chat."));

            while (true) {
                Message message = (Message) inputStream.readObject();
                // Exits
                if (message.content.equalsIgnoreCase("/exit")) {
                    break;
                } else if (message.content.startsWith("/private")) {
                    // Private messages
                    String[] parts = message.content.split(" ", 3);
                    String recipient = parts[1];
                    String privateContent = parts[2];
                    clients.get(recipient).writeObject(new Message(username, privateContent));
                } else if (message.content.startsWith("/changename")) {
                    // Change username request
                    String[] parts = message.content.split(" ", 2);
                    String newUsername = parts[1];
                    clients.remove(username);
                    username = newUsername;
                    clients.put(username, outputStream);
                    broadcastMessage(new Message("Server", "User " + message.sender + " changed name to " + username));
                } else {
                    // Broadcast to all
                    broadcastMessage(message);
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            // "..." left the chat
            clients.remove(username);
            broadcastMessage(new Message("Server", username + " left the chat."));
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
