package stateSpace;

import java.util.HashMap;
import java.util.Map;
import java.lang.StringBuilder;
import dataStructure.Variable;
import dataStructure.DiscreteVariable;
import dataStructure.ContinuousVariable;

public class Message {

    private String senderActor;
    private String receiverActor;
    private String serverName;            // name of the handler in the receiver actor
    private HashMap<String, Variable> parameters;   // parameters of the handler in the receiver actor
    private ContinuousVariable arrivalTime;

    public Message(
            String senderActor,
            String receiverActor,
            String serverName,
            HashMap<String, Variable> parameters,
            ContinuousVariable arrivalTime
    ) {
        this.senderActor = senderActor;
        this.receiverActor = receiverActor;
        this.serverName = serverName;
        this.parameters = parameters;
        this.arrivalTime = arrivalTime;
    }

    public String getSenderActor() {
        return senderActor;
    }

    public String getReceiverActor() {
        return receiverActor;
    }

    public String getServerName() {
        return ServerName;
    }

    public HashMap<String, Variable> getParameters() {
        return parameters;
    }

    public ContinuousVariable getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(ContinuousVariable arrivalTime) {
        if (arrivalTime.isValid()) {
            this.arrivalTime = arrivalTime;
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Sender Actor: ").append(getSenderActor()).append("\n");
        stringBuilder.append("Receiver Actor: ").append(getReceiverActor()).append("\n");
        stringBuilder.append("Server Name: ").append(getServerName()).append("\n");
        stringBuilder.append("Arrival Time: ").append(getArrivalTime().toString()).append("\n");
        stringBuilder.append("Parameters: ").append("\n");
        for (Map.Entry<String, Variable> entry : getParameters().entrySet()) {
            stringBuilder.append(entry.getKey()).append(": ").append(entry.getValue().toString()).append("\n");
        }
        return stringBuilder.toString();
    }

}
