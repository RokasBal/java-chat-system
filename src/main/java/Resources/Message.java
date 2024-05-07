package Resources;

public class Message {
    private String user;
    private String message;

    public Message(String user, String message) {
        this.user = user;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String getUser() {
        return user;
    }
}
