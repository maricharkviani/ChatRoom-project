package client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ChatUser {
    private static Socket socket;

    public static void sendMessage() {
        String message;
        while (true) {
            Scanner scanner = new Scanner(System.in);
            message = scanner.nextLine();
            try {
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataOutputStream.writeUTF(message);
                dataOutputStream.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void receiveMessage() {
        String message;
        while (true) {
            try {
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                message = dataInputStream.readUTF();
                System.out.println("Received: " + message);
            } catch (IOException e) {
                throw new RuntimeException(e);
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
            socket = new Socket(serverAddress, serverPort);
            new Thread(ChatUser::sendMessage).start();
            new Thread(ChatUser::receiveMessage).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
