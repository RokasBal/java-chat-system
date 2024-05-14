package Resources;

/**
 * Class, which saves data about a single message.
 */

public class Message {
    private String room;     /** Room, in which the message was sent */
    private String user;     /** User, who sent the message */
    private String message;  /** The message itself */

    /**
     * Constructor, initializing the message.
     *
     * @param room The room, in which the message was sent.
     * @param user The user, who sent the message.
     * @param message The message itself.
     */

    public Message(String room, String user, String message) {
        this.room = room;
        this.user = user;
        this.message = message;
    }

    /**
     * Getter for the message.
     *
     * @return The message to give.
     */

    public String getMessage() {
        return message;
    }

    /**
     * Getter for the user.
     *
     * @return The user to give.
     */

    public String getUser() {
        return user;
    }

     /**
     * Getter for the room.
     *
     * @return The room to give.
     */

    public String getRoom() { return room; }
}
