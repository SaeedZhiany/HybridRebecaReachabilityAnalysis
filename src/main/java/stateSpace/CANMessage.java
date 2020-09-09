package stateSpace;

public class CANMessage {

    private Integer priority;
    private Message message;

    public CANMessage(Message message, Integer priority) {
        this.message = message;
        this.priority = priority;
    }

    public Message getMessage() {
        return message;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }
}
