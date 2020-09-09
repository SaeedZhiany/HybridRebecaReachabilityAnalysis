package stateSpace;

import java.util.HashMap;
import java.util.Map;

public class Message {

    private String senderActor;
    private String receiverActor;
    private Map.Entry<String, HashMap<String, Number>> message;
    private Float arrivalTime;

    public Message(
            String senderActor,
            String receiverActor,
            Map.Entry<String, HashMap<String, Number>> message,
            float arrivalTime
    ) {
        this.senderActor = senderActor;
        this.receiverActor = receiverActor;
        this.message = message;
        this.arrivalTime = arrivalTime;
    }

    public String getSenderActor() {
        return senderActor;
    }

    public String getReceiverActor() {
        return receiverActor;
    }

    public Map.Entry<String, HashMap<String, Number>> getMessage() {
        return message;
    }

    public Float getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(float arrivalTime) {
        if (arrivalTime >= 0) {
            this.arrivalTime = arrivalTime;
        }
    }
}
