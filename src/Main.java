public class Main {
    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]); // Get port from console
        new ChatRoom(port);
    }
}