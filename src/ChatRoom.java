import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatRoom {
    public ServerSocket serverSocket;
    public Map<String, ObjectOutputStream> clients;

    public ChatRoom(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public ChatRoom(int port) {
        clients = new ConcurrentHashMap<>();

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Chat room started on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new ClientHandler(serverSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcastMessage(Message message) {
        for (ObjectOutputStream clientStream : clients.values()) {
            try {
                clientStream.writeObject(message);
                clientStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}