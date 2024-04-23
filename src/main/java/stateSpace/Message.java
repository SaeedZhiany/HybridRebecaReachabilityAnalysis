package stateSpace;

import java.util.HashMap;
import java.util.Map;
import java.lang.StringBuilder;
import java.util.Objects;

import dataStructure.*;
import org.apache.commons.lang3.RandomStringUtils;

public class Message {
    private String id;
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
        this.id = RandomStringUtils.randomAlphanumeric(15);
    }

    public Message(Message message) {
        this.senderActor = message.getSenderActor();
        this.receiverActor = message.getReceiverActor();
        this.serverName = message.getServerName();
        HashMap<String, Variable> newParameters = new HashMap<>();
        for (Map.Entry<String, Variable> entry : message.getParameters().entrySet()) {
            if (entry.getValue() instanceof DiscreteDecimalVariable) {
                newParameters.put(entry.getKey(), new DiscreteDecimalVariable((DiscreteDecimalVariable) entry.getValue()));
            } else if (entry.getValue() instanceof ContinuousVariable) {
                newParameters.put(entry.getKey(), new ContinuousVariable((ContinuousVariable) entry.getValue()));
            } else if (entry.getValue() instanceof IntervalRealVariable) {
                newParameters.put(entry.getKey(), new IntervalRealVariable((IntervalRealVariable) entry.getValue()));
            } else if (entry.getValue() instanceof DiscreteBoolVariable) {
                newParameters.put(entry.getKey(), new DiscreteBoolVariable((DiscreteBoolVariable) entry.getValue()));
            }
        }
        this.parameters = newParameters;
        this.arrivalTime = new ContinuousVariable(message.getArrivalTime());
    }

    public String getSenderActor() {
        return senderActor;
    }

    public String getReceiverActor() {
        return receiverActor;
    }

    public String getServerName() {
        return serverName;
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

    public boolean checkBounds(ContinuousVariable globalTime) {
        if (this.arrivalTime.getLowerBound().compareTo(globalTime.getLowerBound()) <= 0) {
            return true;
        }
        return false;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Message)) {
            return false;
        }
        Message objMessage = (Message) obj;
        return this.id.equals(objMessage.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
