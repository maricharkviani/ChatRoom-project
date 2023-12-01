package server;

import client.ChatMessage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatRoomServer {
    private static List<Socket> sockets = new ArrayList<>();
    private static int port;
    private static Map<String, Socket> user = new HashMap<>();
    private static DataOutputStream dataOutputStream;


    public static void acceptClients() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Server address: localhost");
        System.out.println("If you want to change username write: /changeUsername");
        System.out.println("If you want to send private messages in chat write: /private");
        System.out.println("If you want to exit chat write: /exit");
        System.out.println("Enter the server port: ");
        port = scanner.nextInt();
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                while (true) {
                    Socket socket = serverSocket.accept();
                    sockets.add(socket);
                    new Thread(() -> handleClient(socket)).start();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public static void sendMessageToClient(String message, Socket socket) {
        try {
            OutputStream outputStream = socket.getOutputStream();
            dataOutputStream = new DataOutputStream(outputStream);
            dataOutputStream.writeUTF(message);
            dataOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void broadcastMessageToAllExceptSender(String message, Socket senderSocket) {
        for (int i = 0; i < sockets.size(); i++) {
            if (sockets.get(i) != senderSocket) {
                try {
                    DataOutputStream dataOutputStream = new DataOutputStream(sockets.get(i).getOutputStream());
                    dataOutputStream.writeUTF(message);
                    dataOutputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void handleClient(Socket socket) {
        try {
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

            String message;
            while (true) {
                message = dataInputStream.readUTF();
                System.err.println(message);
                if (message.startsWith("/")) {
                    processCommand(message, socket);
                } else {
                    broadcastMessageToAllExceptSender(message, socket);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void changeUsername(String newUsername, Socket socket) {
        user.put(newUsername, socket);
        sockets.remove(newUsername);
    }

    public static void sendPrivateMessage(String sender, String receiver, String message) {
        if (user.containsKey(receiver)) {
            sendMessageToClient(sender + message, user.get(receiver));
        }
    }


    private static void processCommand(String command, Socket socket) {
        String[] parts = command.substring(1).split(" ", 2);
        String cmd = parts[0].toLowerCase(Locale.ROOT);
        String value = "";
        for (int i = 1; i < parts.length; i++) {
            value += parts[i];
        }
        switch (command) {
            case "/changeUsername":
                String newUsername = parts[1];
                changeUsername(newUsername, socket);
                break;
            case "/private": {
                Pattern pattern = Pattern.compile("\\/private (\\w+)");
                Matcher matcher = pattern.matcher(command);
                String sender = null;
                String receiver = null;
                if (matcher.find()) {
                    sender = matcher.group(1);
                    if (matcher.find()) {
                        receiver = matcher.group(1);
                    }
                }
                String message = null;
                pattern = Pattern.compile(":(.*)");
                matcher = pattern.matcher(command);
                if (matcher.find()) {
                    message = matcher.group(1);
                }
                matcher = pattern.matcher(command);
                if (matcher.find()) {
                    message = matcher.group(1);
                }
                sendPrivateMessage(sender, receiver, message);
                break;
            }
            case "/exit": {
                System.exit(0);
            }
        }

    }

    public static void main(String[] args) {
        acceptClients();

    }

}