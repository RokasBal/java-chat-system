package Resources;

public class Message {
    private String room;
    private String user;
    private String message;

    public Message(String room, String user, String message) {
        this.room = room;
        this.user = user;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String getUser() {
        return user;
    }
    public String getRoom() { return room; }
}
