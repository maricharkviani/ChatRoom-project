public class Message {
    private static final long serialVersionUID = 1L;
    String sender;
    String content;

    public Message(String sender, String content) {
        this.sender = sender;
        this.content = content;
    }
}
